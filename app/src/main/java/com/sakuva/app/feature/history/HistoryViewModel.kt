package com.sakuva.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.core.time.PeriodRange
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionFilter
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.WalletRepository
import com.sakuva.app.domain.usecase.DeleteTransactionUseCase
import com.sakuva.app.domain.usecase.GetTransactionsUseCase
import com.sakuva.app.feature.common.Feedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryFilters(
    val query: String = "",
    val walletId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
    val monthStartEpochDay: Long = DateTimeUtils.monthStartEpochDay(),
    val customRange: PeriodRange? = null,
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
            walletId != null ||
            categoryId != null ||
            type != null ||
            customRange != null
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = "IDR",
    val filters: HistoryFilters = HistoryFilters(),
    val transactions: List<Transaction> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val walletNames: Map<Long, String> = emptyMap(),
    val categoryNames: Map<Long, String> = emptyMap(),
    val feedback: Feedback? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    getTransactions: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    private val filters = MutableStateFlow(HistoryFilters())
    private val feedback = MutableStateFlow<Feedback?>(null)

    private val transactions = filters.flatMapLatest { current ->
        val effectiveRange = when {
            current.customRange != null -> current.customRange
            current.query.isNotBlank() -> PeriodRange.allTime()
            else -> DateTimeUtils.monthRange(current.monthStartEpochDay)
        }
        val filter = TransactionFilter(
            query = current.query.trim(),
            walletId = current.walletId,
            categoryId = current.categoryId,
            type = current.type,
            range = effectiveRange,
        )
        getTransactions(filter)
    }

    private val baseState = combine(
        settingsRepository.observeSettings(),
        filters,
        transactions,
        walletRepository.observeAllWallets(),
        categoryRepository.observeAllCategories(),
    ) { settings, currentFilters, transactionList, wallets, categories ->
        HistoryUiState(
            isLoading = false,
            currencyCode = settings.currencyCode,
            filters = currentFilters,
            transactions = transactionList,
            wallets = wallets,
            categories = categories,
            walletNames = wallets.associate { it.id to it.name },
            categoryNames = categories.associate { it.id to it.name },
        )
    }

    val uiState: StateFlow<HistoryUiState> = combine(baseState, feedback) { state, currentFeedback ->
        state.copy(feedback = currentFeedback)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun setQuery(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun setWallet(walletId: Long?) {
        filters.value = filters.value.copy(walletId = walletId)
    }

    fun setCategory(categoryId: Long?) {
        filters.value = filters.value.copy(categoryId = categoryId)
    }

    fun setType(type: TransactionType?) {
        filters.value = filters.value.copy(type = type)
    }

    fun shiftMonth(delta: Int) {
        val nextMonth = DateTimeUtils.shiftMonth(filters.value.monthStartEpochDay, delta)
        filters.value = filters.value.copy(monthStartEpochDay = nextMonth, customRange = null)
    }

    fun setMonth(epochDay: Long) {
        val start = DateTimeUtils.monthStartEpochDay(DateTimeUtils.toLocalDate(epochDay))
        filters.value = filters.value.copy(monthStartEpochDay = start, customRange = null)
    }

    fun setCustomRange(startEpochDay: Long, endEpochDay: Long) {
        filters.value = filters.value.copy(
            customRange = PeriodRange(startEpochDay, endEpochDay),
        )
    }

    fun clearCustomRange() {
        filters.value = filters.value.copy(customRange = null)
    }

    fun clearFilters() {
        filters.value = HistoryFilters(monthStartEpochDay = filters.value.monthStartEpochDay)
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
            feedback.value = Feedback.TRANSACTION_DELETED
        }
    }

    fun consumeFeedback() {
        feedback.value = null
    }
}
