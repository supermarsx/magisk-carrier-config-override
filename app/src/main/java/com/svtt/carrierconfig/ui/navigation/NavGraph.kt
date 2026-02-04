package com.svtt.carrierconfig.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svtt.carrierconfig.ui.MainActivity
import com.svtt.carrierconfig.ui.screens.dashboard.DashboardScreen
import timber.log.Timber

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Method1 : Screen("method1")
    object Method2 : Screen("method2")
    object Diagnostics : Screen("diagnostics")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToMethod1 = {
                    Timber.d("Navigating to Method 1")
                    navController.navigate(Screen.Method1.route)
                },
                onNavigateToMethod2 = {
                    Timber.d("Navigating to Method 2")
                    navController.navigate(Screen.Method2.route)
                },
                onNavigateToDiagnostics = {
                    Timber.d("Navigating to Diagnostics")
                    navController.navigate(Screen.Diagnostics.route)
                },
                onOpenWfcSettings = {
                    Timber.d("Opening WFC settings")
                    (context as? MainActivity)?.openWfcSettings()
                }
            )
        }
        
        composable(Screen.Method1.route) {
            com.svtt.carrierconfig.ui.screens.method1.Method1Screen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Method2.route) {
            PlaceholderScreen("Method 2 - Coming Soon")
        }
        
        composable(Screen.Diagnostics.route) {
            PlaceholderScreen("Diagnostics - Coming Soon")
        }
        
        composable(Screen.Settings.route) {
            PlaceholderScreen("Settings - Coming Soon")
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = com.svtt.carrierconfig.ui.theme.TextPrimary
        )
    }
}
