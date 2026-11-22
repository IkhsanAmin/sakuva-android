package com.sakuva.app.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakuva.app.core.designsystem.SakuvaTheme
import com.sakuva.app.core.locale.ProvideAppLocale
import com.sakuva.app.feature.navigation.SakuvaNavHost

@Composable
fun SakuvaApp(appViewModel: AppViewModel = hiltViewModel()) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()

    ProvideAppLocale(language = settings.language) {
        SakuvaTheme(themeMode = settings.themeMode) {
            SakuvaNavHost()
        }
    }
}
