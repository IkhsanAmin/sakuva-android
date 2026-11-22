package com.sakuva.app.domain.usecase

import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.CategoryExpense
import com.sakuva.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoryExpensesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(
        period: PeriodType,
        maxCategories: Int = DEFAULT_MAX_CATEGORIES,
    ): Flow<List<CategoryExpense>> {
        val range = DateTimeUtils.rangeFor(period)
        return transactionRepository.observeExpenseByCategory(range, QUERY_LIMIT)
            .map { slices ->
                val sorted = slices.sortedByDescending { it.amount }
                val top = sorted.take(maxCategories).map { slice ->
                    CategoryExpense(
                        categoryId = slice.categoryId,
                        label = slice.label,
                        colorHex = slice.colorHex,
                        amount = slice.amount,
                    )
                }
                val remaining = sorted.drop(maxCategories)
                if (remaining.isEmpty()) {
                    top
                } else {
                    top + CategoryExpense(
                        categoryId = null,
                        label = "",
                        colorHex = OTHER_COLOR,
                        amount = remaining.sumOf { it.amount },
                        isOther = true,
                    )
                }
            }
    }

    companion object {
        const val DEFAULT_MAX_CATEGORIES = 4
        private const val QUERY_LIMIT = 100
        private const val OTHER_COLOR = "#9AA6B2"
    }
}
