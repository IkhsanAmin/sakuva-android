package com.sakuva.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.locale.Language
import com.sakuva.app.domain.model.AppSettings
import com.sakuva.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "sakuva_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppSettings(
                themeMode = ThemeMode.fromName(preferences[Keys.THEME] ?: ThemeMode.SYSTEM.name),
                language = Language.fromTag(preferences[Keys.LANGUAGE] ?: Language.INDONESIAN.tag),
                currencyCode = preferences[Keys.CURRENCY] ?: "IDR",
                firstLaunchTimestamp = preferences[Keys.FIRST_LAUNCH_TS] ?: 0L,
                lastBackupAt = preferences[Keys.LAST_BACKUP_AT],
                backupReminderEnabled = preferences[Keys.BACKUP_REMINDER] ?: true,
                dailyReminderEnabled = preferences[Keys.DAILY_REMINDER] ?: false,
                dailyReminderHour = preferences[Keys.REMINDER_HOUR]
                    ?: AppSettings.DEFAULT_REMINDER_HOUR,
                dailyReminderMinute = preferences[Keys.REMINDER_MINUTE]
                    ?: AppSettings.DEFAULT_REMINDER_MINUTE,
                hasPromptedNotificationPermission = preferences[Keys.HAS_PROMPTED_NOTIFICATION] ?: false,
            )
        }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.THEME] = themeMode.name
        }
    }

    override suspend fun setLanguage(language: Language) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language.tag
        }
    }

    override suspend fun setCurrency(currencyCode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.CURRENCY] = currencyCode
        }
    }

    override suspend fun setFirstLaunchTimestamp(timestamp: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.FIRST_LAUNCH_TS] = timestamp
        }
    }

    override suspend fun setLastBackupAt(epochMillis: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LAST_BACKUP_AT] = epochMillis
        }
    }

    override suspend fun setBackupReminderEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.BACKUP_REMINDER] = enabled
        }
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DAILY_REMINDER] = enabled
        }
    }

    override suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.REMINDER_HOUR] = hour
            preferences[Keys.REMINDER_MINUTE] = minute
        }
    }

    override suspend fun clearLastBackupAt() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(Keys.LAST_BACKUP_AT)
        }
    }

    override suspend fun setHasPromptedNotificationPermission(prompted: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.HAS_PROMPTED_NOTIFICATION] = prompted
        }
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY = stringPreferencesKey("currency")
        val FIRST_LAUNCH_TS = longPreferencesKey("first_launch_timestamp")
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
        val BACKUP_REMINDER = booleanPreferencesKey("backup_reminder_enabled")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val HAS_PROMPTED_NOTIFICATION = booleanPreferencesKey("has_prompted_notification_permission")
    }
}
