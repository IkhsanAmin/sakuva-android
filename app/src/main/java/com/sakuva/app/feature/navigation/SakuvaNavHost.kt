package com.sakuva.app.feature.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.feature.category.CategoryManagementRoute
import com.sakuva.app.feature.dashboard.DashboardRoute
import com.sakuva.app.feature.history.HistoryRoute
import com.sakuva.app.feature.settings.SettingsRoute
import com.sakuva.app.feature.transaction.TransactionEditorRoute
import com.sakuva.app.feature.wallet.WalletRoute

private val mainDestinations = listOf(
    DashboardDestination,
    HistoryDestination,
    WalletDestination,
    SettingsDestination,
)

@Composable
fun SakuvaNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val strings = LocalStrings.current

    val showBottomBar = currentDestination?.isMainDestination() == true
    val showFab = currentDestination?.let { destination ->
        destination.hasRoute(DashboardDestination::class) ||
            destination.hasRoute(HistoryDestination::class)
    } == true

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            if (showBottomBar) {
                SakuvaBottomBar(navController = navController, currentDestination = currentDestination)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(TransactionEditorDestination()) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = strings.addTransactionDescription,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<DashboardDestination> {
                DashboardRoute(
                    onAddTransaction = { navController.navigate(TransactionEditorDestination()) },
                    onSeeAllHistory = { navController.navigateToMain(HistoryDestination) },
                    onTransactionClick = { transactionId ->
                        navController.navigate(TransactionEditorDestination(transactionId))
                    },
                    onBackupNow = { navController.navigateToMain(SettingsDestination) },
                )
            }
            composable<HistoryDestination> {
                HistoryRoute(
                    onTransactionClick = { transactionId ->
                        navController.navigate(TransactionEditorDestination(transactionId = transactionId))
                    },
                    onDuplicate = { id ->
                        navController.navigate(TransactionEditorDestination(duplicateId = id))
                    },
                )
            }
            composable<WalletDestination> {
                WalletRoute()
            }
            composable<SettingsDestination> {
                SettingsRoute(
                    onManageCategories = {
                        navController.navigate(CategoryManagementDestination)
                    },
                )
            }
            composable<CategoryManagementDestination> {
                CategoryManagementRoute(onBack = { navController.popBackStack() })
            }
            composable<TransactionEditorDestination> {
                TransactionEditorRoute(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onManageCategories = {
                        navController.navigate(CategoryManagementDestination)
                    },
                )
            }
        }
    }
}

private fun NavDestination.isMainDestination(): Boolean =
    mainDestinations.any { destination -> hasRoute(destination::class) }

fun NavHostController.navigateToMain(destination: Any) {
    navigate(destination) {
        popUpTo(DashboardDestination) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
