package com.sakuva.app.domain.model

data class RadarChartSlice(
    val categoryId: Long?,
    val label: String,
    val colorHex: String,
    val amount: Long,
)

data class DashboardSummary(
    val totalBalance: Long,
    val totalIncome: Long,
    val totalExpense: Long,
)
