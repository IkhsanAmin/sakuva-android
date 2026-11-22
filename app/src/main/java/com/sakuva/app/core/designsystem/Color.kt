package com.sakuva.app.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black

internal val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF2F3E4E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD5E2EE),
    onPrimaryContainer = Color(0xFF0E1B26),
    secondary = Color(0xFF52616E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE3EB),
    onSecondaryContainer = Color(0xFF101C26),
    tertiary = Color(0xFF46635B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9E8DC),
    onTertiaryContainer = Color(0xFF08201A),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF3F5F8),
    onBackground = Color(0xFF161B21),
    surface = Color(0xFFF7F9FB),
    onSurface = Color(0xFF161B21),
    surfaceVariant = Color(0xFFE3E8EE),
    onSurfaceVariant = Color(0xFF45525E),
    outline = Color(0xFF6F7B87),
    outlineVariant = Color(0xFFC6D0D9),
    inverseSurface = Color(0xFF2B3238),
    inverseOnSurface = Color(0xFFEEF1F4),
    inversePrimary = Color(0xFFAFC7DC),
    scrim = Black,
    surfaceTint = Color(0xFF2F3E4E),
)

internal val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFB3CADF),
    onPrimary = Color(0xFF17293A),
    primaryContainer = Color(0xFF2A3B4C),
    onPrimaryContainer = Color(0xFFD5E2EE),
    secondary = Color(0xFFB4C2CF),
    onSecondary = Color(0xFF1E2B36),
    secondaryContainer = Color(0xFF34424F),
    onSecondaryContainer = Color(0xFFDAE3EB),
    tertiary = Color(0xFFA8CFC0),
    onTertiary = Color(0xFF12332A),
    tertiaryContainer = Color(0xFF2A4A40),
    onTertiaryContainer = Color(0xFFC9E8DC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1217),
    onBackground = Color(0xFFE3E7EB),
    surface = Color(0xFF12171D),
    onSurface = Color(0xFFE3E7EB),
    surfaceVariant = Color(0xFF1F272F),
    onSurfaceVariant = Color(0xFFA9B6C2),
    outline = Color(0xFF84919D),
    outlineVariant = Color(0xFF2C353E),
    inverseSurface = Color(0xFFE3E7EB),
    inverseOnSurface = Color(0xFF2B3238),
    inversePrimary = Color(0xFF2F3E4E),
    scrim = Black,
    surfaceTint = Color(0xFFB3CADF),
)

data class SakuvaExtraColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val chartPalette: List<Color>,
)

internal val LightExtraColors = SakuvaExtraColors(
    income = Color(0xFF2E7D5B),
    expense = Color(0xFFB3261E),
    transfer = Color(0xFF3E6B8A),
    chartPalette = listOf(
        Color(0xFF2F3E4E),
        Color(0xFF3E6B8A),
        Color(0xFF46635B),
        Color(0xFF6D5B7C),
        Color(0xFF8A6D3B),
        Color(0xFF5B6B8A),
    ),
)

internal val DarkExtraColors = SakuvaExtraColors(
    income = Color(0xFF7FD3A8),
    expense = Color(0xFFFFB4AB),
    transfer = Color(0xFF9CC7E3),
    chartPalette = listOf(
        Color(0xFFB3CADF),
        Color(0xFF9CC7E3),
        Color(0xFFA8CFC0),
        Color(0xFFC9B6E8),
        Color(0xFFE2C58B),
        Color(0xFF9FB4D9),
    ),
)
