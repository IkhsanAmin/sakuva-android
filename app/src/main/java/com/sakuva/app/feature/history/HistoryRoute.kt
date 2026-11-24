package com.sakuva.app.feature.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakuva.app.feature.common.FeedbackEffect
import com.sakuva.app.feature.navigation.TransactionEditorDestination

@Composable
fun HistoryRoute(
    onTransactionClick: (Long) -> Unit,
    onDuplicate: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    FeedbackEffect(
        feedback = uiState.feedback,
        snackbarHostState = snackbarHostState,
        onConsumed = viewModel::consumeFeedback,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HistoryScreen(
            uiState = uiState,
            onQueryChange = viewModel::setQuery,
            onWalletSelected = viewModel::setWallet,
            onCategorySelected = viewModel::setCategory,
            onTypeSelected = viewModel::setType,
            onMonthSelected = viewModel::setMonth,
            onShiftMonth = viewModel::shiftMonth,
            onCustomRangeSelected = { start, end -> viewModel.setCustomRange(start, end) },
            onClearFilters = { viewModel.clearFilters() },
            onEdit = onTransactionClick,
            onDuplicate = onDuplicate,
            onDelete = viewModel::deleteTransaction,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
