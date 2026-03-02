package com.supermarsx.carrierconfig.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.supermarsx.carrierconfig.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented navigation tests using the real Hilt-injected Activity.
 *
 * Uses [HiltAndroidTest] so that hiltViewModel() calls inside screens resolve properly.
 */
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun app_launches_to_dashboard() {
        composeTestRule.onNodeWithText("CCO Dashboard").assertExists()
    }

    @Test
    fun bottom_nav_displays_all_items() {
        composeTestRule.onNodeWithText("Dashboard").assertExists()
        composeTestRule.onNodeWithText("Config").assertExists()
        composeTestRule.onNodeWithText("Entitlement").assertExists()
        composeTestRule.onNodeWithText("Diagnostics").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun navigation_to_carrier_config_works() {
        composeTestRule.onNodeWithText("Config").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Carrier Configuration").assertExists()
    }

    @Test
    fun navigation_to_diagnostics_works() {
        composeTestRule.onNodeWithText("Diagnostics").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Advanced Diagnostics").assertExists()
    }

    @Test
    fun navigation_to_settings_works() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun navigation_to_entitlement_works() {
        composeTestRule.onNodeWithText("Entitlement").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Runtime Hooks").assertExists()
    }
}
