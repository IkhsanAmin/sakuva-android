package com.sakuva.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sakuva.app.domain.model.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val initialBalance: Long,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val createdAt: Long,
)
