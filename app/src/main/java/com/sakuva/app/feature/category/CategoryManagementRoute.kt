package com.sakuva.app.feature.category

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
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.feature.common.FeedbackEffect

@Composable
fun CategoryManagementRoute(
    onBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editorCategory by remember { mutableStateOf<Category?>(null) }
    var editorType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var showEditor by remember { mutableStateOf(false) }
    var categoryToArchive by remember { mutableStateOf<Category?>(null) }

    FeedbackEffect(
        feedback = uiState.feedback,
        snackbarHostState = snackbarHostState,
        onConsumed = viewModel::consumeFeedback,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CategoryManagementScreen(
            uiState = uiState,
            onBack = onBack,
            onAddCategory = { type ->
                editorCategory = null
                editorType = type
                showEditor = true
            },
            onEditCategory = { category ->
                editorCategory = category
                editorType = category.type
                showEditor = true
            },
            onArchiveCategory = { category -> categoryToArchive = category },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(Spacing.lg),
        )
    }

    if (showEditor) {
        CategoryEditorDialog(
            category = editorCategory,
            initialType = editorType,
            onDismiss = { showEditor = false },
            onSave = { name, type ->
                viewModel.saveCategory(editorCategory?.id, name, type)
                showEditor = false
            },
        )
    }

    categoryToArchive?.let { category ->
        ArchiveCategoryDialog(
            onDismiss = { categoryToArchive = null },
            onConfirm = {
                viewModel.archiveCategory(category.id)
                categoryToArchive = null
            },
        )
    }
}
