package com.sakuva.app.feature.common

import com.sakuva.app.core.designsystem.ThemeMode
import com.sakuva.app.core.locale.AppStrings
import com.sakuva.app.core.locale.Language
import com.sakuva.app.core.time.DateRangePreset
import com.sakuva.app.core.time.PeriodType
import com.sakuva.app.domain.model.CategoryType
import com.sakuva.app.domain.model.TransactionType
import com.sakuva.app.domain.model.WalletType

fun AppStrings.labelFor(type: TransactionType): String = when (type) {
    TransactionType.INCOME -> typeIncome
    TransactionType.EXPENSE -> typeExpense
    TransactionType.TRANSFER -> typeTransfer
}

fun AppStrings.labelFor(type: WalletType): String = when (type) {
    WalletType.CASH -> walletTypeCash
    WalletType.BANK -> walletTypeBank
    WalletType.EWALLET -> walletTypeEwallet
}

fun AppStrings.labelFor(type: CategoryType): String = when (type) {
    CategoryType.INCOME -> categoryIncome
    CategoryType.EXPENSE -> categoryExpense
}

fun AppStrings.labelFor(period: PeriodType): String = when (period) {
    PeriodType.DAILY -> periodDaily
    PeriodType.WEEKLY -> periodWeekly
    PeriodType.MONTHLY -> periodMonthly
    PeriodType.YEARLY -> periodYearly
}

fun AppStrings.labelFor(preset: DateRangePreset): String = when (preset) {
    DateRangePreset.THIS_MONTH -> exportPeriodThisMonth
    DateRangePreset.LAST_30_DAYS -> exportPeriodLast30Days
    DateRangePreset.THIS_YEAR -> exportPeriodThisYear
    DateRangePreset.ALL_TIME -> exportPeriodAllTime
}

fun AppStrings.labelFor(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> themeSystem
    ThemeMode.LIGHT -> themeLight
    ThemeMode.DARK -> themeDark
}

fun AppStrings.labelFor(language: Language): String = when (language) {
    Language.INDONESIAN -> languageIndonesian
    Language.ENGLISH -> languageEnglish
}
