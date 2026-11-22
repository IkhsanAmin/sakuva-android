package com.sakuva.app.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.DoubleBezelCard
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.CategoryLocalizer
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.DestructiveConfirmDialog
import com.sakuva.app.feature.common.EmptyState
import com.sakuva.app.feature.common.LoadingState
import com.sakuva.app.feature.common.TransactionRow
import com.sakuva.app.feature.common.labelFor
import com.sakuva.app.feature.common.resolveTransactionDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onQueryChange: (String) -> Unit,
    onWalletSelected: (Long?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onTypeSelected: (TransactionType?) -> Unit,
    onMonthSelected: (Long) -> Unit,
    onShiftMonth: (Int) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onClearCustomRange: () -> Unit,
    onEdit: (Long) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val strings = LocalStrings.current
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    ContentWidthContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Text(
                    text = strings.history,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            item {
                MonthYearPicker(
                    monthStartEpochDay = uiState.filters.monthStartEpochDay,
                    onMonthSelected = onMonthSelected,
                    onShiftMonth = onShiftMonth,
                )
            }
            item {
                HistorySearchField(
                    query = uiState.filters.query,
                    onQueryChange = onQueryChange,
                )
            }
            item {
                val walletOptions = buildList {
                    add(FilterOption<Long>(value = null, label = strings.allWallets))
                    uiState.wallets.forEach { wallet ->
                        add(FilterOption(value = wallet.id, label = wallet.name))
                    }
                }
                val categoryOptions = buildList {
                    add(FilterOption<Long>(value = null, label = strings.allCategories))
                    uiState.categories.forEach { category ->
                        add(
                            FilterOption(
                                value = category.id,
                                label = CategoryLocalizer.localize(category.name, strings),
                            ),
                        )
                    }
                }
                val typeOptions = buildList {
                    add(FilterOption<TransactionType>(value = null, label = strings.allTypes))
                    TransactionType.entries.forEach { type ->
                        add(FilterOption(value = type, label = strings.labelFor(type)))
                    }
                }
                HistoryFilterRow(
                    filters = uiState.filters,
                    wallets = walletOptions,
                    categories = categoryOptions,
                    types = typeOptions,
                    onWalletSelected = onWalletSelected,
                    onCategorySelected = onCategorySelected,
                    onTypeSelected = onTypeSelected,
                )
            }
            if (uiState.filters.customRange != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Rentang: ${DateTimeUtils.formatEpochDay(uiState.filters.customRange!!.startEpochDay)} - ${DateTimeUtils.formatEpochDay(uiState.filters.customRange.endEpochDay)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onClearCustomRange) {
                            Text(text = strings.clearFilters)
                        }
                    }
                }
            }
            if (uiState.filters.hasActiveFilters && uiState.filters.customRange == null) {
                item {
                    TextButton(onClick = onClearCustomRange) {
                        Text(text = strings.clearFilters)
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingState(modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    EmptyState(title = strings.noTransactionsFound)
                }
            } else {
                val grouped = uiState.transactions.groupBy { it.dateEpochDay }
                grouped.forEach { (date, transactions) ->
                    item {
                        Text(
                            text = DateTimeUtils.formatEpochDay(date, "EEEE, d MMMM yyyy"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        DoubleBezelCard(modifier = Modifier.fillMaxWidth()) {
                            transactions.forEach { transaction ->
                                TransactionRow(
                                    transaction = transaction,
                                    walletNames = uiState.walletNames,
                                    categoryNames = uiState.categoryNames,
                                    currencyCode = uiState.currencyCode,
                                    onClick = { selectedTransaction = transaction },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    if (selectedTransaction != null) {
        TransactionDetailSheet(
            transaction = selectedTransaction!!,
            walletNames = uiState.walletNames,
            categoryNames = uiState.categoryNames,
            currencyCode = uiState.currencyCode,
            onDismiss = { selectedTransaction = null },
            onEdit = { onEdit(selectedTransaction!!.id); selectedTransaction = null },
            onDuplicate = { onDuplicate(selectedTransaction!!.id); selectedTransaction = null },
            onDelete = { showDeleteConfirm = selectedTransaction!!.id; selectedTransaction = null },
        )
    }

    if (showRangePicker) {
        HistoryDateRangePicker(
            onRangeSelected = { start, end -> onCustomRangeSelected(start, end); showRangePicker = false },
            onDismiss = { showRangePicker = false },
        )
    }

    if (showDeleteConfirm != null) {
        DestructiveConfirmDialog(
            title = "Hapus transaksi?",
            message = "Transaksi ini akan dihapus secara permanen.",
            confirmLabel = strings.delete,
            onConfirm = { onDelete(showDeleteConfirm!!); showDeleteConfirm = null },
            onDismiss = { showDeleteConfirm = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    walletNames: Map<Long, String>,
    categoryNames: Map<Long, String>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalStrings.current
    val display = resolveTransactionDisplay(transaction, walletNames, categoryNames, strings)
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
        ) {
            ListItem(
                headlineContent = {
                    Text(text = display.title, style = MaterialTheme.typography.titleLarge)
                },
                supportingContent = {
                    if (display.subtitle.isNotBlank()) {
                        Text(text = display.subtitle)
                    }
                },
                trailingContent = {
                    Text(
                        text = MoneyFormatter.format(transaction.amount, currencyCode),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
            HorizontalDivider()
            Text(
                text = "Tanggal: ${DateTimeUtils.formatEpochDay(transaction.dateEpochDay)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(onClick = onDuplicate, modifier = Modifier.weight(1f)) {
                    Text(text = "Catat Lagi")
                }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text(text = strings.edit)
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(text = strings.delete)
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}
