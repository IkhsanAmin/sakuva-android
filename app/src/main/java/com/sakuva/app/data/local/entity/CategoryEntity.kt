package com.sakuva.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sakuva.app.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
)
