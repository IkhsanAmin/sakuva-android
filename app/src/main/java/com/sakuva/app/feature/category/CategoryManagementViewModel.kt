package com.sakuva.app.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.repository.CategoryRepository
import com.sakuva.app.feature.common.Feedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val feedback: Feedback? = null,
)

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val feedback = MutableStateFlow<Feedback?>(null)

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryRepository.observeCategories(type = null),
        feedback,
    ) { categories, currentFeedback ->
        CategoryUiState(
            isLoading = false,
            categories = categories,
            feedback = currentFeedback,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryUiState(),
    )

    fun saveCategory(
        categoryId: Long?,
        name: String,
        type: CategoryType,
    ) {
        if (name.isBlank()) {
            feedback.value = Feedback.ERROR
            return
        }
        viewModelScope.launch {
            val existing = uiState.value.categories.firstOrNull { it.id == categoryId }
            val category = Category(
                id = categoryId ?: 0L,
                name = name.trim(),
                type = type,
                iconKey = existing?.iconKey ?: "category",
                colorHex = existing?.colorHex ?: defaultColorFor(type),
                isDefault = existing?.isDefault ?: false,
                isArchived = existing?.isArchived ?: false,
            )
            runCatching { categoryRepository.saveCategory(category) }
                .onSuccess {
                    feedback.value = if (categoryId == null) Feedback.CATEGORY_CREATED else Feedback.CATEGORY_UPDATED
                }
                .onFailure { feedback.value = Feedback.ERROR }
        }
    }

    fun archiveCategory(categoryId: Long) {
        viewModelScope.launch {
            runCatching { categoryRepository.archiveCategory(categoryId) }
                .onSuccess { feedback.value = Feedback.CATEGORY_ARCHIVED }
                .onFailure { feedback.value = Feedback.ERROR }
        }
    }

    fun consumeFeedback() {
        feedback.value = null
    }

    private fun defaultColorFor(type: CategoryType): String = when (type) {
        CategoryType.INCOME -> "#1B6C4F"
        CategoryType.EXPENSE -> "#E07A3F"
    }
}
