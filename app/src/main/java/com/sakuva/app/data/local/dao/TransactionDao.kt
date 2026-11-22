package com.sakuva.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakuva.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseRow(
    val categoryId: Long,
    val name: String,
    val colorHex: String,
    val amount: Long,
)

data class ExpenseDayRow(
    val dateEpochDay: Long,
    val amount: Long,
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY dateEpochDay DESC, createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
          AND (:walletId IS NULL OR walletId = :walletId OR toWalletId = :walletId)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:typeFilter IS NULL OR type = :typeFilter)
          AND (:query = '' OR note LIKE '%' || :query || '%')
        ORDER BY dateEpochDay DESC, createdAt DESC
        """,
    )
    fun observeFiltered(
        walletId: Long?,
        categoryId: Long?,
        typeFilter: String?,
        startEpochDay: Long,
        endEpochDay: Long,
        query: String,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'INCOME' AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        """,
    )
    fun observeIncomeTotal(startEpochDay: Long, endEpochDay: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        """,
    )
    fun observeExpenseTotal(startEpochDay: Long, endEpochDay: Long): Flow<Long>

    @Query(
        """
        SELECT t.categoryId AS categoryId, c.name AS name, c.colorHex AS colorHex, SUM(t.amount) AS amount
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE'
          AND t.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
          AND c.isArchived = 0
        GROUP BY t.categoryId, c.name, c.colorHex
        ORDER BY amount DESC
        LIMIT :limit
        """,
    )
    fun observeExpenseByCategory(
        startEpochDay: Long,
        endEpochDay: Long,
        limit: Int,
    ): Flow<List<CategoryExpenseRow>>

    @Query(
        """
        SELECT dateEpochDay AS dateEpochDay, SUM(amount) AS amount
        FROM transactions
        WHERE type = 'EXPENSE'
          AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY dateEpochDay
        ORDER BY dateEpochDay ASC
        """,
    )
    fun observeExpenseByDay(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<ExpenseDayRow>>
}
