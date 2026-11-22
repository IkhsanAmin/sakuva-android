package com.sakuva.app.data.io

import kotlinx.serialization.Serializable

@Serializable
data class SakuvaBackupPayload(
    val schemaVersion: Int = 1,
    val wallets: List<WalletBackupDto> = emptyList(),
    val categories: List<CategoryBackupDto> = emptyList(),
    val transactions: List<TransactionBackupDto> = emptyList(),
)

@Serializable
data class WalletBackupDto(
    val id: Long,
    val name: String,
    val type: String,
    val initialBalance: Long,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val createdAt: Long = 0,
)

@Serializable
data class CategoryBackupDto(
    val id: Long,
    val name: String,
    val type: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
)

@Serializable
data class TransactionBackupDto(
    val id: Long,
    val amount: Long,
    val type: String,
    val walletId: Long,
    val toWalletId: Long? = null,
    val categoryId: Long? = null,
    val dateEpochDay: Long,
    val note: String = "",
    val createdAt: Long = 0,
)
