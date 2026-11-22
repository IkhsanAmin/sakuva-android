package com.sakuva.app

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.sakuva.app.core.designsystem.syncApplicationNightMode
import com.sakuva.app.domain.repository.SettingsRepository
import com.sakuva.app.feature.SakuvaApp
import com.sakuva.app.feature.settings.DailyReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val themeReady = MutableStateFlow(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lifecycleScope.launch {
                val settings = settingsRepository.observeSettings().first()
                settingsRepository.setHasPromptedNotificationPermission(true)
                if (granted && !settings.dailyReminderEnabled) {
                    settingsRepository.setDailyReminderEnabled(true)
                    DailyReminderScheduler.schedule(
                        this@MainActivity,
                        settings.dailyReminderHour,
                        settings.dailyReminderMinute,
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !themeReady.value }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        lifecycleScope.launch {
            val settings = runCatching { settingsRepository.observeSettings().first() }.getOrNull()
            settings?.let { syncApplicationNightMode(this@MainActivity, it.themeMode) }
            themeReady.value = true

            if (settings != null && !settings.hasPromptedNotificationPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    settingsRepository.setHasPromptedNotificationPermission(true)
                    if (!settings.dailyReminderEnabled) {
                        settingsRepository.setDailyReminderEnabled(true)
                        DailyReminderScheduler.schedule(
                            this@MainActivity,
                            settings.dailyReminderHour,
                            settings.dailyReminderMinute,
                        )
                    }
                }
            }
        }

        setContent {
            SakuvaApp()
        }
    }
}
