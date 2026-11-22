package com.sakuva.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.time.DateRangePreset
import com.sakuva.app.feature.common.DestructiveConfirmDialog
import com.sakuva.app.feature.common.labelFor

@Composable
fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    DestructiveConfirmDialog(
        title = strings.restoreConfirmTitle,
        message = strings.restoreConfirmMessage,
        confirmLabel = strings.restoreConfirmAction,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun ResetDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    DestructiveConfirmDialog(
        title = strings.resetDialogTitle,
        message = strings.resetDialogMessage,
        confirmLabel = strings.resetConfirm,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun ExportPeriodDialog(
    onConfirm: (DateRangePreset) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var selected by remember { mutableStateOf(DateRangePreset.THIS_MONTH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = strings.exportCsv) },
        text = {
            Column {
                DateRangePreset.entries.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == preset,
                                role = Role.RadioButton,
                                onClick = { selected = preset },
                            )
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == preset,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(text = strings.labelFor(preset))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(text = strings.exportCsv)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = strings.cancel)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int,
) {
    val strings = LocalStrings.current
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = strings.chooseTime) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                },
            ) {
                Text(text = strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = strings.cancel)
            }
        },
    )
}
