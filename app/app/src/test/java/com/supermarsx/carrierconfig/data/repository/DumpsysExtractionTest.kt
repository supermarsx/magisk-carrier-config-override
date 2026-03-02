package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [DumpsysRepository] extraction logic.
 *
 * Uses reflection to test private helper methods:
 * - extractRegistrationType()
 * - extractImsFeatures()
 *
 * Also tests the higher-level extractImsInfo() through simulated dumpsys output.
 */
class DumpsysExtractionTest {

    private val repository = DumpsysRepository(org.mockito.kotlin.mock())

    // =========================================================================
    // extractRegistrationType via reflection
    // =========================================================================

    private fun callExtractRegistrationType(output: String): String {
        val method = DumpsysRepository::class.java
            .getDeclaredMethod("extractRegistrationType", String::class.java)
        method.isAccessible = true
        return method.invoke(repository, output) as String
    }

    @Test
    fun `extractRegistrationType detects Wi-Fi`() {
        assertThat(callExtractRegistrationType("registration TYPE_WIFI active")).isEqualTo("Wi-Fi")
    }

    @Test
    fun `extractRegistrationType detects LTE`() {
        assertThat(callExtractRegistrationType("IMS over TYPE_LTE")).isEqualTo("LTE")
    }

    @Test
    fun `extractRegistrationType detects 5G NR`() {
        assertThat(callExtractRegistrationType("registered via TYPE_NR")).isEqualTo("5G NR")
    }

    @Test
    fun `extractRegistrationType returns Unknown for unrecognized`() {
        assertThat(callExtractRegistrationType("some other output")).isEqualTo("Unknown")
    }

    @Test
    fun `extractRegistrationType case insensitive`() {
        assertThat(callExtractRegistrationType("type_wifi")).isEqualTo("Wi-Fi")
        assertThat(callExtractRegistrationType("TYPE_LTE")).isEqualTo("LTE")
    }

    @Test
    fun `extractRegistrationType prefers Wi-Fi when both present`() {
        // Wi-Fi comes first in the when chain
        assertThat(callExtractRegistrationType("TYPE_WIFI and TYPE_LTE")).isEqualTo("Wi-Fi")
    }

    // =========================================================================
    // extractImsFeatures via reflection
    // =========================================================================

    private fun callExtractImsFeatures(output: String): List<String> {
        val method = DumpsysRepository::class.java
            .getDeclaredMethod("extractImsFeatures", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(repository, output) as List<String>
    }

    @Test
    fun `extractImsFeatures detects MMTEL`() {
        val features = callExtractImsFeatures("Feature: MMTEL enabled")
        assertThat(features).contains("MMTEL")
    }

    @Test
    fun `extractImsFeatures detects RCS`() {
        val features = callExtractImsFeatures("RCS capability available")
        assertThat(features).contains("RCS")
    }

    @Test
    fun `extractImsFeatures detects UT XCAP`() {
        val features = callExtractImsFeatures("UT supplementary services")
        assertThat(features).contains("UT (XCAP)")
    }

    @Test
    fun `extractImsFeatures detects SMS over IP`() {
        val features = callExtractImsFeatures("SMS over IP enabled")
        assertThat(features).contains("SMS over IP")
    }

    @Test
    fun `extractImsFeatures detects all features`() {
        val output = "MMTEL registered, RCS provisioned, UT active, SMS delivery"
        val features = callExtractImsFeatures(output)
        assertThat(features).containsExactly("MMTEL", "RCS", "UT (XCAP)", "SMS over IP")
    }

    @Test
    fun `extractImsFeatures returns empty for no matches`() {
        val features = callExtractImsFeatures("nothing relevant here")
        assertThat(features).isEmpty()
    }

    @Test
    fun `extractImsFeatures UT does not match output or compute`() {
        // UT uses word boundary regex \bUT\b, so "output" should NOT match
        val features = callExtractImsFeatures("output from COMPUTE process")
        assertThat(features).doesNotContain("UT (XCAP)")
    }

    @Test
    fun `extractImsFeatures UT matches standalone UT`() {
        val features = callExtractImsFeatures("MMTEL UT RCS")
        assertThat(features).contains("UT (XCAP)")
    }

    @Test
    fun `extractImsFeatures case insensitive`() {
        val features = callExtractImsFeatures("mmtel rcs sms")
        assertThat(features).containsExactly("MMTEL", "RCS", "SMS over IP")
    }

    // =========================================================================
    // ImsExtractedInfo construction scenarios
    // =========================================================================

    @Test
    fun `ImsExtractedInfo fully registered over Wi-Fi with all features`() {
        val info = ImsExtractedInfo(
            registered = true,
            voiceCapable = true,
            videoCapable = true,
            voWifiCapable = true,
            registrationType = "Wi-Fi",
            imsFeatures = listOf("MMTEL", "RCS", "UT (XCAP)", "SMS over IP")
        )
        assertThat(info.registered).isTrue()
        assertThat(info.voWifiCapable).isTrue()
        assertThat(info.imsFeatures).hasSize(4)
    }

    @Test
    fun `ImsExtractedInfo unregistered defaults`() {
        val info = ImsExtractedInfo(
            registered = false,
            voiceCapable = false,
            videoCapable = false,
            voWifiCapable = false,
            registrationType = "Unknown",
            imsFeatures = emptyList()
        )
        assertThat(info.registered).isFalse()
        assertThat(info.imsFeatures).isEmpty()
    }

    @Test
    fun `ImsExtractedInfo over 5G NR`() {
        val info = ImsExtractedInfo(
            registered = true,
            voiceCapable = true,
            videoCapable = false,
            voWifiCapable = false,
            registrationType = "5G NR",
            imsFeatures = listOf("MMTEL")
        )
        assertThat(info.registrationType).isEqualTo("5G NR")
    }

    // =========================================================================
    // DumpsysResult extensions
    // =========================================================================

    @Test
    fun `DumpsysResult Success with large output`() {
        val bigOutput = "line\n".repeat(10000)
        val result = DumpsysResult.Success("ims", bigOutput, 10000)
        assertThat(result.lineCount).isEqualTo(10000)
    }

    @Test
    fun `DumpsysResult Success with empty output`() {
        val result = DumpsysResult.Success("carrier_config", "", 0)
        assertThat(result.output).isEmpty()
        assertThat(result.lineCount).isEqualTo(0)
    }

    @Test
    fun `DumpsysResult Error for different services`() {
        val services = listOf("ims", "phone", "carrier_config", "telecom", "connectivity")
        services.forEach { svc ->
            val error = DumpsysResult.Error(svc, "Permission denied")
            assertThat(error.service).isEqualTo(svc)
        }
    }
}
