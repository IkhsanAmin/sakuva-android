package com.sakuva.app.feature.transaction

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.AppStrings
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.feature.common.labelFor
import java.util.Locale

@Composable
fun TransactionTypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        TransactionType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(text = strings.labelFor(type)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionField(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(text = label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text = text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDateField(
    epochDay: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = DateTimeUtils.epochDayToUtcMillis(epochDay),
    )
    val locale = Locale.forLanguageTag(strings.language.tag)
    val formattedDate = DateTimeUtils.formatEpochDay(epochDay, "dd MMM yyyy", locale)

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(imageVector = Icons.Filled.DateRange, contentDescription = null)
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(text = "${strings.dateLabel}: $formattedDate")
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateTimeUtils.utcMillisToEpochDay(millis))
                        }
                        showDialog = false
                    },
                ) {
                    Text(text = strings.ok)
                }
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

@Composable
fun FormErrorText(
    error: TransactionFormError?,
    modifier: Modifier = Modifier,
) {
    if (error == null) return
    val strings = LocalStrings.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = strings.messageFor(error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

fun AppStrings.messageFor(error: TransactionFormError): String = when (error) {
    TransactionFormError.AMOUNT_INVALID -> amountInvalid
    TransactionFormError.WALLET_REQUIRED -> walletRequired
    TransactionFormError.CATEGORY_REQUIRED -> categoryRequired
    TransactionFormError.TARGET_WALLET_REQUIRED -> targetWalletRequired
    TransactionFormError.SAME_WALLET -> transferSameWallet
    TransactionFormError.NOTE_TOO_LONG -> noteTooLong
    TransactionFormError.GENERIC -> genericError
}
