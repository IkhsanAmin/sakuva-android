package com.sakuva.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sakuva.app.data.local.dao.CategoryDao
import com.sakuva.app.data.local.dao.TransactionDao
import com.sakuva.app.data.local.dao.WalletDao
import com.sakuva.app.data.local.entity.CategoryEntity
import com.sakuva.app.data.local.entity.TransactionEntity
import com.sakuva.app.data.local.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SakuvaDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "sakuva.db"
    }
}
