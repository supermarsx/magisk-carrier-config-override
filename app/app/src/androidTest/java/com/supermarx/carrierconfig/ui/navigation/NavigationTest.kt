package com.supermarsx.carrierconfig.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supermarsx.carrierconfig.ui.theme.CarrierConfigTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun app_launches_to_dashboard() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Dashboard should be the initial screen
        composeTestRule.onNodeWithText("Dashboard").assertExists()
    }

    @Test
    fun bottom_nav_displays_all_items() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Verify all bottom nav items exist
        composeTestRule.onNodeWithText("Dashboard").assertExists()
        composeTestRule.onNodeWithText("Config").assertExists()
        composeTestRule.onNodeWithText("Entitlement").assertExists()
        composeTestRule.onNodeWithText("Diagnostics").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun navigation_to_carrier_config_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Click Config in bottom nav
        composeTestRule.onNodeWithText("Config").performClick()

        // Should navigate to CarrierConfig screen
        composeTestRule.onNodeWithText("Carrier Configuration").assertExists()
    }

    @Test
    fun navigation_to_diagnostics_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Click Diagnostics in bottom nav
        composeTestRule.onNodeWithText("Diagnostics").performClick()

        // Should navigate to Diagnostics screen
        composeTestRule.onNodeWithText("Logs").assertExists()
        composeTestRule.onNodeWithText("Dumpsys").assertExists()
        composeTestRule.onNodeWithText("Tests").assertExists()
    }

    @Test
    fun navigation_to_settings_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Click Settings in bottom nav
        composeTestRule.onNodeWithText("Settings").performClick()

        // Should navigate to Settings screen
        composeTestRule.onNodeWithText("Auto-refresh dashboard").assertExists()
    }

    @Test
    fun navigation_to_entitlement_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Click Entitlement in bottom nav
        composeTestRule.onNodeWithText("Entitlement").performClick()

        // Should navigate to Entitlement screen
        composeTestRule.onNodeWithText("Runtime Hooks").assertExists()
    }

    @Test
    fun back_navigation_from_settings_to_about() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Navigate to Settings
        composeTestRule.onNodeWithText("Settings").performClick()

        // Click About
        composeTestRule.onNodeWithText("About").performClick()

        // Should navigate to About screen
        composeTestRule.onNodeWithText("About CCO").assertExists()

        // Navigate back
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Should return to Settings
        composeTestRule.onNodeWithText("Auto-refresh dashboard").assertExists()
    }

    @Test
    fun bottom_nav_selection_state_updates() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation()
            }
        }

        // Dashboard should be selected initially
        composeTestRule.onNodeWithText("Dashboard").assertIsSelected()

        // Navigate to Config
        composeTestRule.onNodeWithText("Config").performClick()
        composeTestRule.onNodeWithText("Config").assertIsSelected()

        // Navigate to Diagnostics
        composeTestRule.onNodeWithText("Diagnostics").performClick()
        composeTestRule.onNodeWithText("Diagnostics").assertIsSelected()
    }

    @Test
    fun deep_link_to_diagnostics_works() {
        // This would test deep linking functionality
        // Requires proper deep link setup
        composeTestRule.setContent {
            CarrierConfigTheme {
                AppNavigation(startDestination = Screen.Diagnostics.route)
            }
        }

        composeTestRule.onNodeWithText("Logs").assertExists()
    }
}
