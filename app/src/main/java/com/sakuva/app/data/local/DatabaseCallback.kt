package com.sakuva.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeds default wallets and categories synchronously inside Room's onCreate
 * transaction so the first UI frame never observes an empty database.
 */
class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        seedWallets(db, now)
        seedCategories(db)
    }

    private fun seedWallets(db: SupportSQLiteDatabase, now: Long) {
        DefaultSeeds.wallets.forEachIndexed { index, wallet ->
            db.execSQL(
                """
                INSERT INTO wallets
                    (name, type, initialBalance, iconKey, colorHex, isArchived, createdAt)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    wallet.name,
                    wallet.type,
                    0L,
                    wallet.iconKey,
                    wallet.colorHex,
                    0,
                    now + index,
                ),
            )
        }
    }

    private fun seedCategories(db: SupportSQLiteDatabase) {
        DefaultSeeds.categories.forEach { category ->
            db.execSQL(
                """
                INSERT INTO categories
                    (name, type, iconKey, colorHex, isDefault, isArchived)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    category.name,
                    category.type,
                    category.iconKey,
                    category.colorHex,
                    1,
                    0,
                ),
            )
        }
    }
}
