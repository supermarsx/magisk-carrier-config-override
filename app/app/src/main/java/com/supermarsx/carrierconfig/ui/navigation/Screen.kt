package com.supermarsx.carrierconfig.ui.navigation

/**
 * Navigation routes for CCO app screens
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CarrierConfig : Screen("carrier_config")
    object Entitlement : Screen("entitlement")
    object Diagnostics : Screen("diagnostics")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Licenses : Screen("licenses")
}
