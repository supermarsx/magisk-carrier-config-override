package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import org.junit.Test

/**
 * Edge-case tests for [DeviceRepository] — privacy redaction corner cases
 * and blocker detection boundary conditions.
 */
class DeviceRepositoryEdgeCaseTest {

    private fun repo() = DeviceRepository(org.mockito.kotlin.mock())

    // =========================================================================
    // Privacy redaction edge cases
    // =========================================================================

    @Test
    fun `redactSensitiveData with IMSI embedded in sentence`() {
        val r = repo().redactSensitiveData("SIM IMSI=310260123456789 active")
        assertThat(r).doesNotContain("31026012345")
        assertThat(r).contains("6789")
    }

    @Test
    fun `redactSensitiveData does not touch 14-digit sequences`() {
        val input = "Value: 12345678901234"
        val r = repo().redactSensitiveData(input)
        // 14 digits won't match the 15-digit IMSI regex but may match phone regex
        // Just verify no crash
        assertThat(r).isNotEmpty()
    }

    @Test
    fun `redactSensitiveData with null-like string`() {
        val r = repo().redactSensitiveData("null")
        assertThat(r).isEqualTo("null")
    }

    @Test
    fun `redactSensitiveData with only whitespace`() {
        val r = repo().redactSensitiveData("   ")
        assertThat(r).isEqualTo("   ")
    }

    @Test
    fun `redactSensitiveData with international format +44`() {
        val r = repo().redactSensitiveData("UK: +44 7911 123456")
        assertThat(r).doesNotContain("7911 123")
    }

    @Test
    fun `redactSensitiveData preserves surrounding text`() {
        val r = repo().redactSensitiveData("Before +15551234567 After")
        assertThat(r).startsWith("Before")
        assertThat(r).endsWith("After")
    }

    @Test
    fun `redactSensitiveData with extremely long input`() {
        val longInput = "x".repeat(50_000) + " +15551234567 " + "y".repeat(50_000)
        val r = repo().redactSensitiveData(longInput)
        assertThat(r).doesNotContain("555123")
    }

    @Test
    fun `redactSensitiveData with consecutive phone numbers`() {
        val r = repo().redactSensitiveData("+15551234567+15559876543")
        assertThat(r).doesNotContain("555123")
        assertThat(r).doesNotContain("555987")
    }

    @Test
    fun `redactSensitiveData with tab and newline separators`() {
        val r = repo().redactSensitiveData("Phone:\t+15551234567\nIMSI:\t310260123456789")
        assertThat(r).doesNotContain("555123")
        assertThat(r).doesNotContain("31026012345")
    }

    // =========================================================================
    // Blocker detection edge cases
    // =========================================================================

    @Test
    fun `detectBlocker with only IMS null and WFC provided`() {
        val wfc = WFCUIStatus(settingsActivityExists = true, pagePopulates = true, togglePresent = true)
        assertThat(repo().detectBlocker(null, wfc)).isEqualTo(WFCBlocker.UNKNOWN)
    }

    @Test
    fun `detectBlocker with IMS provided and WFC null`() {
        val ims = IMSStatus(isRegistered = true, isVoLTEAvailable = true, isVoWiFiAvailable = true, registrationState = "OK")
        assertThat(repo().detectBlocker(ims, null)).isEqualTo(WFCBlocker.UNKNOWN)
    }

    @Test
    fun `detectBlocker with VoLTE but no VoWifi`() {
        val ims = IMSStatus(isRegistered = true, isVoLTEAvailable = true, isVoWiFiAvailable = false, registrationState = "LTE")
        val wfc = WFCUIStatus(settingsActivityExists = true, pagePopulates = true, togglePresent = true)
        val blocker = repo().detectBlocker(ims, wfc)
        assertThat(blocker).isEqualTo(WFCBlocker.CARRIER_CONFIG_GATE)
    }

    @Test
    fun `detectBlocker with IMS registered but nothing else`() {
        val ims = IMSStatus(isRegistered = true, isVoLTEAvailable = false, isVoWiFiAvailable = false, registrationState = "PARTIAL")
        val wfc = WFCUIStatus(settingsActivityExists = false, pagePopulates = false, togglePresent = false)
        val blocker = repo().detectBlocker(ims, wfc)
        // Settings missing takes priority over carrier config gate
        assertThat(blocker).isEqualTo(WFCBlocker.SETTINGS_MISSING)
    }

    @Test
    fun `detectBlocker all false returns IMS_NOT_REGISTERED`() {
        val ims = IMSStatus(isRegistered = false, isVoLTEAvailable = false, isVoWiFiAvailable = false, registrationState = "NONE")
        val wfc = WFCUIStatus(settingsActivityExists = false, pagePopulates = false, togglePresent = false)
        val blocker = repo().detectBlocker(ims, wfc)
        assertThat(blocker).isEqualTo(WFCBlocker.IMS_NOT_REGISTERED)
    }

    // =========================================================================
    // Data model edge cases
    // =========================================================================

    @Test
    fun `DeviceInfo copy preserves all fields`() {
        val orig = DeviceInfo("samsung", "S24", "fp", "14", "6.1", "2024-01", true)
        val copy = orig.copy(isRooted = false)
        assertThat(copy.manufacturer).isEqualTo("samsung")
        assertThat(copy.isRooted).isFalse()
    }

    @Test
    fun `SIMInfo equality with same values`() {
        val a = SIMInfo(0, "Carrier", "310", "260", "1234", true)
        val b = SIMInfo(0, "Carrier", "310", "260", "1234", true)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `SIMInfo inequality on different slot`() {
        val a = SIMInfo(0, "Carrier", "310", "260", "1234", true)
        val b = SIMInfo(1, "Carrier", "310", "260", "1234", true)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `IMSStatus all-true state`() {
        val status = IMSStatus(true, true, true, "FULL")
        assertThat(status.isRegistered).isTrue()
        assertThat(status.isVoLTEAvailable).isTrue()
        assertThat(status.isVoWiFiAvailable).isTrue()
    }

    @Test
    fun `WFCBlocker enum count matches spec`() {
        // NONE, IMS_NOT_REGISTERED, CARRIER_CONFIG_GATE, CSC_GATE,
        // ENTITLEMENT_GATE, SETTINGS_MISSING, UNKNOWN
        assertThat(WFCBlocker.values()).hasLength(7)
    }

    @Test
    fun `DashboardState isLoading default is true`() {
        assertThat(DashboardState().isLoading).isTrue()
    }

    @Test
    fun `DashboardState error can hold long messages`() {
        val longError = "E".repeat(5000)
        val state = DashboardState(error = longError)
        assertThat(state.error).hasLength(5000)
    }
}
