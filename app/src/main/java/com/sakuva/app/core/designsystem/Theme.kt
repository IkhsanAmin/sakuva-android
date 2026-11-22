@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.sakuva.app.core.designsystem

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sakuva.app.R

val LocalSakuvaExtraColors = staticCompositionLocalOf {
    LightExtraColors
}

val LocalSakuvaDarkTheme = staticCompositionLocalOf { false }

object SakuvaTheme {
    val extraColors: SakuvaExtraColors
        @Composable get() = LocalSakuvaExtraColors.current

    val isDark: Boolean
        @Composable get() = LocalSakuvaDarkTheme.current
}

private fun jakartaFont(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private val SakuvaFontFamily = FontFamily(
    jakartaFont(400),
    jakartaFont(500),
    jakartaFont(600),
    jakartaFont(700),
)

private fun TextStyle.asSakuvaStyle(): TextStyle = copy(
    fontFamily = SakuvaFontFamily,
    fontFeatureSettings = "tnum",
)

private fun Typography.asSakuvaTypography(): Typography = Typography(
    displayLarge = displayLarge.asSakuvaStyle(),
    displayMedium = displayMedium.asSakuvaStyle(),
    displaySmall = displaySmall.asSakuvaStyle(),
    headlineLarge = headlineLarge.asSakuvaStyle(),
    headlineMedium = headlineMedium.asSakuvaStyle(),
    headlineSmall = headlineSmall.asSakuvaStyle(),
    titleLarge = titleLarge.asSakuvaStyle(),
    titleMedium = titleMedium.asSakuvaStyle(),
    titleSmall = titleSmall.asSakuvaStyle(),
    bodyLarge = bodyLarge.asSakuvaStyle(),
    bodyMedium = bodyMedium.asSakuvaStyle(),
    bodySmall = bodySmall.asSakuvaStyle(),
    labelLarge = labelLarge.asSakuvaStyle(),
    labelMedium = labelMedium.asSakuvaStyle(),
    labelSmall = labelSmall.asSakuvaStyle(),
)

internal val SakuvaTypography = Typography().asSakuvaTypography()

internal val SakuvaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SakuvaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    SyncSystemBars(darkTheme = darkTheme)

    CompositionLocalProvider(
        LocalSakuvaExtraColors provides extraColors,
        LocalSakuvaDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SakuvaTypography,
            shapes = SakuvaShapes,
            content = content,
        )
    }
}

@Composable
private fun SyncSystemBars(darkTheme: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    SideEffect {
        val style = if (darkTheme) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity.enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style,
        )
    }
}
