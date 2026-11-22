package com.sakuva.app.domain.usecase

import com.sakuva.app.core.time.PeriodRange
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.ExpensePoint
import com.sakuva.app.domain.model.ExpenseTrendPoint
import com.sakuva.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetExpenseTrendUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(
        period: PeriodType,
        today: LocalDate = LocalDate.now(),
    ): Flow<List<ExpenseTrendPoint>> {
        val range = ExpenseTrendCalculator.windowFor(period, today)
        return transactionRepository.observeExpenseByDay(range)
            .map { points -> ExpenseTrendCalculator.build(points, period, today) }
    }
}

object ExpenseTrendCalculator {

    fun windowFor(period: PeriodType, today: LocalDate): PeriodRange = when (period) {
        PeriodType.DAILY -> PeriodRange(today.minusDays(6).toEpochDay(), today.toEpochDay())
        PeriodType.WEEKLY -> {
            val lastWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val firstWeek = lastWeek.minusWeeks(7)
            PeriodRange(firstWeek.toEpochDay(), lastWeek.plusDays(6).toEpochDay())
        }

        PeriodType.MONTHLY -> {
            val lastMonth = today.withDayOfMonth(1)
            val firstMonth = lastMonth.minusMonths(11)
            PeriodRange(
                firstMonth.toEpochDay(),
                today.with(TemporalAdjusters.lastDayOfMonth()).toEpochDay(),
            )
        }

        PeriodType.YEARLY -> {
            val firstYear = today.withDayOfYear(1).minusYears(4)
            val lastYear = today.with(TemporalAdjusters.lastDayOfYear())
            PeriodRange(firstYear.toEpochDay(), lastYear.toEpochDay())
        }
    }

    fun build(
        points: List<ExpensePoint>,
        period: PeriodType,
        today: LocalDate,
    ): List<ExpenseTrendPoint> = when (period) {
        PeriodType.DAILY -> {
            val end = today
            (0 until 7).map { offset ->
                val day = end.minusDays((6 - offset).toLong())
                val epochDay = day.toEpochDay()
                ExpenseTrendPoint(
                    label = "${day.dayOfMonth}/${day.monthValue}",
                    amount = points.filter { it.dateEpochDay == epochDay }.sumOf { it.amount },
                    startEpochDay = epochDay,
                    endEpochDay = epochDay,
                )
            }
        }

        PeriodType.WEEKLY -> {
            val lastWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            (0 until 8).map { index ->
                val start = lastWeek.minusWeeks((7 - index).toLong())
                bucket(start, start.plusDays(6), points, "${start.dayOfMonth}/${start.monthValue}")
            }
        }

        PeriodType.MONTHLY -> {
            val lastMonth = today.withDayOfMonth(1)
            (0 until 12).map { index ->
                val month = lastMonth.minusMonths((11 - index).toLong())
                val start = month
                val end = month.with(TemporalAdjusters.lastDayOfMonth())
                bucket(
                    start,
                    end,
                    points,
                    "%02d/%02d".format(month.monthValue, month.year % 100),
                )
            }
        }

        PeriodType.YEARLY -> {
            val lastYear = today.withDayOfYear(1)
            (0 until 5).map { index ->
                val year = lastYear.minusYears((4 - index).toLong())
                bucket(
                    year,
                    year.with(TemporalAdjusters.lastDayOfYear()),
                    points,
                    year.year.toString(),
                )
            }
        }
    }

    private fun bucket(
        start: LocalDate,
        end: LocalDate,
        points: List<ExpensePoint>,
        label: String,
    ): ExpenseTrendPoint {
        val startEpochDay = start.toEpochDay()
        val endEpochDay = end.toEpochDay()
        return ExpenseTrendPoint(
            label = label,
            amount = points
                .filter { it.dateEpochDay in startEpochDay..endEpochDay }
                .sumOf { it.amount },
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay,
        )
    }
}
