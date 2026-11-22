package com.sakuva.app.data.mapper

import com.sakuva.app.data.local.entity.CategoryEntity
import com.sakuva.app.data.local.entity.TransactionEntity
import com.sakuva.app.data.local.entity.WalletEntity
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.Wallet

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    iconKey = iconKey,
    colorHex = colorHex,
    isArchived = isArchived,
    createdAt = createdAt,
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    iconKey = iconKey,
    colorHex = colorHex,
    isArchived = isArchived,
    createdAt = createdAt,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    type = type,
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    isArchived = isArchived,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type,
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    isArchived = isArchived,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = type,
    walletId = walletId,
    toWalletId = toWalletId,
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type,
    walletId = walletId,
    toWalletId = toWalletId,
    categoryId = categoryId,
    dateEpochDay = dateEpochDay,
    note = note,
    createdAt = createdAt,
)
