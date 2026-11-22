package com.sakuva.app.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}

data class Transaction(
    val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val walletId: Long,
    val toWalletId: Long? = null,
    val categoryId: Long? = null,
    val dateEpochDay: Long,
    val note: String = "",
    val createdAt: Long,
)
