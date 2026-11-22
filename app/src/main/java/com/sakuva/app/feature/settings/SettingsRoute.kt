package com.sakuva.app.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakuva.app.core.time.DateRangePreset

@Composable
fun SettingsRoute(
    onManageCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRestoreConfirm by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var pendingExportPreset by remember { mutableStateOf<DateRangePreset?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.backup(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        val preset = pendingExportPreset ?: DateRangePreset.THIS_MONTH
        if (uri != null) {
            viewModel.exportCsv(uri, preset)
        }
        pendingExportPreset = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            uiState = uiState,
            onThemeSelected = viewModel::setThemeMode,
            onLanguageSelected = viewModel::setLanguage,
            onCurrencySelected = viewModel::setCurrency,
            onBackup = { backupLauncher.launch("sakuva-backup.json") },
            onRestore = { restoreLauncher.launch(arrayOf("application/json")) },
            onExportCsv = { showExportDialog = true },
            onManageCategories = onManageCategories,
            onBackupReminderToggle = viewModel::setBackupReminderEnabled,
            onDailyReminderToggle = viewModel::setDailyReminderEnabled,
            onTimeSelected = viewModel::setDailyReminderTime,
            onResetData = { showResetDialog = true },
        )

        if (showExportDialog) {
            ExportPeriodDialog(
                onConfirm = { preset ->
                    pendingExportPreset = preset
                    showExportDialog = false
                    exportLauncher.launch("sakuva-report.csv")
                },
                onDismiss = { showExportDialog = false },
            )
        }

        if (showRestoreConfirm) {
            RestoreConfirmDialog(
                onConfirm = {
                    showRestoreConfirm = false
                    pendingRestoreUri?.let { viewModel.restore(it) }
                    pendingRestoreUri = null
                },
                onDismiss = {
                    showRestoreConfirm = false
                    pendingRestoreUri = null
                },
            )
        }

        if (showResetDialog) {
            ResetDataDialog(
                onConfirm = {
                    showResetDialog = false
                    viewModel.resetAllData()
                },
                onDismiss = { showResetDialog = false },
            )
        }
    }
}
