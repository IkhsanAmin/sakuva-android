package com.sakuva.app.domain.model

enum class WalletType {
    CASH,
    BANK,
    EWALLET,
}

data class Wallet(
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val initialBalance: Long,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val createdAt: Long,
)

data class WalletBalance(
    val wallet: Wallet,
    val balance: Long,
)
