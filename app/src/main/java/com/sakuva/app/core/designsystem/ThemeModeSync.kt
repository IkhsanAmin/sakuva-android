package com.sakuva.app.core.designsystem

import android.content.Context
import android.os.Build
import android.app.UiModeManager

fun syncApplicationNightMode(context: Context, themeMode: ThemeMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val uiModeManager = context.getSystemService(UiModeManager::class.java) ?: return
        val nightMode = when (themeMode) {
            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        }
        uiModeManager.setApplicationNightMode(nightMode)
    }
}
