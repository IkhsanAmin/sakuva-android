package com.sakuva.app.feature.transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TransactionEditorRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onManageCategories: (() -> Unit)? = null,
    viewModel: TransactionEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    TransactionEditorScreen(
        uiState = uiState,
        onBack = onBack,
        onTypeSelected = viewModel::setType,
        onAmountChange = viewModel::setAmountText,
        onWalletSelected = viewModel::setWallet,
        onTargetWalletSelected = viewModel::setTargetWallet,
        onCategorySelected = viewModel::setCategory,
        onDateSelected = viewModel::setDate,
        onNoteChange = viewModel::setNote,
        onSave = viewModel::save,
        onManageCategories = onManageCategories,
    )
}
