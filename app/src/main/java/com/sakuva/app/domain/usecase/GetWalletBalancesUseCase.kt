package com.sakuva.app.domain.usecase

import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.WalletBalance
import com.sakuva.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWalletBalancesUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    operator fun invoke(): Flow<List<WalletBalance>> = walletRepository.observeWalletBalances()
}

/**
 * Pure balance model used to verify the SQL aggregation contract in unit tests.
 * Production balances always come from the Room query (single source of truth).
 */
object WalletBalanceCalculator {
    fun balance(
        initialBalance: Long,
        walletId: Long,
        transactions: List<Transaction>,
    ): Long {
        var total = initialBalance
        for (transaction in transactions) {
            when (transaction.type) {
                TransactionType.INCOME -> {
                    if (transaction.walletId == walletId) total += transaction.amount
                }

                TransactionType.EXPENSE -> {
                    if (transaction.walletId == walletId) total -= transaction.amount
                }

                TransactionType.TRANSFER -> {
                    if (transaction.walletId == walletId) total -= transaction.amount
                    if (transaction.toWalletId == walletId) total += transaction.amount
                }
            }
        }
        return total
    }
}
