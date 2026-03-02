package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [ConnectivityTestRepository] data models.
 *
 * ConnectivityTestRepository itself needs ConnectivityManager / TelephonyManager
 * from an Android runtime, so full execution tests belong in androidTest/.
 * Here we verify the data-class / sealed-class contracts.
 */
class ConnectivityTestRepositoryTest {

    // =========================================================================
    // TestResult variants
    // =========================================================================

    @Test
    fun `TestResult Passed carries message`() {
        val result = TestResult.Passed("HTTP 200 in 42ms")
        assertThat(result.message).isEqualTo("HTTP 200 in 42ms")
        assertThat(result).isInstanceOf(TestResult::class.java)
    }

    @Test
    fun `TestResult Failed carries message`() {
        val result = TestResult.Failed("No active network")
        assertThat(result.message).isEqualTo("No active network")
    }

    @Test
    fun `TestResult Error carries message`() {
        val result = TestResult.Error("Exception: timeout")
        assertThat(result.message).isEqualTo("Exception: timeout")
    }

    @Test
    fun `TestResult Skipped carries reason`() {
        val result = TestResult.Skipped("Not connected to Wi-Fi")
        assertThat(result.reason).isEqualTo("Not connected to Wi-Fi")
    }

    // =========================================================================
    // ConnectivityTestSuite
    // =========================================================================

    @Test
    fun `allPassed returns true when every test passed or skipped`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Passed("OK"),
            internetConnectivity = TestResult.Passed("OK"),
            wifiCalling = TestResult.Skipped("No Wi-Fi"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Passed("OK"),
            timestamp = System.currentTimeMillis()
        )
        assertThat(suite.allPassed).isTrue()
        assertThat(suite.failedCount).isEqualTo(0)
    }

    @Test
    fun `allPassed returns false when any test failed`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Failed("DNS timeout"),
            internetConnectivity = TestResult.Passed("OK"),
            wifiCalling = TestResult.Skipped("No Wi-Fi"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Passed("OK"),
            timestamp = System.currentTimeMillis()
        )
        assertThat(suite.allPassed).isFalse()
        assertThat(suite.failedCount).isEqualTo(1)
    }

    @Test
    fun `failedCount counts both Failed and Error`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Failed("DNS timeout"),
            internetConnectivity = TestResult.Error("Exception"),
            wifiCalling = TestResult.Skipped("No Wi-Fi"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Failed("Disconnected"),
            timestamp = System.currentTimeMillis()
        )
        assertThat(suite.failedCount).isEqualTo(3)
    }

    @Test
    fun `suite timestamp is preserved`() {
        val ts = 1709424000000L
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Passed("OK"),
            internetConnectivity = TestResult.Passed("OK"),
            wifiCalling = TestResult.Passed("OK"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Passed("OK"),
            timestamp = ts
        )
        assertThat(suite.timestamp).isEqualTo(ts)
    }

    @Test
    fun `suite with all errors has failedCount of 6`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Error("err"),
            dnsResolution = TestResult.Error("err"),
            internetConnectivity = TestResult.Error("err"),
            wifiCalling = TestResult.Error("err"),
            imsRegistration = TestResult.Error("err"),
            cellularData = TestResult.Error("err"),
            timestamp = 0L
        )
        assertThat(suite.failedCount).isEqualTo(6)
        assertThat(suite.allPassed).isFalse()
    }
}
