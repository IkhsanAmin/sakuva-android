package com.sakuva.app.core.designsystem

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        fun fromName(name: String): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
