package com.sakuva.app.data.repository

import com.sakuva.app.data.local.dao.WalletDao
import com.sakuva.app.data.mapper.toDomain
import com.sakuva.app.data.mapper.toEntity
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.model.WalletBalance
import com.sakuva.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
) : WalletRepository {

    override fun observeActiveWallets(): Flow<List<Wallet>> =
        walletDao.observeActiveWallets().map { wallets -> wallets.map { it.toDomain() } }

    override fun observeAllWallets(): Flow<List<Wallet>> =
        walletDao.observeAllWallets().map { wallets -> wallets.map { it.toDomain() } }

    override fun observeWalletBalances(): Flow<List<WalletBalance>> =
        walletDao.observeBalances().map { rows ->
            rows.map { row -> WalletBalance(wallet = row.wallet.toDomain(), balance = row.balance) }
        }

    override suspend fun getWallet(id: Long): Wallet? = walletDao.getById(id)?.toDomain()

    override suspend fun getAllWallets(): List<Wallet> =
        walletDao.getAll().map { it.toDomain() }

    override suspend fun saveWallet(wallet: Wallet): Long {
        val entity = wallet.toEntity().withCreatedAt()
        return if (entity.id == 0L) {
            walletDao.insert(entity)
        } else {
            walletDao.update(entity)
            entity.id
        }
    }

    override suspend fun archiveWallet(id: Long) = walletDao.archive(id)

    private fun com.sakuva.app.data.local.entity.WalletEntity.withCreatedAt() =
        if (createdAt == 0L) copy(createdAt = System.currentTimeMillis()) else this
}
