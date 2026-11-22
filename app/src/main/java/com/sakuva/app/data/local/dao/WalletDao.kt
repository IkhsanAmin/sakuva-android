package com.sakuva.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakuva.app.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

data class WalletWithBalanceRow(
    @Embedded val wallet: WalletEntity,
    val balance: Long,
)

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActiveWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY isArchived ASC, createdAt ASC")
    fun observeAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets ORDER BY isArchived ASC, createdAt ASC")
    suspend fun getAll(): List<WalletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallets: List<WalletEntity>)

    @Update
    suspend fun update(wallet: WalletEntity)

    @Query("UPDATE wallets SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()

    @Query(
        """
        SELECT w.*,
            w.initialBalance
            + COALESCE(SUM(CASE WHEN t.type = 'INCOME' AND t.walletId = w.id THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' AND t.walletId = w.id THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'TRANSFER' AND t.walletId = w.id THEN t.amount ELSE 0 END), 0)
            + COALESCE(SUM(CASE WHEN t.type = 'TRANSFER' AND t.toWalletId = w.id THEN t.amount ELSE 0 END), 0)
            AS balance
        FROM wallets w
        LEFT JOIN transactions t ON t.walletId = w.id OR t.toWalletId = w.id
        WHERE w.isArchived = 0
        GROUP BY w.id
        ORDER BY w.createdAt ASC
        """,
    )
    fun observeBalances(): Flow<List<WalletWithBalanceRow>>
}
