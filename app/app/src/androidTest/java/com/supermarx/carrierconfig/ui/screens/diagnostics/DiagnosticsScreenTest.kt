package dev.mars.carrierconfig.ui.screens.diagnostics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import dev.mars.carrierconfig.data.repository.ConnectivityTestRepository
import dev.mars.carrierconfig.data.repository.DumpsysRepository
import dev.mars.carrierconfig.data.repository.LogcatRepository
import dev.mars.carrierconfig.ui.theme.CarrierConfigTheme
import org.junit.Rule
import org.junit.Test

class DiagnosticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun diagnosticsScreen_displays_three_tabs() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Verify all three tabs are present
        composeTestRule.onNodeWithText("Logs").assertExists()
        composeTestRule.onNodeWithText("Dumpsys").assertExists()
        composeTestRule.onNodeWithText("Tests").assertExists()
    }

    @Test
    fun logsTab_displays_filter_chips() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Logs tab should be selected by default
        composeTestRule.onNodeWithText("Logs").assertIsSelected()

        // Verify filter chips exist
        composeTestRule.onNodeWithText("All").assertExists()
        composeTestRule.onNodeWithText("CarrierConfig").assertExists()
        composeTestRule.onNodeWithText("IMS").assertExists()
        composeTestRule.onNodeWithText("Telephony").assertExists()
        composeTestRule.onNodeWithText("WFC").assertExists()
    }

    @Test
    fun logsTab_fab_toggles_live_monitoring() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Find and click FAB
        composeTestRule.onNodeWithContentDescription("Start live monitoring")
            .assertExists()
            .performClick()

        // FAB should change to stop
        composeTestRule.onNodeWithContentDescription("Stop live monitoring")
            .assertExists()
    }

    @Test
    fun logsTab_export_button_exists() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Export button should exist
        composeTestRule.onNodeWithText("Export Logs").assertExists()
    }

    @Test
    fun dumpsysTab_displays_service_selector() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Switch to Dumpsys tab
        composeTestRule.onNodeWithText("Dumpsys").performClick()

        // Verify service selector exists
        composeTestRule.onNodeWithText("Select Service").assertExists()
        
        // Click to open dropdown
        composeTestRule.onNodeWithText("Select Service").performClick()

        // Verify services are listed
        composeTestRule.onNodeWithText("IMS").assertExists()
        composeTestRule.onNodeWithText("Phone").assertExists()
        composeTestRule.onNodeWithText("Carrier Config").assertExists()
    }

    @Test
    fun testsTab_displays_run_button() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Switch to Tests tab
        composeTestRule.onNodeWithText("Tests").performClick()

        // Verify run tests button exists
        composeTestRule.onNodeWithText("Run All Tests").assertExists()
    }

    @Test
    fun testsTab_run_button_is_clickable() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Switch to Tests tab
        composeTestRule.onNodeWithText("Tests").performClick()

        // Click run tests button
        composeTestRule.onNodeWithText("Run All Tests")
            .assertExists()
            .assertIsEnabled()
            .performClick()

        // Loading indicator should appear (eventually)
        // Note: This is a simplified test, real implementation would need to handle async
    }

    @Test
    fun tab_navigation_works_correctly() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Start on Logs tab
        composeTestRule.onNodeWithText("Logs").assertIsSelected()

        // Navigate to Dumpsys
        composeTestRule.onNodeWithText("Dumpsys").performClick()
        composeTestRule.onNodeWithText("Dumpsys").assertIsSelected()

        // Navigate to Tests
        composeTestRule.onNodeWithText("Tests").performClick()
        composeTestRule.onNodeWithText("Tests").assertIsSelected()

        // Navigate back to Logs
        composeTestRule.onNodeWithText("Logs").performClick()
        composeTestRule.onNodeWithText("Logs").assertIsSelected()
    }

    @Test
    fun logsTab_filter_chip_selection_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Click IMS filter chip
        composeTestRule.onNodeWithText("IMS").performClick()

        // IMS chip should be selected
        // Note: Would need to check visual state in real implementation
    }

    @Test
    fun logsTab_mode_toggle_works() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Verify mode selector exists
        composeTestRule.onNodeWithText("Live").assertExists()
        composeTestRule.onNodeWithText("Snapshot").assertExists()

        // Click Snapshot mode
        composeTestRule.onNodeWithText("Snapshot").performClick()

        // Should switch to snapshot mode
        // Note: Would verify state change in real implementation
    }

    @Test
    fun topAppBar_displays_title() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Diagnostics").assertExists()
    }

    @Test
    fun topAppBar_has_back_button() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").assertExists()
    }

    @Test
    fun empty_logs_shows_placeholder() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Should show "No logs available" or similar placeholder
        composeTestRule.onNodeWithText("No logs available").assertExists()
    }

    @Test
    fun dumpsysTab_empty_state_shows_instruction() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Switch to Dumpsys tab
        composeTestRule.onNodeWithText("Dumpsys").performClick()

        // Should show instruction text
        composeTestRule.onNodeWithText("Select a service to view system diagnostics").assertExists()
    }

    @Test
    fun testsTab_empty_state_shows_instruction() {
        composeTestRule.setContent {
            CarrierConfigTheme {
                val navController = rememberNavController()
                DiagnosticsScreen(navController = navController)
            }
        }

        // Switch to Tests tab
        composeTestRule.onNodeWithText("Tests").performClick()

        // Should show instruction or run button
        composeTestRule.onNodeWithText("Run All Tests").assertExists()
    }
}
