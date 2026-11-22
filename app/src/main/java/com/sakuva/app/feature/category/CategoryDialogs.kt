package com.sakuva.app.feature.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.domain.model.Category
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.feature.common.DestructiveConfirmDialog
import com.sakuva.app.feature.common.labelFor

@Composable
fun CategoryEditorDialog(
    category: Category?,
    initialType: CategoryType,
    onDismiss: () -> Unit,
    onSave: (name: String, type: CategoryType) -> Unit,
) {
    val strings = LocalStrings.current
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var type by remember(category?.id) { mutableStateOf(category?.type ?: initialType) }
    var error by remember(category?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (category == null) strings.addCategory else strings.editCategory)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text(text = strings.categoryNameLabel) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(text = message) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = strings.categoryTypeLabel,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    CategoryType.entries.forEach { categoryType ->
                        FilterChip(
                            selected = type == categoryType,
                            onClick = { type = categoryType },
                            label = { Text(text = strings.labelFor(categoryType)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        error = strings.categoryNameRequired
                        return@TextButton
                    }
                    onSave(trimmedName, type)
                },
            ) {
                Text(text = strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel)
            }
        },
    )
}

@Composable
fun ArchiveCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val strings = LocalStrings.current
    DestructiveConfirmDialog(
        title = strings.archiveCategoryTitle,
        message = strings.archiveCategoryMessage,
        confirmLabel = strings.archiveCategory,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
