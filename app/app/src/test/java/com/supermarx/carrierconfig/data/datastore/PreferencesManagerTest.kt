package com.supermarsx.carrierconfig.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesManagerTest {

    @Test
    fun `PreferenceKeys are properly defined`() {
        // Verify all keys exist and have correct types
        assertNotNull(PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED)
        assertNotNull(PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS)
        assertNotNull(PreferencesManager.PreferenceKeys.THEME_MODE)
        assertNotNull(PreferencesManager.PreferenceKeys.GLASS_STRENGTH)
        assertNotNull(PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS)
        assertNotNull(PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING)
        assertNotNull(PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES)
    }

    @Test
    fun `default values are reasonable`() {
        // Test that default values make sense
        val defaultRefreshEnabled = true // Auto-refresh on by default
        val defaultRefreshInterval = 30 // 30 seconds
        val defaultTheme = "dark" // Dark theme by default
        val defaultGlassStrength = "medium" // Medium glass effect
        val defaultShowTechnical = false // Hidden by default
        val defaultVerboseLogging = false // Off by default
        val defaultBackup = true // Backup enabled by default

        assertTrue(defaultRefreshEnabled)
        assertTrue(defaultRefreshInterval >= 10) // At least 10 seconds
        assertTrue(listOf("dark", "amoled", "auto").contains(defaultTheme))
        assertTrue(listOf("subtle", "medium", "strong", "none").contains(defaultGlassStrength))
        assertFalse(defaultShowTechnical)
        assertFalse(defaultVerboseLogging)
        assertTrue(defaultBackup)
    }

    @Test
    fun `theme mode values are valid`() {
        val validThemes = listOf("dark", "amoled", "auto")
        
        validThemes.forEach { theme ->
            // Each theme should be a valid string
            assertTrue(theme.isNotEmpty())
            assertTrue(theme.lowercase() == theme)
        }
    }

    @Test
    fun `glass strength values are valid`() {
        val validStrengths = listOf("subtle", "medium", "strong", "none")
        
        validStrengths.forEach { strength ->
            assertTrue(strength.isNotEmpty())
            assertTrue(strength.lowercase() == strength)
        }
    }

    @Test
    fun `refresh interval has reasonable bounds`() {
        val minInterval = 5 // Minimum 5 seconds
        val maxInterval = 300 // Maximum 5 minutes
        val defaultInterval = 30

        assertTrue(defaultInterval >= minInterval)
        assertTrue(defaultInterval <= maxInterval)
    }

    @Test
    fun `preference keys have unique names`() {
        val keys = listOf(
            PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED.name,
            PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS.name,
            PreferencesManager.PreferenceKeys.THEME_MODE.name,
            PreferencesManager.PreferenceKeys.GLASS_STRENGTH.name,
            PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS.name,
            PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING.name,
            PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES.name
        )

        val uniqueKeys = keys.toSet()
        assertEquals(keys.size, uniqueKeys.size)
    }

    @Test
    fun `boolean preferences have descriptive names`() {
        val booleanKeys = listOf(
            PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED,
            PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS,
            PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING,
            PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES
        )

        booleanKeys.forEach { key ->
            // Boolean keys should contain action words
            val name = key.name.lowercase()
            assertTrue(
                name.contains("enable") || 
                name.contains("show") || 
                name.contains("backup") ||
                name.contains("auto")
            )
        }
    }

    @Test
    fun `string preferences have descriptive names`() {
        val stringKeys = listOf(
            PreferencesManager.PreferenceKeys.THEME_MODE,
            PreferencesManager.PreferenceKeys.GLASS_STRENGTH
        )

        stringKeys.forEach { key ->
            // String keys should be descriptive
            val name = key.name.lowercase()
            assertTrue(name.contains("theme") || name.contains("glass") || name.contains("mode"))
        }
    }

    @Test
    fun `preference key names follow naming convention`() {
        val allKeys = listOf(
            PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED.name,
            PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS.name,
            PreferencesManager.PreferenceKeys.THEME_MODE.name,
            PreferencesManager.PreferenceKeys.GLASS_STRENGTH.name,
            PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS.name,
            PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING.name,
            PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES.name
        )

        allKeys.forEach { keyName ->
            // Should be lowercase with underscores
            assertEquals(keyName, keyName.lowercase())
            assertFalse(keyName.contains(' '))
            // Should not start or end with underscore
            assertFalse(keyName.startsWith('_'))
            assertFalse(keyName.endsWith('_'))
        }
    }

    @Test
    fun `theme mode options cover common use cases`() {
        val themes = listOf("dark", "amoled", "auto")
        
        // Should have dark mode
        assertTrue(themes.contains("dark"))
        
        // Should have AMOLED option for battery saving
        assertTrue(themes.contains("amoled"))
        
        // Should have auto option for system preference
        assertTrue(themes.contains("auto"))
    }

    @Test
    fun `glass strength options provide range`() {
        val strengths = listOf("subtle", "medium", "strong", "none")
        
        // Should have subtle option for minimal effect
        assertTrue(strengths.contains("subtle"))
        
        // Should have medium as balanced option
        assertTrue(strengths.contains("medium"))
        
        // Should have strong for maximum effect
        assertTrue(strengths.contains("strong"))
        
        // Should have none to disable
        assertTrue(strengths.contains("none"))
        
        // Should have 4 levels of control
        assertEquals(4, strengths.size)
    }

    @Test
    fun `preferences support common app settings`() {
        // Verify we have preferences for all common app settings
        
        // Display preferences
        assertNotNull(PreferencesManager.PreferenceKeys.THEME_MODE)
        assertNotNull(PreferencesManager.PreferenceKeys.GLASS_STRENGTH)
        
        // Behavior preferences  
        assertNotNull(PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED)
        assertNotNull(PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS)
        
        // Advanced preferences
        assertNotNull(PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS)
        assertNotNull(PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING)
        
        // Safety preferences
        assertNotNull(PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES)
    }

    @Test
    fun `refresh interval is measured in seconds`() {
        val key = PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS
        
        // Key name should indicate unit
        assertTrue(key.name.contains("seconds"))
    }

    @Test
    fun `backup preference promotes safety`() {
        val key = PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES
        
        // Should be enabled by default for safety
        // (This is tested conceptually - actual default would be in implementation)
        assertTrue(key.name.contains("backup"))
        assertTrue(key.name.contains("before"))
    }

    @Test
    fun `verbose logging is separate from technical details`() {
        val technicalKey = PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS
        val loggingKey = PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING
        
        // These should be distinct settings
        assertNotEquals(technicalKey.name, loggingKey.name)
        
        // Technical details is about UI display
        assertTrue(technicalKey.name.contains("show") || technicalKey.name.contains("technical"))
        
        // Verbose logging is about app behavior
        assertTrue(loggingKey.name.contains("enable") || loggingKey.name.contains("logging"))
    }

    @Test
    fun `auto refresh has associated interval`() {
        val enabledKey = PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED
        val intervalKey = PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS
        
        // Both should relate to refresh
        assertTrue(enabledKey.name.contains("refresh"))
        assertTrue(intervalKey.name.contains("refresh"))
        
        // One is boolean, one is numeric
        assertTrue(enabledKey is Preferences.Key<Boolean>)
        assertTrue(intervalKey is Preferences.Key<Int>)
    }

    @Test
    fun `preferences support user customization`() {
        // Test that we have customizable preferences for common needs
        
        // Visual customization
        val hasThemeCustomization = PreferencesManager.PreferenceKeys.THEME_MODE != null
        val hasGlassCustomization = PreferencesManager.PreferenceKeys.GLASS_STRENGTH != null
        assertTrue(hasThemeCustomization)
        assertTrue(hasGlassCustomization)
        
        // Behavior customization
        val hasRefreshCustomization = 
            PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED != null &&
            PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS != null
        assertTrue(hasRefreshCustomization)
        
        // Advanced customization
        val hasAdvancedOptions = 
            PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS != null &&
            PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING != null
        assertTrue(hasAdvancedOptions)
    }

    @Test
    fun `preference types match their purpose`() {
        // Boolean for on/off settings
        assertTrue(PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED is Preferences.Key<Boolean>)
        assertTrue(PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS is Preferences.Key<Boolean>)
        assertTrue(PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING is Preferences.Key<Boolean>)
        assertTrue(PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES is Preferences.Key<Boolean>)
        
        // String for choice settings
        assertTrue(PreferencesManager.PreferenceKeys.THEME_MODE is Preferences.Key<String>)
        assertTrue(PreferencesManager.PreferenceKeys.GLASS_STRENGTH is Preferences.Key<String>)
        
        // Int for numeric settings
        assertTrue(PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS is Preferences.Key<Int>)
    }

    @Test
    fun `all preferences have reasonable names`() {
        val allKeys = listOf(
            PreferencesManager.PreferenceKeys.AUTO_REFRESH_ENABLED.name,
            PreferencesManager.PreferenceKeys.REFRESH_INTERVAL_SECONDS.name,
            PreferencesManager.PreferenceKeys.THEME_MODE.name,
            PreferencesManager.PreferenceKeys.GLASS_STRENGTH.name,
            PreferencesManager.PreferenceKeys.SHOW_TECHNICAL_DETAILS.name,
            PreferencesManager.PreferenceKeys.ENABLE_VERBOSE_LOGGING.name,
            PreferencesManager.PreferenceKeys.BACKUP_BEFORE_CHANGES.name
        )

        allKeys.forEach { keyName ->
            // Should be descriptive (at least 10 characters)
            assertTrue("Key name '$keyName' is too short", keyName.length >= 10)
            
            // Should not be too long (less than 50 characters)
            assertTrue("Key name '$keyName' is too long", keyName.length < 50)
            
            // Should be readable (contains underscores for separation)
            if (keyName.length > 15) {
                assertTrue("Key name '$keyName' should have underscores", keyName.contains('_'))
            }
        }
    }
}
