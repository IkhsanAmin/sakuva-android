package com.sakuva.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import org.junit.Test

class GetWalletBalancesUseCaseTest {

    private fun createTransaction(
        id: Long,
        type: TransactionType,
        amount: Long,
        walletId: Long,
        toWalletId: Long? = null,
        categoryId: Long? = null,
    ) = Transaction(
        id = id,
        amount = amount,
        type = type,
        walletId = walletId,
        toWalletId = toWalletId,
        categoryId = categoryId,
        dateEpochDay = 19000L,
        note = "",
        createdAt = 1000L,
    )

    @Test
    fun balance_withNoTransactions_returnsInitialBalance() {
        val result = WalletBalanceCalculator.balance(
            initialBalance = 50000L,
            walletId = 1L,
            transactions = emptyList(),
        )
        assertThat(result).isEqualTo(50000L)
    }

    @Test
    fun balance_withIncome_addsToBalance() {
        val transactions = listOf(
            createTransaction(1L, TransactionType.INCOME, 10000L, 1L),
        )
        val result = WalletBalanceCalculator.balance(
            initialBalance = 50000L,
            walletId = 1L,
            transactions = transactions,
        )
        assertThat(result).isEqualTo(60000L)
    }

    @Test
    fun balance_withExpense_subtractsFromBalance() {
        val transactions = listOf(
            createTransaction(1L, TransactionType.EXPENSE, 10000L, 1L),
        )
        val result = WalletBalanceCalculator.balance(
            initialBalance = 50000L,
            walletId = 1L,
            transactions = transactions,
        )
        assertThat(result).isEqualTo(40000L)
    }

    @Test
    fun balance_withTransferOut_subtractsFromSource() {
        val transactions = listOf(
            createTransaction(1L, TransactionType.TRANSFER, 10000L, 1L, 2L),
        )
        val result = WalletBalanceCalculator.balance(
            initialBalance = 50000L,
            walletId = 1L,
            transactions = transactions,
        )
        assertThat(result).isEqualTo(40000L)
    }

    @Test
    fun balance_withTransferIn_addsToDestination() {
        val transactions = listOf(
            createTransaction(1L, TransactionType.TRANSFER, 10000L, 1L, 2L),
        )
        val result = WalletBalanceCalculator.balance(
            initialBalance = 50000L,
            walletId = 2L,
            transactions = transactions,
        )
        assertThat(result).isEqualTo(60000L)
    }

    @Test
    fun balance_withMultipleTransactions_aggregatesCorrectly() {
        val transactions = listOf(
            createTransaction(1L, TransactionType.INCOME, 20000L, 1L),
            createTransaction(2L, TransactionType.EXPENSE, 5000L, 1L),
            createTransaction(3L, TransactionType.TRANSFER, 10000L, 1L, 2L),
        )
        val result = WalletBalanceCalculator.balance(
            initialBalance = 100000L,
            walletId = 1L,
            transactions = transactions,
        )
        assertThat(result).isEqualTo(105000L) // 100000 + 20000 - 5000 - 10000
    }
}
