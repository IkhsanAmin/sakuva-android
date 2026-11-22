package com.sakuva.app.data.repository

import com.sakuva.app.core.time.PeriodRange
import com.sakuva.app.data.local.dao.TransactionDao
import com.sakuva.app.data.mapper.toDomain
import com.sakuva.app.data.mapper.toEntity
import com.sakuva.app.domain.model.ExpensePoint
import com.sakuva.app.domain.model.RadarChartSlice
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionFilter
import com.sakuva.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { transactions -> transactions.map { it.toDomain() } }

    override fun observeFiltered(filter: TransactionFilter): Flow<List<Transaction>> =
        transactionDao.observeFiltered(
            walletId = filter.walletId,
            categoryId = filter.categoryId,
            typeFilter = filter.type?.name,
            startEpochDay = filter.range.startEpochDay,
            endEpochDay = filter.range.endEpochDay,
            query = filter.query,
        ).map { transactions -> transactions.map { it.toDomain() } }

    override fun observeIncomeTotal(range: PeriodRange): Flow<Long> =
        transactionDao.observeIncomeTotal(range.startEpochDay, range.endEpochDay)

    override fun observeExpenseTotal(range: PeriodRange): Flow<Long> =
        transactionDao.observeExpenseTotal(range.startEpochDay, range.endEpochDay)

    override fun observeExpenseByCategory(
        range: PeriodRange,
        limit: Int,
    ): Flow<List<RadarChartSlice>> =
        transactionDao.observeExpenseByCategory(range.startEpochDay, range.endEpochDay, limit)
            .map { rows ->
                rows.map { row ->
                    RadarChartSlice(
                        categoryId = row.categoryId,
                        label = row.name,
                        colorHex = row.colorHex,
                        amount = row.amount,
                    )
                }
            }

    override fun observeExpenseByDay(range: PeriodRange): Flow<List<ExpensePoint>> =
        transactionDao.observeExpenseByDay(range.startEpochDay, range.endEpochDay)
            .map { rows -> rows.map { ExpensePoint(it.dateEpochDay, it.amount) } }

    override suspend fun getTransaction(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun getAllTransactions(): List<Transaction> =
        transactionDao.getAll().map { it.toDomain() }

    override suspend fun saveTransaction(transaction: Transaction): Long {
        val entity = transaction.toEntity().let {
            if (it.createdAt == 0L) it.copy(createdAt = System.currentTimeMillis()) else it
        }
        return if (entity.id == 0L) {
            transactionDao.insert(entity)
        } else {
            transactionDao.update(entity)
            entity.id
        }
    }

    override suspend fun deleteTransaction(id: Long) = transactionDao.deleteById(id)
}
