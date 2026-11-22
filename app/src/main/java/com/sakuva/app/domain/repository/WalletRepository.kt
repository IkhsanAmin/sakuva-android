package com.sakuva.app.domain.repository

import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.model.WalletBalance
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeActiveWallets(): Flow<List<Wallet>>
    fun observeAllWallets(): Flow<List<Wallet>>
    fun observeWalletBalances(): Flow<List<WalletBalance>>
    suspend fun getWallet(id: Long): Wallet?
    suspend fun getAllWallets(): List<Wallet>
    suspend fun saveWallet(wallet: Wallet): Long
    suspend fun archiveWallet(id: Long)
}
