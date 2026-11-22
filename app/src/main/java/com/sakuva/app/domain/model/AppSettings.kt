package com.sakuva.app.domain.model

import com.sakuva.app.core.locale.Language
import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.time.PeriodRange

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: Language = Language.INDONESIAN,
    val currencyCode: String = "IDR",
    val firstLaunchTimestamp: Long = 0L,
    val lastBackupAt: Long? = null,
    val backupReminderEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = DEFAULT_REMINDER_HOUR,
    val dailyReminderMinute: Int = DEFAULT_REMINDER_MINUTE,
    val hasPromptedNotificationPermission: Boolean = false,
) {
    companion object {
        const val DEFAULT_REMINDER_HOUR = 20
        const val DEFAULT_REMINDER_MINUTE = 0
    }
}

data class TransactionFilter(
    val query: String = "",
    val walletId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
    val range: PeriodRange = PeriodRange.allTime(),
)
