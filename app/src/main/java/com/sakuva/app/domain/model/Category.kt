package com.sakuva.app.domain.model

enum class CategoryType {
    INCOME,
    EXPENSE,
}

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
)
