package com.sakuva.app.domain.usecase

import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.DashboardSummary
import com.sakuva.app.domain.repository.TransactionRepository
import com.sakuva.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(period: PeriodType): Flow<DashboardSummary> {
        val range = DateTimeUtils.rangeFor(period)
        return combine(
            walletRepository.observeWalletBalances(),
            transactionRepository.observeIncomeTotal(range),
            transactionRepository.observeExpenseTotal(range),
        ) { balances, income, expense ->
            DashboardSummary(
                totalBalance = balances.sumOf { it.balance },
                totalIncome = income,
                totalExpense = expense,
            )
        }
    }
}
