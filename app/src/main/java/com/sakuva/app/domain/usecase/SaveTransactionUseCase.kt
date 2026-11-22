package com.sakuva.app.domain.usecase

import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.repository.TransactionRepository
import javax.inject.Inject

enum class TransactionValidationError {
    AMOUNT_INVALID,
    WALLET_REQUIRED,
    CATEGORY_REQUIRED,
    TARGET_WALLET_REQUIRED,
    SAME_WALLET,
    NOTE_TOO_LONG,
    INVALID_COMBINATION,
}

class TransactionValidationException(
    val error: TransactionValidationError,
) : IllegalArgumentException(error.name)

class SaveTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        val validationError = validate(transaction)
        if (validationError != null) {
            return Result.failure(TransactionValidationException(validationError))
        }
        return runCatching { transactionRepository.saveTransaction(transaction) }
    }

    companion object {
        const val MAX_NOTE_LENGTH = 200

        fun validate(transaction: Transaction): TransactionValidationError? {
            if (transaction.amount <= 0L) return TransactionValidationError.AMOUNT_INVALID
            if (transaction.walletId <= 0L) return TransactionValidationError.WALLET_REQUIRED
            if (transaction.note.length > MAX_NOTE_LENGTH) return TransactionValidationError.NOTE_TOO_LONG

            return when (transaction.type) {
                TransactionType.INCOME, TransactionType.EXPENSE -> validateCashFlow(transaction)
                TransactionType.TRANSFER -> validateTransfer(transaction)
            }
        }

        private fun validateCashFlow(transaction: Transaction): TransactionValidationError? {
            if (transaction.categoryId == null) return TransactionValidationError.CATEGORY_REQUIRED
            if (transaction.toWalletId != null) return TransactionValidationError.INVALID_COMBINATION
            return null
        }

        private fun validateTransfer(transaction: Transaction): TransactionValidationError? {
            val targetWalletId = transaction.toWalletId
                ?: return TransactionValidationError.TARGET_WALLET_REQUIRED
            if (targetWalletId == transaction.walletId) return TransactionValidationError.SAME_WALLET
            if (transaction.categoryId != null) return TransactionValidationError.INVALID_COMBINATION
            return null
        }
    }
}
