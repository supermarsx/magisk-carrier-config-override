package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [DumpsysRepository] data models — [DumpsysResult] and [ImsExtractedInfo].
 *
 * DumpsysRepository.getDumpsysIms() etc. need a rooted device / Shell; those tests
 * belong in androidTest/.  Here we verify the model contracts.
 */
class DumpsysRepositoryTest {

    // =========================================================================
    // DumpsysResult
    // =========================================================================

    @Test
    fun `DumpsysResult Success carries service, output and lineCount`() {
        val result = DumpsysResult.Success(
            service = "ims",
            output = "line1\nline2",
            lineCount = 2
        )
        assertThat(result.service).isEqualTo("ims")
        assertThat(result.output).contains("line1")
        assertThat(result.lineCount).isEqualTo(2)
    }

    @Test
    fun `DumpsysResult Error carries service and message`() {
        val result = DumpsysResult.Error(
            service = "phone",
            message = "Permission denied"
        )
        assertThat(result.service).isEqualTo("phone")
        assertThat(result.message).isEqualTo("Permission denied")
    }

    @Test
    fun `DumpsysResult types are sealed subtypes`() {
        val success: DumpsysResult = DumpsysResult.Success("ims", "", 0)
        val error: DumpsysResult = DumpsysResult.Error("ims", "err")

        assertThat(success).isInstanceOf(DumpsysResult.Success::class.java)
        assertThat(error).isInstanceOf(DumpsysResult.Error::class.java)
    }

    // =========================================================================
    // ImsExtractedInfo
    // =========================================================================

    @Test
    fun `ImsExtractedInfo default unregistered state`() {
        val info = ImsExtractedInfo(
            registered = false,
            voiceCapable = false,
            videoCapable = false,
            voWifiCapable = false,
            registrationType = "Unknown",
            imsFeatures = emptyList()
        )
        assertThat(info.registered).isFalse()
        assertThat(info.voWifiCapable).isFalse()
        assertThat(info.registrationType).isEqualTo("Unknown")
        assertThat(info.imsFeatures).isEmpty()
    }

    @Test
    fun `ImsExtractedInfo fully registered over wifi`() {
        val info = ImsExtractedInfo(
            registered = true,
            voiceCapable = true,
            videoCapable = false,
            voWifiCapable = true,
            registrationType = "Wi-Fi",
            imsFeatures = listOf("MMTEL", "SMS over IP")
        )
        assertThat(info.registered).isTrue()
        assertThat(info.voWifiCapable).isTrue()
        assertThat(info.registrationType).isEqualTo("Wi-Fi")
        assertThat(info.imsFeatures).containsExactly("MMTEL", "SMS over IP")
    }

    @Test
    fun `ImsExtractedInfo over LTE with all features`() {
        val info = ImsExtractedInfo(
            registered = true,
            voiceCapable = true,
            videoCapable = true,
            voWifiCapable = false,
            registrationType = "LTE",
            imsFeatures = listOf("MMTEL", "RCS", "UT (XCAP)", "SMS over IP")
        )
        assertThat(info.registrationType).isEqualTo("LTE")
        assertThat(info.imsFeatures).hasSize(4)
        assertThat(info.videoCapable).isTrue()
    }

    @Test
    fun `ImsExtractedInfo equality`() {
        val a = ImsExtractedInfo(true, true, false, true, "Wi-Fi", listOf("MMTEL"))
        val b = ImsExtractedInfo(true, true, false, true, "Wi-Fi", listOf("MMTEL"))
        assertThat(a).isEqualTo(b)
    }

    // =========================================================================
    // UT word-boundary regex (Fix #13)
    // 
    // The extractImsFeatures method uses Regex("\\bUT\\b") to avoid false
    // positives from words like "output", "about", "shutting", etc.
    // =========================================================================

    private val utRegex = Regex("\\bUT\\b")

    @Test
    fun `UT regex matches standalone UT`() {
        assertThat(utRegex.containsMatchIn("UT feature enabled")).isTrue()
    }

    @Test
    fun `UT regex matches UT at start of line`() {
        assertThat(utRegex.containsMatchIn("UT is active")).isTrue()
    }

    @Test
    fun `UT regex matches UT at end of line`() {
        assertThat(utRegex.containsMatchIn("service: UT")).isTrue()
    }

    @Test
    fun `UT regex matches UT with parentheses context`() {
        assertThat(utRegex.containsMatchIn("UT (XCAP) enabled")).isTrue()
    }

    @Test
    fun `UT regex does NOT match output`() {
        assertThat(utRegex.containsMatchIn("output data")).isFalse()
    }

    @Test
    fun `UT regex does NOT match about`() {
        assertThat(utRegex.containsMatchIn("about IMS")).isFalse()
    }

    @Test
    fun `UT regex does NOT match shutting`() {
        assertThat(utRegex.containsMatchIn("shutting down")).isFalse()
    }

    @Test
    fun `UT regex does NOT match INPUT`() {
        assertThat(utRegex.containsMatchIn("INPUT_METHOD")).isFalse()
    }

    @Test
    fun `UT regex does NOT match COMPUTE`() {
        assertThat(utRegex.containsMatchIn("COMPUTE result")).isFalse()
    }

    @Test
    fun `UT regex does NOT match reboot`() {
        assertThat(utRegex.containsMatchIn("reboot requested")).isFalse()
    }

    @Test
    fun `UT regex does NOT match lowercase ut`() {
        // The word-boundary regex is case-sensitive by default
        assertThat(utRegex.containsMatchIn("ut lowercase")).isFalse()
    }

    @Test
    fun `UT regex matches UT surrounded by spaces`() {
        assertThat(utRegex.containsMatchIn("MMTEL UT RCS")).isTrue()
    }

    @Test
    fun `UT regex matches UT after comma`() {
        assertThat(utRegex.containsMatchIn("MMTEL,UT,RCS")).isTrue()
    }

    @Test
    fun `UT regex matches UT after colon`() {
        assertThat(utRegex.containsMatchIn("feature:UT:enabled")).isTrue()
    }
}
