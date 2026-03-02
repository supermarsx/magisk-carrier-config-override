package com.supermarsx.carrierconfig.ui.screens.diagnostics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.supermarsx.carrierconfig.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for the Diagnostics screen.
 *
 * Uses the real Hilt-injected Activity so hiltViewModel() resolves.
 * Tests navigate to the Diagnostics tab first, then interact with the screen.
 */
@HiltAndroidTest
class DiagnosticsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        // Navigate to Diagnostics screen
        composeTestRule.onNodeWithText("Diagnostics").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun diagnosticsScreen_displays_title() {
        composeTestRule.onNodeWithText("Advanced Diagnostics").assertExists()
    }

    @Test
    fun diagnosticsScreen_displays_tabs() {
        composeTestRule.onNodeWithText("Logs").assertExists()
        composeTestRule.onNodeWithText("Dumpsys").assertExists()
        composeTestRule.onNodeWithText("Tests").assertExists()
    }

    @Test
    fun dumpsysTab_navigation() {
        composeTestRule.onNodeWithText("Dumpsys").performClick()
        composeTestRule.waitForIdle()
        // Dumpsys tab content should now be visible
        composeTestRule.onNodeWithText("Dumpsys").assertIsSelected()
    }

    @Test
    fun testsTab_navigation() {
        composeTestRule.onNodeWithText("Tests").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Tests").assertIsSelected()
    }

    @Test
    fun tab_switching_back_to_logs() {
        composeTestRule.onNodeWithText("Tests").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Logs").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Logs").assertIsSelected()
    }

    @Test
    fun exportButton_exists_in_toolbar() {
        composeTestRule.onNodeWithContentDescription("Export").assertExists()
    }
}
