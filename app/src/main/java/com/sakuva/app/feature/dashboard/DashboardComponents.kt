package com.sakuva.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.DoubleBezelCard
import com.sakuva.app.core.designsystem.SakuvaTheme
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.designsystem.colorFromHex
import com.sakuva.app.core.locale.CategoryLocalizer
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.CategoryExpense
import com.sakuva.app.domain.model.DashboardSummary
import com.sakuva.app.domain.model.ExpenseTrendPoint
import com.sakuva.app.feature.common.labelFor

@Composable
fun BalanceSummaryCard(
    summary: DashboardSummary,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val extraColors = SakuvaTheme.extraColors

    DoubleBezelCard(modifier = modifier) {
        Text(
            text = strings.totalBalance,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = MoneyFormatter.format(summary.totalBalance, currencyCode),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            AmountStat(
                label = strings.totalIncome,
                amount = summary.totalIncome,
                currencyCode = currencyCode,
                color = extraColors.income,
                modifier = Modifier.weight(1f),
            )
            AmountStat(
                label = strings.totalExpense,
                amount = summary.totalExpense,
                currencyCode = currencyCode,
                color = extraColors.expense,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AmountStat(
    label: String,
    amount: Long,
    currencyCode: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = MoneyFormatter.format(amount, currencyCode),
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
    }
}

@Composable
fun BackupReminderBanner(
    onBackupNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = strings.backupReminderTitle,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.close,
                        )
                    }
                }
                Text(
                    text = strings.backupReminderMessage,
                    style = MaterialTheme.typography.bodySmall,
                )
                FilledTonalButton(onClick = onBackupNow) {
                    Text(text = strings.backupReminderAction)
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        PeriodType.entries.forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(text = strings.labelFor(period)) },
            )
        }
    }
}

@Composable
fun ExpenseTrendCard(
    points: List<ExpenseTrendPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    DoubleBezelCard(modifier = modifier) {
        Text(
            text = strings.expenseTrend,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        if (points.isEmpty() || points.all { it.amount == 0L }) {
            Text(
                text = strings.noChartData,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ExpenseLineChart(points = points, currencyCode = currencyCode)
        }
    }
}

@Composable
fun TopCategoriesCard(
    categories: List<CategoryExpense>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val total = categories.sumOf { it.amount }
    DoubleBezelCard(modifier = modifier) {
        Text(
            text = strings.topCategories,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        if (categories.isEmpty() || total == 0L) {
            Text(
                text = strings.noExpenseData,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            categories.forEach { item ->
                val color = colorFromHex(item.colorHex)
                val fraction = (item.amount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = color, shape = CircleShape),
                        )
                        Text(
                            text = if (item.isOther) {
                                strings.othersCategory
                            } else {
                                CategoryLocalizer.localize(item.label, strings)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = MoneyFormatter.format(item.amount, currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}
