package com.supermarsx.carrierconfig.data.datastore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [AppPreferences] data class defaults and validation.
 *
 * Note: PreferencesManager itself depends on Context.dataStore which requires
 * an Android runtime. Flow-level integration tests belong in androidTest/.
 */
class PreferencesManagerTest {

    @Test
    fun `default AppPreferences has expected values`() {
        val prefs = AppPreferences()

        assertThat(prefs.autoRefresh).isTrue()
        assertThat(prefs.enableNotifications).isFalse()
        assertThat(prefs.theme).isEqualTo("dark")
        assertThat(prefs.glassEffectEnabled).isTrue()
        assertThat(prefs.glassStrength).isEqualTo("medium")
        assertThat(prefs.debugMode).isFalse()
        assertThat(prefs.exportDirectory).isEmpty()
        assertThat(prefs.autoBackup).isFalse()
        assertThat(prefs.backupFrequency).isEqualTo("weekly")
        assertThat(prefs.isFirstRun).isTrue()
    }

    @Test
    fun `AppPreferences copy changes only specified fields`() {
        val original = AppPreferences()
        val modified = original.copy(theme = "light", debugMode = true)

        assertThat(modified.theme).isEqualTo("light")
        assertThat(modified.debugMode).isTrue()
        assertThat(modified.autoRefresh).isTrue()
        assertThat(modified.glassStrength).isEqualTo("medium")
    }

    @Test
    fun `AppPreferences equality`() {
        val a = AppPreferences()
        val b = AppPreferences()
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `AppPreferences with custom export directory`() {
        val prefs = AppPreferences(exportDirectory = "/storage/emulated/0/CCO")
        assertThat(prefs.exportDirectory).isEqualTo("/storage/emulated/0/CCO")
    }

    @Test
    fun `valid theme values`() {
        for (theme in listOf("dark", "light", "system")) {
            val prefs = AppPreferences(theme = theme)
            assertThat(prefs.theme).isEqualTo(theme)
        }
    }

    @Test
    fun `valid glass strength values`() {
        for (strength in listOf("low", "medium", "high")) {
            val prefs = AppPreferences(glassStrength = strength)
            assertThat(prefs.glassStrength).isEqualTo(strength)
        }
    }

    @Test
    fun `valid backup frequency values`() {
        for (freq in listOf("daily", "weekly", "monthly")) {
            val prefs = AppPreferences(backupFrequency = freq)
            assertThat(prefs.backupFrequency).isEqualTo(freq)
        }
    }
}
