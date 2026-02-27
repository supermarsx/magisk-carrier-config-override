package com.supermarsx.carrierconfig.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.supermarsx.carrierconfig.ui.screens.dashboard.DashboardScreen
import com.supermarsx.carrierconfig.ui.screens.diagnostics.DiagnosticsScreen
import com.supermarsx.carrierconfig.ui.screens.carrierconfig.CarrierConfigScreen
import com.supermarsx.carrierconfig.ui.screens.entitlement.EntitlementScreen
import com.supermarsx.carrierconfig.ui.theme.AccentPrimary
import com.supermarsx.carrierconfig.ui.theme.BackgroundDark
import com.supermarsx.carrierconfig.ui.theme.GlassSurfaceMedium
import com.supermarsx.carrierconfig.ui.theme.TextPrimary
import com.supermarsx.carrierconfig.ui.theme.TextSecondary

/**
 * Main navigation host for CCO app
 */
@Composable
fun CCONavHost(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            CCOBottomNavigation(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.CarrierConfig.route) {
                CarrierConfigScreen()
            }
            composable(Screen.Entitlement.route) {
                EntitlementScreen()
            }
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen()
            }
        }
    }
}

/**
 * Bottom navigation bar with glassmorphism styling
 */
@Composable
fun CCOBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        NavigationItem.Dashboard,
        NavigationItem.CarrierConfig,
        NavigationItem.Entitlement,
        NavigationItem.Diagnostics
    )
    
    NavigationBar(
        containerColor = BackgroundDark,
        contentColor = TextPrimary,
        tonalElevation = androidx.compose.ui.unit.dp(0f)
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPrimary,
                    selectedTextColor = AccentPrimary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = GlassSurfaceMedium
                )
            )
        }
    }
}

/**
 * Navigation items configuration
 */
sealed class NavigationItem(
    val screen: Screen,
    val title: String,
    val icon: Int
) {
    object Dashboard : NavigationItem(Screen.Dashboard, "Dashboard", android.R.drawable.ic_menu_info_details)
    object CarrierConfig : NavigationItem(Screen.CarrierConfig, "Config", android.R.drawable.ic_menu_edit)
    object Entitlement : NavigationItem(Screen.Entitlement, "Hooks", android.R.drawable.ic_menu_manage)
    object Diagnostics : NavigationItem(Screen.Diagnostics, "Diagnostics", android.R.drawable.ic_menu_search)
}

