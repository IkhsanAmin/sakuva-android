package com.sakuva.app.domain.usecase

import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionFilter
import com.sakuva.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionRepository.observeFiltered(filter)
}

class GetRecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(limit: Int = 8): Flow<List<Transaction>> =
        transactionRepository.observeRecent(limit)
}
