package com.sakuva.app.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.Transaction
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.TransactionRepository
import com.sakuva.app.domain.repository.WalletRepository
import com.sakuva.app.domain.usecase.SaveTransactionUseCase
import com.sakuva.app.domain.usecase.TransactionValidationError
import com.sakuva.app.domain.usecase.TransactionValidationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFormError {
    AMOUNT_INVALID,
    WALLET_REQUIRED,
    CATEGORY_REQUIRED,
    TARGET_WALLET_REQUIRED,
    SAME_WALLET,
    NOTE_TOO_LONG,
    GENERIC,
}

data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val walletId: Long? = null,
    val toWalletId: Long? = null,
    val categoryId: Long? = null,
    val dateEpochDay: Long = DateTimeUtils.todayEpochDay(),
    val note: String = "",
)

data class TransactionEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val currencyCode: String = "IDR",
    val form: TransactionFormState = TransactionFormState(),
    val wallets: List<Wallet> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: TransactionFormError? = null,
    val saved: Boolean = false,
)

private data class EditorMeta(
    val isLoading: Boolean,
    val error: TransactionFormError?,
    val saved: Boolean,
)

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val saveTransaction: SaveTransactionUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")
    private val duplicateId: Long? = savedStateHandle.get<Long>("duplicateId")
    private val form = MutableStateFlow(TransactionFormState())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<TransactionFormError?>(null)
    private val saved = MutableStateFlow(false)
    private var originalCreatedAt: Long = 0L

    private val meta = combine(loading, error, saved) { isLoading, currentError, isSaved ->
        EditorMeta(isLoading = isLoading, error = currentError, saved = isSaved)
    }

    val uiState: StateFlow<TransactionEditorUiState> = combine(
        settingsRepository.observeSettings(),
        form,
        walletRepository.observeActiveWallets(),
        categoryRepository.observeCategories(type = null),
        meta,
    ) { settings, currentForm, wallets, categories, editorMeta ->
        TransactionEditorUiState(
            isLoading = editorMeta.isLoading,
            isEditing = transactionId != null,
            currencyCode = settings.currencyCode,
            form = currentForm,
            wallets = wallets,
            categories = categories,
            error = editorMeta.error,
            saved = editorMeta.saved,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionEditorUiState(),
    )

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val currencyCode = settingsRepository.observeSettings().first().currencyCode
            loadExistingTransaction(currencyCode)
            applyDefaults()
            loading.value = false
        }
    }

    private suspend fun loadExistingTransaction(currencyCode: String) {
        val existingId = transactionId ?: duplicateId ?: return
        val existing = transactionRepository.getTransaction(existingId) ?: return

        if (transactionId != null) {
            originalCreatedAt = existing.createdAt
        }

        form.value = TransactionFormState(
            type = existing.type,
            amountText = MoneyFormatter.toEditableString(existing.amount, currencyCode),
            walletId = existing.walletId,
            toWalletId = existing.toWalletId,
            categoryId = existing.categoryId,
            dateEpochDay = if (transactionId != null) {
                existing.dateEpochDay
            } else {
                DateTimeUtils.todayEpochDay()
            },
            note = existing.note,
        )
    }

    private suspend fun applyDefaults() {
        val current = form.value
        val wallets = walletRepository.observeActiveWallets().first()
        val categories = categoryRepository.observeCategories(type = null).first()

        val walletId = current.walletId ?: wallets.firstOrNull()?.id
        val categoryId = if (current.type == TransactionType.TRANSFER) {
            null
        } else {
            current.categoryId ?: categories
                .firstOrNull { it.type == current.type.toCategoryType() }
                ?.id
        }
        form.value = current.copy(walletId = walletId, categoryId = categoryId)
    }

    fun setType(type: TransactionType) {
        val current = form.value
        val categories = uiState.value.categories
        val categoryId = if (type == TransactionType.TRANSFER) {
            null
        } else {
            categories.firstOrNull { it.type == type.toCategoryType() }?.id
        }
        form.value = current.copy(
            type = type,
            categoryId = categoryId,
            toWalletId = if (type == TransactionType.TRANSFER) current.toWalletId else null,
        )
        error.value = null
    }

    fun setAmountText(text: String) {
        val filtered = text
            .filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
            .take(MAX_AMOUNT_INPUT_LENGTH)
        form.value = form.value.copy(amountText = filtered)
        error.value = null
    }

    fun setWallet(walletId: Long) {
        form.value = form.value.copy(walletId = walletId)
        error.value = null
    }

    fun setTargetWallet(walletId: Long) {
        form.value = form.value.copy(toWalletId = walletId)
        error.value = null
    }

    fun setCategory(categoryId: Long) {
        form.value = form.value.copy(categoryId = categoryId)
        error.value = null
    }

    fun setDate(epochDay: Long) {
        form.value = form.value.copy(dateEpochDay = epochDay)
    }

    fun setNote(note: String) {
        form.value = form.value.copy(note = note.take(SaveTransactionUseCase.MAX_NOTE_LENGTH))
        error.value = null
    }

    fun save() {
        val current = form.value
        val amount = MoneyFormatter.parse(current.amountText, uiState.value.currencyCode)
        if (amount == null || amount <= 0L) {
            error.value = TransactionFormError.AMOUNT_INVALID
            return
        }

        val transaction = Transaction(
            id = transactionId ?: 0L,
            amount = amount,
            type = current.type,
            walletId = current.walletId ?: 0L,
            toWalletId = current.toWalletId,
            categoryId = current.categoryId,
            dateEpochDay = current.dateEpochDay,
            note = current.note.trim(),
            createdAt = if (transactionId != null) originalCreatedAt else System.currentTimeMillis(),
        )

        val validationError = SaveTransactionUseCase.validate(transaction)
        if (validationError != null) {
            error.value = validationError.toFormError()
            return
        }

        viewModelScope.launch {
            saveTransaction(transaction)
                .onSuccess { saved.value = true }
                .onFailure { failure ->
                    error.value = if (failure is TransactionValidationException) {
                        failure.error.toFormError()
                    } else {
                        TransactionFormError.GENERIC
                    }
                }
        }
    }

    private fun TransactionType.toCategoryType(): CategoryType = when (this) {
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.TRANSFER -> CategoryType.EXPENSE
    }

    private fun TransactionValidationError.toFormError(): TransactionFormError = when (this) {
        TransactionValidationError.AMOUNT_INVALID -> TransactionFormError.AMOUNT_INVALID
        TransactionValidationError.WALLET_REQUIRED -> TransactionFormError.WALLET_REQUIRED
        TransactionValidationError.CATEGORY_REQUIRED -> TransactionFormError.CATEGORY_REQUIRED
        TransactionValidationError.TARGET_WALLET_REQUIRED -> TransactionFormError.TARGET_WALLET_REQUIRED
        TransactionValidationError.SAME_WALLET -> TransactionFormError.SAME_WALLET
        TransactionValidationError.NOTE_TOO_LONG -> TransactionFormError.NOTE_TOO_LONG
        TransactionValidationError.INVALID_COMBINATION -> TransactionFormError.GENERIC
    }

    private companion object {
        const val MAX_AMOUNT_INPUT_LENGTH = 20
    }
}
