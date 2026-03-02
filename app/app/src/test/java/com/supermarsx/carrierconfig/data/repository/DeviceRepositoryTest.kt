package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import org.junit.Test

/**
 * Unit tests for [DeviceRepository].
 *
 * Tests cover:
 * - Privacy redaction regex (Fix #12: phone numbers and IMSI)
 * - Blocker detection logic
 * - Data model contracts (DeviceInfo, SIMInfo, IMSStatus, WFCUIStatus, WFCBlocker)
 *
 * Root/Shell-dependent methods require androidTest.
 */
class DeviceRepositoryTest {

    /**
     * Create a DeviceRepository for pure-Kotlin method testing.
     * Uses a mock context since redactSensitiveData() and detectBlocker() don't need it.
     */
    private fun createRepository(): DeviceRepository {
        return DeviceRepository(org.mockito.kotlin.mock())
    }

    // =========================================================================
    // Privacy redaction (redactSensitiveData)
    // =========================================================================

    @Test
    fun `redactSensitiveData masks phone number with country code`() {
        val repo = createRepository()
        val input = "Phone: +1-555-123-4567"
        val result = repo.redactSensitiveData(input)
        assertThat(result).doesNotContain("555-123")
        assertThat(result).contains("4567")
    }

    @Test
    fun `redactSensitiveData masks phone number without country code`() {
        val repo = createRepository()
        val input = "Number: 5551234567"
        val result = repo.redactSensitiveData(input)
        assertThat(result).doesNotContain("555123")
        assertThat(result).contains("4567")
    }

    @Test
    fun `redactSensitiveData masks IMSI (15-digit)`() {
        val repo = createRepository()
        val input = "IMSI: 310260123456789"
        val result = repo.redactSensitiveData(input)
        assertThat(result).doesNotContain("31026012345")
        assertThat(result).contains("6789")
    }

