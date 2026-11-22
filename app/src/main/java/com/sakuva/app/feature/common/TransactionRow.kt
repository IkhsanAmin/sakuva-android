package com.sakuva.app.feature.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.sakuva.app.core.designsystem.SakuvaTheme
import com.sakuva.app.core.locale.AppStrings
import com.sakuva.app.core.locale.CategoryLocalizer
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType

data class TransactionDisplay(
    val title: String,
    val subtitle: String,
)

fun resolveTransactionDisplay(
    transaction: Transaction,
    walletNames: Map<Long, String>,
    categoryNames: Map<Long, String>,
    strings: AppStrings,
): TransactionDisplay {
    val title = when (transaction.type) {
        TransactionType.INCOME, TransactionType.EXPENSE ->
            transaction.categoryId?.let { categoryNames[it] }
                ?.let { CategoryLocalizer.localize(it, strings) }
                ?: strings.labelFor(transaction.type)

        TransactionType.TRANSFER -> strings.transferLabel
    }

    val walletPart = if (transaction.type == TransactionType.TRANSFER) {
        val source = walletNames[transaction.walletId].orEmpty()
        val target = transaction.toWalletId?.let { walletNames[it] }.orEmpty()
        "$source → $target"
    } else {
        walletNames[transaction.walletId].orEmpty()
    }

    val subtitle = listOfNotNull(
        walletPart.takeIf { it.isNotBlank() },
        transaction.note.takeIf { it.isNotBlank() },
    ).joinToString(separator = " · ")

    return TransactionDisplay(title = title, subtitle = subtitle)
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    walletNames: Map<Long, String>,
    categoryNames: Map<Long, String>,
    currencyCode: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val strings = LocalStrings.current
    val display = resolveTransactionDisplay(transaction, walletNames, categoryNames, strings)
    val extraColors = SakuvaTheme.extraColors

    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> extraColors.income
        TransactionType.EXPENSE -> extraColors.expense
        TransactionType.TRANSFER -> extraColors.transfer
    }
    val sign = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "−"
        TransactionType.TRANSFER -> ""
    }
    val amountText = sign + MoneyFormatter.format(transaction.amount, currencyCode)

    ListItem(
        headlineContent = {
            Text(
                text = display.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            if (display.subtitle.isNotBlank()) {
                Text(
                    text = display.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingContent = {
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleSmall,
                color = amountColor,
            )
        },
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {},
    )
}
