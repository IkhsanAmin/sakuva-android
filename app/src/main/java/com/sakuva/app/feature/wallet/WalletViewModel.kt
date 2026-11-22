package com.sakuva.app.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.domain.model.WalletBalance
import com.sakuva.app.domain.model.WalletType
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.domain.repository.WalletRepository
import com.sakuva.app.feature.common.Feedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = "IDR",
    val wallets: List<WalletBalance> = emptyList(),
    val feedback: Feedback? = null,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val feedback = MutableStateFlow<Feedback?>(null)

    val uiState: StateFlow<WalletUiState> = combine(
        settingsRepository.observeSettings(),
        walletRepository.observeWalletBalances(),
        feedback,
    ) { settings, wallets, currentFeedback ->
        WalletUiState(
            isLoading = false,
            currencyCode = settings.currencyCode,
            wallets = wallets,
            feedback = currentFeedback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WalletUiState(),
    )

    fun saveWallet(
        walletId: Long?,
        name: String,
        type: WalletType,
        initialBalance: Long,
    ) {
        if (name.isBlank()) {
            feedback.value = Feedback.ERROR
            return
        }
        viewModelScope.launch {
            val existing = uiState.value.wallets.firstOrNull { it.wallet.id == walletId }?.wallet
            val wallet = Wallet(
                id = walletId ?: 0L,
                name = name.trim(),
                type = type,
                initialBalance = initialBalance,
                iconKey = existing?.iconKey ?: type.name.lowercase(Locale.ROOT),
                colorHex = existing?.colorHex ?: defaultColorFor(type),
                isArchived = existing?.isArchived ?: false,
                createdAt = existing?.createdAt ?: 0L,
            )
            runCatching { walletRepository.saveWallet(wallet) }
                .onSuccess {
                    feedback.value = if (walletId == null) Feedback.WALLET_CREATED else Feedback.WALLET_UPDATED
                }
                .onFailure { feedback.value = Feedback.ERROR }
        }
    }

    fun archiveWallet(walletId: Long) {
        viewModelScope.launch {
            runCatching { walletRepository.archiveWallet(walletId) }
                .onSuccess { feedback.value = Feedback.WALLET_ARCHIVED }
                .onFailure { feedback.value = Feedback.ERROR }
        }
    }

    fun consumeFeedback() {
        feedback.value = null
    }

    private fun defaultColorFor(type: WalletType): String = when (type) {
        WalletType.CASH -> "#1B6C4F"
        WalletType.BANK -> "#2F6690"
        WalletType.EWALLET -> "#6A4C93"
    }
}
