package com.sakuva.app.feature.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.feature.common.labelFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPicker(
    monthStartEpochDay: Long,
    onMonthSelected: (Long) -> Unit,
    onShiftMonth: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var showDialog by remember { mutableStateOf(false) }
    val monthLabel = DateTimeUtils.formatEpochDay(monthStartEpochDay, "MMMM yyyy")

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        IconButton(onClick = { onShiftMonth(-1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
            )
        }
        TextButton(onClick = { showDialog = true }) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = { onShiftMonth(1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.epochDayToUtcMillis(monthStartEpochDay),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onMonthSelected(DateTimeUtils.utcMillisToEpochDay(it))
                        }
                        showDialog = false
                    },
                ) { Text(text = strings.ok) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = strings.cancel)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDateRangePicker(
    onRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val state = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    val start = state.selectedStartDateMillis!!
                    val end = state.selectedEndDateMillis!!
                    onRangeSelected(
                        DateTimeUtils.utcMillisToEpochDay(start),
                        DateTimeUtils.utcMillisToEpochDay(end),
                    )
                },
            ) { Text(text = strings.ok) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel)
            }
        },
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.padding(bottom = 16.dp),
            title = {
                Text(
                    text = strings.chooseDate,
                    modifier = Modifier.padding(Spacing.lg),
                )
            },
        )
    }
}

data class FilterOption<T>(
    val value: T?,
    val label: String,
)

@Composable
fun HistorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(text = strings.searchTransactions) },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = strings.clearFilters,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
fun <T> FilterDropdownChip(
    label: String,
    options: List<FilterOption<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selected }?.label ?: label

    Box(modifier = modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(text = selectedLabel) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.label) },
                    onClick = {
                        onSelect(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun HistoryFilterRow(
    filters: HistoryFilters,
    wallets: List<FilterOption<Long>>,
    categories: List<FilterOption<Long>>,
    types: List<FilterOption<com.sakuva.app.domain.model.TransactionType>>,
    onWalletSelected: (Long?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onTypeSelected: (com.sakuva.app.domain.model.TransactionType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        FilterDropdownChip(
            label = strings.allWallets,
            options = wallets,
            selected = filters.walletId,
            onSelect = onWalletSelected,
        )
        FilterDropdownChip(
            label = strings.allCategories,
            options = categories,
            selected = filters.categoryId,
            onSelect = onCategorySelected,
        )
        FilterDropdownChip(
            label = strings.allTypes,
            options = types,
            selected = filters.type,
            onSelect = onTypeSelected,
        )
    }
}
