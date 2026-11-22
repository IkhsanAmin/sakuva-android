package com.sakuva.app.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.designsystem.syncApplicationNightMode
import com.sakuva.app.core.locale.Language
import com.sakuva.app.core.time.DateRangePreset
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.data.io.BackupError
import com.sakuva.app.data.io.BackupException
import com.sakuva.app.data.io.BackupJsonManager
import com.sakuva.app.data.io.CsvExportManager
import com.sakuva.app.data.local.DataResetManager
import com.sakuva.app.domain.model.AppSettings
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.TransactionRepository
import com.sakuva.app.domain.repository.WalletRepository
import com.sakuva.app.feature.common.Feedback
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val feedback: Feedback? = null,
    val lastBackupAt: Long? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val backupJsonManager: BackupJsonManager,
    private val csvExportManager: CsvExportManager,
    private val dataResetManager: DataResetManager,
) : ViewModel() {

    private val feedback = MutableStateFlow<Feedback?>(null)
    private val busy = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeSettings(),
        feedback,
        busy,
    ) { settings, currentFeedback, isBusy ->
        SettingsUiState(
            isLoading = false,
            isBusy = isBusy,
            settings = settings,
            feedback = currentFeedback,
            lastBackupAt = settings.lastBackupAt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
            syncApplicationNightMode(context, themeMode)
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.setCurrency(currencyCode)
        }
    }

    fun backup(uri: Uri) {
        viewModelScope.launch {
            busy.value = true
            try {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Unable to open backup destination")
                    output.use { stream -> backupJsonManager.export(stream) }
                    settingsRepository.setLastBackupAt(System.currentTimeMillis())
                }
                feedback.value = Feedback.BACKUP_SUCCESS
            } catch (_: Exception) {
                feedback.value = Feedback.ERROR
            } finally {
                busy.value = false
            }
        }
    }

    fun restore(uri: Uri) {
        viewModelScope.launch {
            busy.value = true
            try {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open backup source")
                    input.use { stream -> backupJsonManager.restore(stream) }
                }
                feedback.value = Feedback.RESTORE_SUCCESS
            } catch (error: BackupException) {
                feedback.value = if (error.error == BackupError.FILE_TOO_LARGE) {
                    Feedback.FILE_TOO_LARGE
                } else {
                    Feedback.INVALID_BACKUP
                }
            } catch (_: Exception) {
                feedback.value = Feedback.ERROR
            } finally {
                busy.value = false
            }
        }
    }

    fun exportCsv(uri: Uri, preset: DateRangePreset) {
        viewModelScope.launch {
            busy.value = true
            try {
                val currencyCode = uiState.value.settings.currencyCode
                withContext(Dispatchers.IO) {
                    val wallets = walletRepository.getAllWallets()
                    val categories = categoryRepository.getAllCategories()
                    val range = DateTimeUtils.rangeForPreset(preset)
                    val transactions = transactionRepository.getAllTransactions()
                        .filter { it.dateEpochDay in range.startEpochDay..range.endEpochDay }
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Unable to open export destination")
                    output.use { stream ->
                        csvExportManager.export(
                            outputStream = stream,
                            transactions = transactions,
                            wallets = wallets,
                            categories = categories,
                            currencyCode = currencyCode,
                        )
                    }
                }
                feedback.value = Feedback.EXPORT_SUCCESS
            } catch (_: Exception) {
                feedback.value = Feedback.ERROR
            } finally {
                busy.value = false
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            busy.value = true
            try {
                withContext(Dispatchers.IO) {
                    dataResetManager.resetAllData()
                    settingsRepository.clearLastBackupAt()
                }
                feedback.value = Feedback.DATA_RESET
            } catch (_: Exception) {
                feedback.value = Feedback.ERROR
            } finally {
                busy.value = false
            }
        }
    }

    fun setBackupReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackupReminderEnabled(enabled)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderEnabled(enabled)
            if (enabled) {
                val settings = settingsRepository.observeSettings().first()
                DailyReminderScheduler.schedule(
                    context,
                    settings.dailyReminderHour,
                    settings.dailyReminderMinute,
                )
            } else {
                DailyReminderScheduler.cancel(context)
            }
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderTime(hour, minute)
            if (uiState.value.settings.dailyReminderEnabled) {
                DailyReminderScheduler.schedule(context, hour, minute)
            }
        }
    }

    fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun consumeFeedback() {
        feedback.value = null
    }
}
