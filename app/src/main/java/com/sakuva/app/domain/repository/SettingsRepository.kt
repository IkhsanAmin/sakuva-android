package com.sakuva.app.domain.repository

import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.locale.Language
import com.sakuva.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setLanguage(language: Language)
    suspend fun setCurrency(currencyCode: String)
    suspend fun setFirstLaunchTimestamp(timestamp: Long)
    suspend fun setLastBackupAt(epochMillis: Long)
    suspend fun setBackupReminderEnabled(enabled: Boolean)
    suspend fun setDailyReminderEnabled(enabled: Boolean)
    suspend fun setDailyReminderTime(hour: Int, minute: Int)
    suspend fun clearLastBackupAt()
    suspend fun setHasPromptedNotificationPermission(prompted: Boolean)
}
