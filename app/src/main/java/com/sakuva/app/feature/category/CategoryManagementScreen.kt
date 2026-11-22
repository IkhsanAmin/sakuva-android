package com.sakuva.app.feature.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.EmptyState
import com.sakuva.app.feature.common.LoadingState
import com.sakuva.app.feature.common.labelFor

@Composable
fun CategoryManagementScreen(
    uiState: CategoryUiState,
    onBack: () -> Unit,
    onAddCategory: (CategoryType) -> Unit,
    onEditCategory: (Category) -> Unit,
    onArchiveCategory: (Category) -> Unit,
) {
    val strings = LocalStrings.current
    var selectedType by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }
    val visibleCategories = uiState.categories.filter { it.type == selectedType }

    ContentWidthContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backDescription,
                        )
                    }
                    Text(
                        text = strings.categoriesTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                }
            }
            item {
                Button(
                    onClick = { onAddCategory(selectedType) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(text = strings.addCategory)
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                ) {
                    CategoryType.entries.forEach { categoryType ->
                        FilterChip(
                            selected = selectedType == categoryType,
                            onClick = { selectedType = categoryType },
                            label = { Text(text = strings.labelFor(categoryType)) },
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingState(modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else if (visibleCategories.isEmpty()) {
                item {
                    EmptyState(title = strings.noCategories)
                }
            } else {
                items(visibleCategories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEdit = { onEditCategory(category) },
                        onArchive = { onArchiveCategory(category) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}
