package com.sakuva.app.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.CategoryLocalizer
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.util.CurrencyVisualTransformation
import com.sakuva.app.core.util.isZeroDecimalCurrency
import com.sakuva.app.core.util.sanitizeAmountInput
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.usecase.SaveTransactionUseCase
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.LoadingState

@Composable
fun TransactionEditorScreen(
    uiState: TransactionEditorUiState,
    onBack: () -> Unit,
    onTypeSelected: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onWalletSelected: (Long) -> Unit,
    onTargetWalletSelected: (Long) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onManageCategories: (() -> Unit)? = null,
) {
    val strings = LocalStrings.current
    val form = uiState.form

    val categoryType = when (form.type) {
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.TRANSFER -> null
    }
    val categoryOptions = categoryType
        ?.let { type -> uiState.categories.filter { it.type == type } }
        ?.map { it.id to CategoryLocalizer.localize(it.name, strings) }
        .orEmpty()
    val walletOptions = uiState.wallets.map { it.id to it.name }
    val targetWalletOptions = uiState.wallets
        .filter { it.id != form.walletId }
        .map { it.id to it.name }

    ContentWidthContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backDescription,
                        )
                    }
                    Text(
                        text = if (uiState.isEditing) strings.editTransaction else strings.newTransaction,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingState(modifier = Modifier.fillMaxWidth().height(240.dp))
                }
            } else {
                item {
                    TransactionTypeSelector(
                        selected = form.type,
                        onSelect = onTypeSelected,
                    )
                }
                item {
                    val currencyCode = uiState.currencyCode
                    val visualTransformation = remember(currencyCode) {
                        CurrencyVisualTransformation(currencyCode)
                    }
                    OutlinedTextField(
                        value = form.amountText,
                        onValueChange = { onAmountChange(sanitizeAmountInput(it, currencyCode)) },
                        label = { Text(text = strings.amountLabel) },
                        prefix = { Text(text = uiState.currencyCode) },
                        singleLine = true,
                        isError = uiState.error == TransactionFormError.AMOUNT_INVALID,
                        visualTransformation = visualTransformation,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isZeroDecimalCurrency(currencyCode)) {
                                KeyboardType.Number
                            } else {
                                KeyboardType.Decimal
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    SelectionField(
                        label = strings.sourceWalletLabel,
                        options = walletOptions,
                        selected = form.walletId,
                        onSelect = onWalletSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (form.type == TransactionType.TRANSFER) {
                    item {
                        SelectionField(
                            label = strings.targetWalletLabel,
                            options = targetWalletOptions,
                            selected = form.toWalletId,
                            onSelect = onTargetWalletSelected,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    item {
                        SelectionField(
                            label = strings.categoryLabel,
                            options = categoryOptions,
                            selected = form.categoryId,
                            onSelect = onCategorySelected,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (onManageCategories != null) {
                            TextButton(onClick = onManageCategories) {
                                Text(text = "+ ${strings.manageCategories}")
                            }
                        }
                    }
                }
                item {
                    TransactionDateField(
                        epochDay = form.dateEpochDay,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = onNoteChange,
                        label = { Text(text = strings.noteLabel) },
                        placeholder = { Text(text = strings.notePlaceholder) },
                        minLines = 3,
                        maxLines = 5,
                        supportingText = {
                            Text(
                                text = "${form.note.length}/${SaveTransactionUseCase.MAX_NOTE_LENGTH}",
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    FormErrorText(error = uiState.error)
                }
                item {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(text = strings.saveTransaction)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}
