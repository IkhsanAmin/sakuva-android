package com.sakuva.app.domain.repository

import com.sakuva.app.core.time.PeriodRange
import com.sakuva.app.domain.model.ExpensePoint
import com.sakuva.app.domain.model.RadarChartSlice
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeFiltered(filter: TransactionFilter): Flow<List<Transaction>>
    fun observeIncomeTotal(range: PeriodRange): Flow<Long>
    fun observeExpenseTotal(range: PeriodRange): Flow<Long>
    fun observeExpenseByCategory(range: PeriodRange, limit: Int): Flow<List<RadarChartSlice>>
    fun observeExpenseByDay(range: PeriodRange): Flow<List<ExpensePoint>>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun getAllTransactions(): List<Transaction>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long)
}
