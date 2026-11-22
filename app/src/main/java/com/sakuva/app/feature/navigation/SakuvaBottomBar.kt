package com.sakuva.app.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.sakuva.app.core.locale.LocalStrings

private data class BottomTab(
    val destination: Any,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun SakuvaBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
) {
    val strings = LocalStrings.current
    val tabs = listOf(
        BottomTab(DashboardDestination, strings.dashboard, Icons.Filled.Home),
        BottomTab(HistoryDestination, strings.history, Icons.AutoMirrored.Filled.List),
        BottomTab(WalletDestination, strings.wallets, Icons.Filled.AccountBox),
        BottomTab(SettingsDestination, strings.settings, Icons.Filled.Settings),
    )

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { destination ->
                destination.hasRoute(tab.destination::class)
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToMain(tab.destination) },
                icon = {
                    Icon(imageVector = tab.icon, contentDescription = null)
                },
                label = { Text(text = tab.label) },
            )
        }
    }
}
