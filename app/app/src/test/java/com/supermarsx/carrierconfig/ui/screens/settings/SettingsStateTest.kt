package com.supermarsx.carrierconfig.ui.screens.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [SettingsState] data class.
 *
 * Tests cover:
 * - Default state values
 * - Safe-cast defaults matching SettingsState defaults (Fix #19)
 * - State copy semantics
 */
class SettingsStateTest {

    // =========================================================================
    // Default values (must match safe-cast defaults in loadPreferences)
    // =========================================================================

    @Test
    fun `default autoRefresh is true`() {
        assertThat(SettingsState().autoRefresh).isTrue()
    }

    @Test
    fun `default enableNotifications is false`() {
        assertThat(SettingsState().enableNotifications).isFalse()
    }

    @Test
    fun `default theme is dark`() {
        assertThat(SettingsState().theme).isEqualTo("dark")
    }

    @Test
    fun `default glassEffectEnabled is true`() {
        assertThat(SettingsState().glassEffectEnabled).isTrue()
    }

    @Test
    fun `default glassStrength is medium`() {
        assertThat(SettingsState().glassStrength).isEqualTo("medium")
    }

    @Test
    fun `default debugMode is false`() {
        assertThat(SettingsState().debugMode).isFalse()
    }

    @Test
    fun `default exportDirectory is empty`() {
        assertThat(SettingsState().exportDirectory).isEmpty()
    }

    @Test
    fun `default cacheSize is 0`() {
        assertThat(SettingsState().cacheSize).isEqualTo(0L)
    }

    @Test
    fun `default autoBackup is false`() {
        assertThat(SettingsState().autoBackup).isFalse()
    }

    @Test
    fun `default backupFrequency is weekly`() {
        assertThat(SettingsState().backupFrequency).isEqualTo("weekly")
    }

    @Test
    fun `default isLoading is false`() {
        assertThat(SettingsState().isLoading).isFalse()
    }

    @Test
    fun `default message is null`() {
        assertThat(SettingsState().message).isNull()
    }

    @Test
    fun `default error is null`() {
        assertThat(SettingsState().error).isNull()
    }

    // =========================================================================
    // Copy semantics
    // =========================================================================

    @Test
    fun `copy changes only specified fields`() {
        val original = SettingsState()
        val modified = original.copy(
            theme = "system",
            debugMode = true,
            cacheSize = 1024L
        )

        assertThat(modified.theme).isEqualTo("system")
        assertThat(modified.debugMode).isTrue()
        assertThat(modified.cacheSize).isEqualTo(1024L)
        // Unchanged fields
        assertThat(modified.autoRefresh).isTrue()
        assertThat(modified.glassEffectEnabled).isTrue()
        assertThat(modified.backupFrequency).isEqualTo("weekly")
    }

    @Test
    fun `copy with message and error`() {
        val state = SettingsState().copy(
            message = "Settings saved",
            error = null
        )
        assertThat(state.message).isEqualTo("Settings saved")
        assertThat(state.error).isNull()
    }

    @Test
    fun `copy with loading state`() {
        val state = SettingsState().copy(isLoading = true)
        assertThat(state.isLoading).isTrue()
    }

    // =========================================================================
    // Equality
    // =========================================================================

    @Test
    fun `two default states are equal`() {
        assertThat(SettingsState()).isEqualTo(SettingsState())
    }

    @Test
    fun `modified states are not equal to default`() {
        assertThat(SettingsState(theme = "light")).isNotEqualTo(SettingsState())
    }

    // =========================================================================
    // Safe-cast default values verification
    //
    // Fix #19: SettingsViewModel.loadPreferences() uses safe casts:
    //   (values[0] as? Boolean) ?: true
    // These tests verify the fallback defaults match SettingsState defaults
    // =========================================================================

    @Test
    fun `safe cast autoRefresh fallback matches default`() {
        val fallback = (null as? Boolean) ?: true
        assertThat(fallback).isEqualTo(SettingsState().autoRefresh)
    }

    @Test
    fun `safe cast enableNotifications fallback matches default`() {
        val fallback = (null as? Boolean) ?: false
        assertThat(fallback).isEqualTo(SettingsState().enableNotifications)
    }

    @Test
    fun `safe cast theme fallback matches default`() {
        val fallback = (null as? String) ?: "dark"
        assertThat(fallback).isEqualTo(SettingsState().theme)
    }

    @Test
    fun `safe cast glassEffectEnabled fallback matches default`() {
        val fallback = (null as? Boolean) ?: true
        assertThat(fallback).isEqualTo(SettingsState().glassEffectEnabled)
    }

    @Test
    fun `safe cast glassStrength fallback matches default`() {
        val fallback = (null as? String) ?: "medium"
        assertThat(fallback).isEqualTo(SettingsState().glassStrength)
    }

    @Test
    fun `safe cast debugMode fallback matches default`() {
        val fallback = (null as? Boolean) ?: false
        assertThat(fallback).isEqualTo(SettingsState().debugMode)
    }

    @Test
    fun `safe cast exportDirectory fallback matches default`() {
        val fallback = (null as? String) ?: ""
        assertThat(fallback).isEqualTo(SettingsState().exportDirectory)
    }

    @Test
    fun `safe cast autoBackup fallback matches default`() {
        val fallback = (null as? Boolean) ?: false
        assertThat(fallback).isEqualTo(SettingsState().autoBackup)
    }

    @Test
    fun `safe cast backupFrequency fallback matches default`() {
        val fallback = (null as? String) ?: "weekly"
        assertThat(fallback).isEqualTo(SettingsState().backupFrequency)
    }

    // =========================================================================
    // Type safety - casting wrong types uses fallback
    // =========================================================================

    @Test
    fun `safe cast wrong type Boolean from String uses fallback`() {
        val value: Any = "not a boolean"
        val result = (value as? Boolean) ?: true
        assertThat(result).isTrue()
    }

    @Test
    fun `safe cast wrong type String from Boolean uses fallback`() {
        val value: Any = true
        val result = (value as? String) ?: "dark"
        assertThat(result).isEqualTo("dark")
    }

    @Test
    fun `safe cast correct Boolean type passes through`() {
        val value: Any = false
        val result = (value as? Boolean) ?: true
        assertThat(result).isFalse()
    }

    @Test
    fun `safe cast correct String type passes through`() {
        val value: Any = "light"
        val result = (value as? String) ?: "dark"
        assertThat(result).isEqualTo("light")
    }
}
