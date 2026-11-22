package com.sakuva.app.feature.wallet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.core.util.CurrencyVisualTransformation
import com.sakuva.app.core.util.isZeroDecimalCurrency
import com.sakuva.app.core.util.sanitizeAmountInput
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.model.WalletType
import com.sakuva.app.feature.common.DestructiveConfirmDialog
import com.sakuva.app.feature.common.labelFor

@Composable
fun WalletEditorDialog(
    wallet: Wallet?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (name: String, type: WalletType, initialBalance: Long) -> Unit,
) {
    val strings = LocalStrings.current
    var name by remember(wallet?.id) { mutableStateOf(wallet?.name.orEmpty()) }
    var type by remember(wallet?.id) { mutableStateOf(wallet?.type ?: WalletType.CASH) }
    var balanceText by remember(wallet?.id) {
        mutableStateOf(
            wallet?.let { MoneyFormatter.toEditableString(it.initialBalance, currencyCode) }.orEmpty(),
        )
    }
    var error by remember(wallet?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (wallet == null) strings.addWallet else strings.editWallet)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text(text = strings.walletNameLabel) },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = strings.walletTypeLabel,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    WalletType.entries.forEach { walletType ->
                        FilterChip(
                            selected = type == walletType,
                            onClick = { type = walletType },
                            label = { Text(text = strings.labelFor(walletType)) },
                        )
                    }
                }
                val visualTransformation = remember(currencyCode) {
                    CurrencyVisualTransformation(currencyCode)
                }
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        balanceText = sanitizeAmountInput(input, currencyCode)
                        error = null
                    },
                    label = { Text(text = strings.initialBalanceLabel) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(text = message) } },
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isZeroDecimalCurrency(currencyCode)) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Decimal
                        },
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        error = strings.walletNameRequired
                        return@TextButton
                    }
                    val initialBalance = if (balanceText.isBlank()) {
                        0L
                    } else {
                        MoneyFormatter.parse(balanceText, currencyCode)
                    }
                    if (initialBalance == null) {
                        error = strings.amountInvalid
                        return@TextButton
                    }
                    onSave(trimmedName, type, initialBalance)
                },
            ) {
                Text(text = strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel)
            }
        },
    )
}

@Composable
fun ArchiveWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val strings = LocalStrings.current
    DestructiveConfirmDialog(
        title = strings.archiveWalletTitle,
        message = strings.archiveWalletMessage,
        confirmLabel = strings.archiveWallet,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
