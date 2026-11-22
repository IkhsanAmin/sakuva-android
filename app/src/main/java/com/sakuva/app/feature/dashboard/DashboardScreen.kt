package com.sakuva.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.EmptyState
import com.sakuva.app.feature.common.LoadingState
import com.sakuva.app.feature.common.SectionHeader
import com.sakuva.app.feature.common.TransactionRow

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    onAddTransaction: () -> Unit,
    onSeeAllHistory: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onBackupNow: () -> Unit,
    onDismissBackup: () -> Unit,
) {
    val strings = LocalStrings.current

    ContentWidthContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            item {
                Text(
                    text = strings.dashboard,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        LoadingState()
                    }
                }
            } else {
                if (uiState.showBackupReminder) {
                    item {
                        BackupReminderBanner(
                            onBackupNow = onBackupNow,
                            onDismiss = onDismissBackup,
                        )
                    }
                }
                item {
                    BalanceSummaryCard(
                        summary = uiState.summary,
                        currencyCode = uiState.currencyCode,
                    )
                }
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = onPeriodSelected,
                    )
                }
                item {
                    ExpenseTrendCard(
                        points = uiState.expenseTrend,
                        currencyCode = uiState.currencyCode,
                    )
                }
                item {
                    TopCategoriesCard(
                        categories = uiState.categoryExpenses,
                        currencyCode = uiState.currencyCode,
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionHeader(
                            text = strings.recentTransactions,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onSeeAllHistory) {
                            Text(text = strings.seeAll)
                        }
                    }
                }

                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = strings.noRecentTransactions,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(uiState.recentTransactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            walletNames = uiState.walletNames,
                            categoryNames = uiState.categoryNames,
                            currencyCode = uiState.currencyCode,
                            onClick = { onTransactionClick(transaction.id) },
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}
