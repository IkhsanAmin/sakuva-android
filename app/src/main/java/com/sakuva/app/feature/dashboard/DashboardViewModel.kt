package com.sakuva.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.AppSettings
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryExpense
import com.sakuva.app.domain.model.DashboardSummary
import com.sakuva.app.domain.model.ExpenseTrendPoint
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.TransactionRepository
import com.sakuva.app.domain.repository.WalletRepository
import com.sakuva.app.domain.usecase.GetCategoryExpensesUseCase
import com.sakuva.app.domain.usecase.GetDashboardSummaryUseCase
import com.sakuva.app.domain.usecase.GetExpenseTrendUseCase
import com.sakuva.app.domain.usecase.GetRecentTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = "IDR",
    val summary: DashboardSummary = DashboardSummary(0, 0, 0),
    val expenseTrend: List<ExpenseTrendPoint> = emptyList(),
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val walletNames: Map<Long, String> = emptyMap(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val showBackupReminder: Boolean = false,
    val lastBackupAt: Long? = null,
)

private data class DashboardContext(
    val settings: AppSettings,
    val wNames: Map<Long, String>,
    val cNames: Map<Long, String>,
    val summary: DashboardSummary,
    val trend: List<ExpenseTrendPoint>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    getDashboardSummary: GetDashboardSummaryUseCase,
    getExpenseTrend: GetExpenseTrendUseCase,
    getCategoryExpenses: GetCategoryExpensesUseCase,
    getRecentTransactions: GetRecentTransactionsUseCase,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(PeriodType.MONTHLY)
    val period: StateFlow<PeriodType> = selectedPeriod.asStateFlow()
    private val dismissBackupReminder = MutableStateFlow(false)

    private val contextFlow = combine(
        settingsRepository.observeSettings(),
        walletRepository.observeAllWallets().map { it.associate { w -> w.id to w.name } },
        categoryRepository.observeAllCategories().map { it.associate { c -> c.id to c.name } },
        selectedPeriod.flatMapLatest { getDashboardSummary(it) },
        selectedPeriod.flatMapLatest { getExpenseTrend(it) },
    ) { settings, wNames, cNames, summary, trend ->
        DashboardContext(settings, wNames, cNames, summary, trend)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        contextFlow,
        selectedPeriod.flatMapLatest { getCategoryExpenses(it) },
        getRecentTransactions(),
        transactionRepository.observeRecent(1).map { it.isNotEmpty() },
        dismissBackupReminder,
    ) { ctx, categories, recent, hasTransactions, dismissed ->
        val now = System.currentTimeMillis()
        val lastBackupAt = ctx.settings.lastBackupAt ?: 0L
        val firstLaunchAt = ctx.settings.firstLaunchTimestamp
        val referenceTime = if (lastBackupAt > 0L) lastBackupAt else firstLaunchAt
        val isBackupReminderEnabled = ctx.settings.backupReminderEnabled
        val backupIsStale = referenceTime > 0L && (now - referenceTime) > BACKUP_REMINDER_INTERVAL_MS
        DashboardUiState(
            isLoading = false,
            currencyCode = ctx.settings.currencyCode,
            summary = ctx.summary,
            expenseTrend = ctx.trend,
            categoryExpenses = categories,
            recentTransactions = recent,
            walletNames = ctx.wNames,
            categoryNames = ctx.cNames,
            showBackupReminder = isBackupReminderEnabled && backupIsStale && hasTransactions && !dismissed,
            lastBackupAt = ctx.settings.lastBackupAt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    fun selectPeriod(period: PeriodType) {
        selectedPeriod.value = period
    }

    fun dismissBackupReminder() {
        dismissBackupReminder.value = true
    }

    private companion object {
        const val BACKUP_REMINDER_INTERVAL_MS = 7L * 24L * 60L * 60L * 1000L
    }
}
