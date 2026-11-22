package com.sakuva.app.core.time

enum class PeriodType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

data class PeriodRange(
    val startEpochDay: Long,
    val endEpochDay: Long,
) {
    fun contains(epochDay: Long): Boolean = epochDay in startEpochDay..endEpochDay

    companion object {
        fun allTime(): PeriodRange = PeriodRange(Long.MIN_VALUE, Long.MAX_VALUE)
    }
}
