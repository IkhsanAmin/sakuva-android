package com.sakuva.app.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardRoute(
    onAddTransaction: () -> Unit,
    onSeeAllHistory: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onBackupNow: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.period.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = viewModel::selectPeriod,
        onAddTransaction = onAddTransaction,
        onSeeAllHistory = onSeeAllHistory,
        onTransactionClick = onTransactionClick,
        onBackupNow = onBackupNow,
        onDismissBackup = viewModel::dismissBackupReminder,
    )
}
