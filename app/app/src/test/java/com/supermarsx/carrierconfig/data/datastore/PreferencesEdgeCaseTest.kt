package com.supermarsx.carrierconfig.data.datastore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Edge-case tests for [AppPreferences] data class — boundary values,
 * unusual inputs, and defensive validation.
 */
class PreferencesEdgeCaseTest {

    // =========================================================================
    // Theme boundary
    // =========================================================================

    @Test
    fun `AppPreferences with empty theme`() {
        val prefs = AppPreferences(theme = "")
        assertThat(prefs.theme).isEmpty()
    }

    @Test
    fun `AppPreferences with unknown theme value`() {
        val prefs = AppPreferences(theme = "neon_green")
        assertThat(prefs.theme).isEqualTo("neon_green")
    }

    @Test
    fun `AppPreferences with very long theme string`() {
        val long = "x".repeat(1000)
        val prefs = AppPreferences(theme = long)
        assertThat(prefs.theme).hasLength(1000)
    }

    // =========================================================================
    // Glass strength boundary
    // =========================================================================

    @Test
    fun `AppPreferences with empty glassStrength`() {
        val prefs = AppPreferences(glassStrength = "")
        assertThat(prefs.glassStrength).isEmpty()
    }

    @Test
    fun `AppPreferences with unknown glassStrength`() {
        val prefs = AppPreferences(glassStrength = "maximum_overdrive")
        assertThat(prefs.glassStrength).isEqualTo("maximum_overdrive")
    }

    // =========================================================================
    // Backup frequency boundary
    // =========================================================================

    @Test
    fun `AppPreferences with empty backupFrequency`() {
        val prefs = AppPreferences(backupFrequency = "")
        assertThat(prefs.backupFrequency).isEmpty()
    }

    @Test
    fun `AppPreferences with unknown backupFrequency`() {
        val prefs = AppPreferences(backupFrequency = "every_5_seconds")
        assertThat(prefs.backupFrequency).isEqualTo("every_5_seconds")
    }

    // =========================================================================
    // Export directory edge cases
    // =========================================================================

    @Test
    fun `AppPreferences with path containing spaces`() {
        val prefs = AppPreferences(exportDirectory = "/storage/My Documents/CCO")
        assertThat(prefs.exportDirectory).contains("My Documents")
    }

    @Test
    fun `AppPreferences with SAF content URI`() {
        val uri = "content://com.android.externalstorage.documents/tree/primary%3ADownload"
        val prefs = AppPreferences(exportDirectory = uri)
        assertThat(prefs.exportDirectory).startsWith("content://")
    }

    @Test
    fun `AppPreferences with unicode path`() {
        val prefs = AppPreferences(exportDirectory = "/storage/日本語/CCO")
        assertThat(prefs.exportDirectory).contains("日本語")
    }

    // =========================================================================
    // Boolean field combinations
    // =========================================================================

    @Test
    fun `AppPreferences all booleans true`() {
        val prefs = AppPreferences(
            autoRefresh = true,
            enableNotifications = true,
            glassEffectEnabled = true,
            debugMode = true,
            autoBackup = true,
            isFirstRun = true
        )
        assertThat(prefs.autoRefresh).isTrue()
        assertThat(prefs.enableNotifications).isTrue()
        assertThat(prefs.debugMode).isTrue()
        assertThat(prefs.autoBackup).isTrue()
    }

    @Test
    fun `AppPreferences all booleans false`() {
        val prefs = AppPreferences(
            autoRefresh = false,
            enableNotifications = false,
            glassEffectEnabled = false,
            debugMode = false,
            autoBackup = false,
            isFirstRun = false
        )
        assertThat(prefs.autoRefresh).isFalse()
        assertThat(prefs.glassEffectEnabled).isFalse()
        assertThat(prefs.isFirstRun).isFalse()
    }

    // =========================================================================
    // toString / equality contract
    // =========================================================================

    @Test
    fun `AppPreferences toString contains all field values`() {
        val prefs = AppPreferences(theme = "light", debugMode = true)
        val str = prefs.toString()
        assertThat(str).contains("theme=light")
        assertThat(str).contains("debugMode=true")
    }

    @Test
    fun `AppPreferences with different values are not equal`() {
        val a = AppPreferences(theme = "dark")
        val b = AppPreferences(theme = "light")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `AppPreferences hashCode differs for different objects`() {
        val a = AppPreferences(debugMode = true)
        val b = AppPreferences(debugMode = false)
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
    }

    @Test
    fun `AppPreferences destructuring`() {
        val prefs = AppPreferences(theme = "system", isFirstRun = false)
        val (
            autoRefresh, enableNotifications, theme, glassEffectEnabled,
            glassStrength, debugMode, exportDirectory, autoBackup,
            backupFrequency, isFirstRun
        ) = prefs
        assertThat(theme).isEqualTo("system")
        assertThat(isFirstRun).isFalse()
        assertThat(autoRefresh).isTrue()
    }
}
