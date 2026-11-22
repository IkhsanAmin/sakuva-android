package com.sakuva.app.domain.model

data class ExpensePoint(
    val dateEpochDay: Long,
    val amount: Long,
)

data class ExpenseTrendPoint(
    val label: String,
    val amount: Long,
    val startEpochDay: Long,
    val endEpochDay: Long,
)

data class CategoryExpense(
    val categoryId: Long?,
    val label: String,
    val colorHex: String,
    val amount: Long,
    val isOther: Boolean = false,
)