    @Test
    fun `redactSensitiveData preserves non-sensitive text`() {
        val repo = createRepository()
        val input = "Model: Galaxy S24, Android 14"
        val result = repo.redactSensitiveData(input)
        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `redactSensitiveData handles empty string`() {
        val repo = createRepository()
        assertThat(repo.redactSensitiveData("")).isEmpty()
    }

    @Test
    fun `redactSensitiveData handles multiple phone numbers`() {
        val repo = createRepository()
        val input = "Primary: +12125551234, Secondary: +14085559876"
        val result = repo.redactSensitiveData(input)
        assertThat(result).doesNotContain("212555")
        assertThat(result).doesNotContain("408555")
        assertThat(result).contains("1234")
        assertThat(result).contains("9876")
    }

    @Test
    fun `redactSensitiveData does not mask short digit sequences`() {
        val repo = createRepository()
        val input = "API 35, SDK 33"
        val result = repo.redactSensitiveData(input)
        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `redactSensitiveData masks phone with dashes and spaces`() {
        val repo = createRepository()
        val input = "Phone: 555-123-4567"
        val result = repo.redactSensitiveData(input)
        assertThat(result).doesNotContain("555-123")
    }

    // =========================================================================
    // Blocker detection (detectBlocker)
    // =========================================================================

    @Test
    fun `detectBlocker returns UNKNOWN when inputs are null`() {
        val repo = createRepository()
        assertThat(repo.detectBlocker(null, null)).isEqualTo(WFCBlocker.UNKNOWN)
    }

    @Test
    fun `detectBlocker returns IMS_NOT_REGISTERED when IMS not registered`() {
        val repo = createRepository()
        val ims = IMSStatus(
            isRegistered = false,
            isVoLTEAvailable = false,
            isVoWiFiAvailable = false,
            registrationState = "NOT_REGISTERED"
        )
        val wfc = WFCUIStatus(
            settingsActivityExists = true,
            pagePopulates = true,
            togglePresent = true
        )
        assertThat(repo.detectBlocker(ims, wfc)).isEqualTo(WFCBlocker.IMS_NOT_REGISTERED)
    }

    @Test
    fun `detectBlocker returns SETTINGS_MISSING when activity absent`() {
        val repo = createRepository()
        val ims = IMSStatus(
            isRegistered = true,
            isVoLTEAvailable = true,
            isVoWiFiAvailable = true,
            registrationState = "REGISTERED_LTE"
        )
        val wfc = WFCUIStatus(
            settingsActivityExists = false,
            pagePopulates = false,
            togglePresent = false
        )
        assertThat(repo.detectBlocker(ims, wfc)).isEqualTo(WFCBlocker.SETTINGS_MISSING)
    }

    @Test
    fun `detectBlocker returns CARRIER_CONFIG_GATE when registered but no VoWiFi`() {
        val repo = createRepository()
        val ims = IMSStatus(
            isRegistered = true,
            isVoLTEAvailable = true,
            isVoWiFiAvailable = false,
            registrationState = "REGISTERED_LTE"
        )
        val wfc = WFCUIStatus(
            settingsActivityExists = true,
            pagePopulates = true,
            togglePresent = false
        )
        assertThat(repo.detectBlocker(ims, wfc)).isEqualTo(WFCBlocker.CARRIER_CONFIG_GATE)
    }

    @Test
    fun `detectBlocker returns CSC_GATE when page does not populate`() {
        val repo = createRepository()
        val ims = IMSStatus(
            isRegistered = true,
            isVoLTEAvailable = true,
            isVoWiFiAvailable = true,
            registrationState = "REGISTERED_WIFI"
        )
        val wfc = WFCUIStatus(
            settingsActivityExists = true,
            pagePopulates = false,
            togglePresent = false
        )
        assertThat(repo.detectBlocker(ims, wfc)).isEqualTo(WFCBlocker.CSC_GATE)
    }

    @Test
    fun `detectBlocker returns NONE when everything is working`() {
        val repo = createRepository()
        val ims = IMSStatus(
            isRegistered = true,
            isVoLTEAvailable = true,
            isVoWiFiAvailable = true,
            registrationState = "REGISTERED_WIFI"
        )
        val wfc = WFCUIStatus(
            settingsActivityExists = true,
            pagePopulates = true,
            togglePresent = true
        )
        assertThat(repo.detectBlocker(ims, wfc)).isEqualTo(WFCBlocker.NONE)
    }

    // =========================================================================
    // Data Models
    // =========================================================================

    @Test
    fun `DeviceInfo carries all fields`() {
        val info = DeviceInfo(
            manufacturer = "samsung",
            model = "SM-S928B",
            buildFingerprint = "samsung/...",
            androidVersion = "Android 14 (API 34)",
            oneUIVersion = "One UI 6.1",
            securityPatch = "2024-01-01",
            isRooted = true
        )
        assertThat(info.manufacturer).isEqualTo("samsung")
        assertThat(info.isRooted).isTrue()
        assertThat(info.oneUIVersion).isNotNull()
    }

    @Test
    fun `DeviceInfo with null oneUIVersion`() {
        val info = DeviceInfo(
            manufacturer = "google",
            model = "Pixel 8",
            buildFingerprint = "google/...",
            androidVersion = "Android 14 (API 34)",
            oneUIVersion = null,
            securityPatch = "2024-02-01",
            isRooted = false
        )
        assertThat(info.oneUIVersion).isNull()
        assertThat(info.isRooted).isFalse()
    }

    @Test
    fun `SIMInfo with redacted ICCID`() {
        val sim = SIMInfo(
            slotIndex = 0,
            carrierName = "T-Mobile",
            mcc = "310",
            mnc = "260",
            iccid = "••••1234",
            isActive = true
        )
        assertThat(sim.iccid).startsWith("••••")
        assertThat(sim.isActive).isTrue()
    }

    @Test
    fun `SIMInfo with null fields`() {
        val sim = SIMInfo(
            slotIndex = 1,
            carrierName = null,
            mcc = null,
            mnc = null,
            iccid = null,
            isActive = false
        )
        assertThat(sim.carrierName).isNull()
        assertThat(sim.isActive).isFalse()
    }

    @Test
    fun `IMSStatus model`() {
        val status = IMSStatus(
            isRegistered = true,
            isVoLTEAvailable = true,
            isVoWiFiAvailable = true,
            registrationState = "REGISTERED_WIFI"
        )
        assertThat(status.isRegistered).isTrue()
        assertThat(status.registrationState).isEqualTo("REGISTERED_WIFI")
    }

    @Test
    fun `WFCUIStatus model`() {
        val status = WFCUIStatus(
            settingsActivityExists = true,
            pagePopulates = true,
            togglePresent = false
        )
        assertThat(status.settingsActivityExists).isTrue()
        assertThat(status.togglePresent).isFalse()
    }

    @Test
    fun `WFCBlocker has 7 values`() {
        assertThat(WFCBlocker.values()).hasLength(7)
    }

    @Test
    fun `WFCBlocker enum values`() {
        assertThat(WFCBlocker.values().map { it.name }).containsExactly(
            "NONE", "IMS_NOT_REGISTERED", "CARRIER_CONFIG_GATE",
            "CSC_GATE", "ENTITLEMENT_GATE", "SETTINGS_MISSING", "UNKNOWN"
        )
    }

    @Test
    fun `DashboardState defaults`() {
        val state = DashboardState()
        assertThat(state.deviceInfo).isNull()
        assertThat(state.simInfo).isEmpty()
        assertThat(state.imsStatus).isNull()
        assertThat(state.wfcUIStatus).isNull()
        assertThat(state.detectedBlocker).isEqualTo(WFCBlocker.UNKNOWN)
        assertThat(state.isLoading).isTrue()
        assertThat(state.error).isNull()
    }

    @Test
    fun `DashboardState with populated data`() {
        val state = DashboardState(
            deviceInfo = DeviceInfo("samsung", "S24", "", "14", null, "", true),
            simInfo = listOf(SIMInfo(0, "TMobile", "310", "260", "••••1234", true)),
            imsStatus = IMSStatus(true, true, true, "REGISTERED_WIFI"),
            wfcUIStatus = WFCUIStatus(true, true, true),
            detectedBlocker = WFCBlocker.NONE,
            isLoading = false,
            error = null
        )
        assertThat(state.isLoading).isFalse()
        assertThat(state.detectedBlocker).isEqualTo(WFCBlocker.NONE)
        assertThat(state.simInfo).hasSize(1)
    }
}
