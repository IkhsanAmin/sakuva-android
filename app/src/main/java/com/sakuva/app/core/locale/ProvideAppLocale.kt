package com.sakuva.app.core.locale

import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun ProvideAppLocale(
    language: Language,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = remember(language) { Locale.forLanguageTag(language.tag) }

    val localizedContext = remember(context, configuration, locale) {
        val localizedConfiguration = Configuration(configuration).apply { setLocale(locale) }
        val configurationContext = context.createConfigurationContext(localizedConfiguration)
        object : ContextWrapper(configurationContext) {
            override fun getBaseContext(): android.content.Context = context
        }
    }
    val localizedConfiguration = remember(configuration, locale) {
        Configuration(configuration).apply { setLocale(locale) }
    }

    CompositionLocalProvider(
        LocalStrings provides stringsFor(language),
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        content = content,
    )
}
