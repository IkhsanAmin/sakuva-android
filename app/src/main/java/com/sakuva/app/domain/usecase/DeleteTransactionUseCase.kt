package com.sakuva.app.domain.usecase

import com.sakuva.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long) {
        transactionRepository.deleteTransaction(id)
    }
}
