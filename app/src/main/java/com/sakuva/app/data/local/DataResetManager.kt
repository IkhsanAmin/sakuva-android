package com.sakuva.app.data.local

import com.sakuva.app.data.local.entity.CategoryEntity
import com.sakuva.app.data.local.entity.WalletEntity
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.WalletType
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permanently clears every transaction, wallet, and category, then re-seeds the
 * mandatory default wallets and categories inside a single atomic transaction.
 */
@Singleton
class DataResetManager @Inject constructor(
    private val database: SakuvaDatabase,
) {

    suspend fun resetAllData() {
        database.withTransaction {
            database.transactionDao().deleteAll()
            database.walletDao().deleteAll()
            database.categoryDao().deleteAll()

            val now = System.currentTimeMillis()
            database.walletDao().insertAll(
                DefaultSeeds.wallets.mapIndexed { index, seed ->
                    WalletEntity(
                        name = seed.name,
                        type = WalletType.valueOf(seed.type),
                        initialBalance = 0L,
                        iconKey = seed.iconKey,
                        colorHex = seed.colorHex,
                        isArchived = false,
                        createdAt = now + index,
                    )
                },
            )
            database.categoryDao().insertAll(
                DefaultSeeds.categories.map { seed ->
                    CategoryEntity(
                        name = seed.name,
                        type = CategoryType.valueOf(seed.type),
                        iconKey = seed.iconKey,
                        colorHex = seed.colorHex,
                        isDefault = true,
                        isArchived = false,
                    )
                },
            )
        }
    }
}
