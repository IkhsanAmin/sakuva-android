package com.sakuva.app.core.time

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeUtils {

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun rangeFor(period: PeriodType, anchor: LocalDate = LocalDate.now()): PeriodRange =
        when (period) {
            PeriodType.DAILY -> {
                val day = anchor.toEpochDay()
                PeriodRange(day, day)
            }

            PeriodType.WEEKLY -> {
                val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                PeriodRange(monday.toEpochDay(), monday.plusDays(6).toEpochDay())
            }

            PeriodType.MONTHLY -> {
                val firstDay = anchor.withDayOfMonth(1)
                val lastDay = anchor.with(TemporalAdjusters.lastDayOfMonth())
                PeriodRange(firstDay.toEpochDay(), lastDay.toEpochDay())
            }

            PeriodType.YEARLY -> {
                val firstDay = anchor.withDayOfYear(1)
                val lastDay = anchor.with(TemporalAdjusters.lastDayOfYear())
                PeriodRange(firstDay.toEpochDay(), lastDay.toEpochDay())
            }
        }

    fun rangeForPreset(preset: DateRangePreset, today: LocalDate = LocalDate.now()): PeriodRange =
        when (preset) {
            DateRangePreset.THIS_MONTH -> rangeFor(PeriodType.MONTHLY, today)
            DateRangePreset.LAST_30_DAYS -> PeriodRange(today.minusDays(29).toEpochDay(), today.toEpochDay())
            DateRangePreset.THIS_YEAR -> rangeFor(PeriodType.YEARLY, today)
            DateRangePreset.ALL_TIME -> PeriodRange.allTime()
        }

    fun monthStartEpochDay(anchor: LocalDate = LocalDate.now()): Long =
        anchor.withDayOfMonth(1).toEpochDay()

    fun monthRange(startEpochDay: Long): PeriodRange {
        val start = toLocalDate(startEpochDay).withDayOfMonth(1)
        return PeriodRange(
            startEpochDay = start.toEpochDay(),
            endEpochDay = start.with(TemporalAdjusters.lastDayOfMonth()).toEpochDay(),
        )
    }

    fun shiftMonth(startEpochDay: Long, delta: Int): Long =
        toLocalDate(startEpochDay).plusMonths(delta.toLong()).withDayOfMonth(1).toEpochDay()

    fun formatEpochDay(
        epochDay: Long,
        pattern: String = "dd MMM yyyy",
        locale: Locale = Locale.forLanguageTag("id-ID"),
    ): String = toLocalDate(epochDay).format(DateTimeFormatter.ofPattern(pattern, locale))

    fun formatEpochMillis(
        epochMillis: Long,
        pattern: String = "dd MMM yyyy HH:mm",
        locale: Locale = Locale.forLanguageTag("id-ID"),
    ): String = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(pattern, locale))

    /** Material3 DatePicker exposes UTC midnight millis; convert without zone drift. */
    fun utcMillisToEpochDay(utcMillis: Long): Long =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

    fun epochDayToUtcMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

enum class DateRangePreset {
    THIS_MONTH,
    LAST_30_DAYS,
    THIS_YEAR,
    ALL_TIME,
}
