package com.sakuva.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sakuva.app.BuildConfig
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.locale.Language
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.time.DateTimeUtils
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.LoadingState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (Language) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExportCsv: () -> Unit,
    onManageCategories: () -> Unit,
    onBackupReminderToggle: (Boolean) -> Unit,
    onDailyReminderToggle: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onResetData: () -> Unit,
) {
    val strings = LocalStrings.current
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    if (showTimePicker) {
        val settings = uiState.settings
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                onTimeSelected(hour, minute)
                showTimePicker = false
            },
            initialHour = settings.dailyReminderHour,
            initialMinute = settings.dailyReminderMinute,
        )
    }

    ContentWidthContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item {
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                }

                if (uiState.isLoading) {
                    item {
                        LoadingState(modifier = Modifier.fillMaxWidth().height(240.dp))
                    }
                } else {
                    item {
                        SettingsSection(title = strings.appearanceSection) {
                            Text(
                                text = strings.themeLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            ThemeSelector(
                                selected = uiState.settings.themeMode,
                                onSelect = onThemeSelected,
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text = strings.languageLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            LanguageSelector(
                                selected = uiState.settings.language,
                                onSelect = onLanguageSelected,
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text = strings.currencyLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            CurrencySelector(
                                selected = uiState.settings.currencyCode,
                                onSelect = onCurrencySelected,
                            )
                        }
                    }

                    item {
                        SettingsSection(title = strings.dataSection) {
                            SettingsActionRow(
                                title = strings.backupData,
                                description = strings.backupDataDesc,
                                icon = Icons.Filled.Share,
                                onClick = onBackup,
                                enabled = !uiState.isBusy,
                            )
                            Text(
                                text = if (uiState.lastBackupAt != null) {
                                    "${strings.lastBackupLabel}: ${DateTimeUtils.formatEpochMillis(uiState.lastBackupAt, "dd MMM yyyy HH:mm")}"
                                } else {
                                    strings.notBackedUp
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Spacing.lg, bottom = Spacing.sm),
                            )
                            HorizontalDivider()
                            SettingsActionRow(
                                title = strings.restoreData,
                                description = strings.restoreDataDesc,
                                icon = Icons.Filled.Refresh,
                                onClick = onRestore,
                                enabled = !uiState.isBusy,
                            )
                            HorizontalDivider()
                            SettingsActionRow(
                                title = strings.exportCsv,
                                description = strings.exportCsvDesc,
                                icon = Icons.AutoMirrored.Filled.Send,
                                onClick = onExportCsv,
                                enabled = !uiState.isBusy,
                            )
                            HorizontalDivider()
                            SettingsActionRow(
                                title = strings.manageCategories,
                                description = strings.manageCategoriesDesc,
                                icon = Icons.AutoMirrored.Filled.List,
                                onClick = onManageCategories,
                                enabled = !uiState.isBusy,
                            )
                            HorizontalDivider()
                            SettingsActionRow(
                                title = strings.resetData,
                                description = strings.resetDataDesc,
                                icon = Icons.Filled.Delete,
                                onClick = onResetData,
                                enabled = !uiState.isBusy,
                                destructive = true,
                            )
                        }
                    }

                    item {
                        SettingsSection(title = strings.dailyReminderSection) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
                                    Text(
                                        text = strings.dailyReminderLabel,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = strings.dailyReminderDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.dailyReminderEnabled,
                                    onCheckedChange = onDailyReminderToggle,
                                )
                            }
                            if (uiState.settings.dailyReminderEnabled) {
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = strings.reminderTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = "${uiState.settings.dailyReminderHour}:${uiState.settings.dailyReminderMinute.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showTimePicker = true },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
                                    Text(
                                        text = strings.backupReminderTitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = strings.backupReminderMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.backupReminderEnabled,
                                    onCheckedChange = onBackupReminderToggle,
                                )
                            }
                        }
                    }

                    item {
                        SettingsSection(title = strings.aboutSection) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.appVersion,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = BuildConfig.VERSION_NAME,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        // Footer copyright di bawah About
                        Column(
                            modifier = Modifier
                                .padding(top = 28.dp, bottom = 36.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(0.5.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f),
                                    ),
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = strings.appCopyrightBrand,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.08.em,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = strings.appCopyrightCreator,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.03.em,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f),
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }

            if (uiState.isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }
        }
    }
}
