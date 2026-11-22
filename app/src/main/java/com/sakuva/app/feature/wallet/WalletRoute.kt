package com.sakuva.app.feature.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.feature.common.FeedbackEffect

@Composable
fun WalletRoute(
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editorWallet by remember { mutableStateOf<Wallet?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var walletToArchive by remember { mutableStateOf<Wallet?>(null) }

    FeedbackEffect(
        feedback = uiState.feedback,
        snackbarHostState = snackbarHostState,
        onConsumed = viewModel::consumeFeedback,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        WalletScreen(
            uiState = uiState,
            onAddWallet = {
                editorWallet = null
                showEditor = true
            },
            onEditWallet = { wallet ->
                editorWallet = wallet
                showEditor = true
            },
            onArchiveWallet = { wallet -> walletToArchive = wallet },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(Spacing.lg),
        )
    }

    if (showEditor) {
        WalletEditorDialog(
            wallet = editorWallet,
            currencyCode = uiState.currencyCode,
            onDismiss = { showEditor = false },
            onSave = { name, type, initialBalance ->
                viewModel.saveWallet(editorWallet?.id, name, type, initialBalance)
                showEditor = false
            },
        )
    }

    walletToArchive?.let { wallet ->
        ArchiveWalletDialog(
            onDismiss = { walletToArchive = null },
            onConfirm = {
                viewModel.archiveWallet(wallet.id)
                walletToArchive = null
            },
        )
    }
}
