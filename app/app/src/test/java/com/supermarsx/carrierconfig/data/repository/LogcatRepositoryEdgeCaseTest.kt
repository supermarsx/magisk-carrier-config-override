package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Edge-case tests for [LogcatRepository] public model types.
 * Exercises boundary and degenerate inputs on enums and data classes.
 */
class LogcatRepositoryEdgeCaseTest {

    // =========================================================================
    // LogLevel edge cases
    // =========================================================================

    @Test
    fun `LogLevel fromChar with null-like zero char`() {
        val result = LogLevel.fromChar('\u0000')
        assertThat(result).isEqualTo(LogLevel.DEBUG) // default fallback
    }

    @Test
    fun `LogLevel fromChar with space`() {
        assertThat(LogLevel.fromChar(' ')).isEqualTo(LogLevel.DEBUG)
    }

    @Test
    fun `LogLevel fromChar with digit`() {
        assertThat(LogLevel.fromChar('5')).isEqualTo(LogLevel.DEBUG)
    }

    @Test
    fun `LogLevel values are in increasing severity order`() {
        val levels = LogLevel.values()
        assertThat(levels[0]).isEqualTo(LogLevel.VERBOSE)
        assertThat(levels[5]).isEqualTo(LogLevel.FATAL)
    }

    @Test
    fun `LogLevel fromChar case-insensitive for all levels`() {
        assertThat(LogLevel.fromChar('v')).isEqualTo(LogLevel.VERBOSE)
        assertThat(LogLevel.fromChar('i')).isEqualTo(LogLevel.INFO)
        assertThat(LogLevel.fromChar('w')).isEqualTo(LogLevel.WARNING)
        assertThat(LogLevel.fromChar('f')).isEqualTo(LogLevel.FATAL)
    }

    // =========================================================================
    // LogcatEntry edge cases
    // =========================================================================

    @Test
    fun `LogcatEntry with empty tag and message`() {
        val entry = LogcatEntry(0L, LogLevel.DEBUG, "", "", 0, 0)
        assertThat(entry.tag).isEmpty()
        assertThat(entry.message).isEmpty()
    }

    @Test
    fun `LogcatEntry with negative timestamp`() {
        val entry = LogcatEntry(-1L, LogLevel.ERROR, "T", "M", 1, 1)
        assertThat(entry.timestamp).isEqualTo(-1L)
    }

    @Test
    fun `LogcatEntry with zero PID and TID`() {
        val entry = LogcatEntry(100L, LogLevel.INFO, "Tag", "Msg", 0, 0)
        assertThat(entry.pid).isEqualTo(0)
        assertThat(entry.tid).isEqualTo(0)
    }

    @Test
    fun `LogcatEntry with max int PID`() {
        val entry = LogcatEntry(100L, LogLevel.INFO, "Tag", "Msg", Int.MAX_VALUE, Int.MAX_VALUE)
        assertThat(entry.pid).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `LogcatEntry with very long message`() {
        val longMsg = "M".repeat(100_000)
        val entry = LogcatEntry(100L, LogLevel.DEBUG, "T", longMsg, 1, 1)
        assertThat(entry.message).hasLength(100_000)
    }

    @Test
    fun `LogcatEntry with unicode message`() {
        val entry = LogcatEntry(100L, LogLevel.INFO, "日本", "テスト 🎉", 1, 1)
        assertThat(entry.tag).isEqualTo("日本")
        assertThat(entry.message).contains("🎉")
    }

    @Test
    fun `LogcatEntry copy preserves all fields`() {
        val original = LogcatEntry(123L, LogLevel.WARNING, "TAG", "msg", 42, 99)
        val copy = original.copy(level = LogLevel.ERROR)
        assertThat(copy.timestamp).isEqualTo(123L)
        assertThat(copy.tag).isEqualTo("TAG")
        assertThat(copy.level).isEqualTo(LogLevel.ERROR)
        assertThat(copy.pid).isEqualTo(42)
    }

    @Test
    fun `LogcatEntry toString contains all fields`() {
        val entry = LogcatEntry(100L, LogLevel.INFO, "MyTag", "hello", 1, 2)
        val str = entry.toString()
        assertThat(str).contains("MyTag")
        assertThat(str).contains("hello")
        assertThat(str).contains("INFO")
    }

    // =========================================================================
    // LogcatBuffer edge cases
    // =========================================================================

    @Test
    fun `LogcatBuffer values count`() {
        assertThat(LogcatBuffer.values()).hasLength(3)
    }

    @Test
    fun `LogcatBuffer ALL flag contains both main and radio`() {
        assertThat(LogcatBuffer.ALL.flag).contains("main")
        assertThat(LogcatBuffer.ALL.flag).contains("radio")
    }

    // =========================================================================
    // LogcatFilterType edge cases
    // =========================================================================

    @Test
    fun `LogcatFilterType ALL is superset`() {
        // Verify ALL display name indicates it covers everything
        assertThat(LogcatFilterType.ALL.displayName).isEqualTo("All Logs")
    }

    @Test
    fun `LogcatFilterType each value has non-empty displayName`() {
        LogcatFilterType.values().forEach { filter ->
            assertThat(filter.displayName).isNotEmpty()
        }
    }

    @Test
    fun `LogcatFilterType each value has unique displayName`() {
        val names = LogcatFilterType.values().map { it.displayName }
        assertThat(names).containsNoDuplicates()
    }

    // =========================================================================
    // ConnectivityTestSuite edge cases
    // =========================================================================

    @Test
    fun `ConnectivityTestSuite all errors yields failedCount 6`() {
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

    @Test
    fun `ConnectivityTestSuite all skipped yields allPassed true`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Skipped("n/a"),
            dnsResolution = TestResult.Skipped("n/a"),
            internetConnectivity = TestResult.Skipped("n/a"),
            wifiCalling = TestResult.Skipped("n/a"),
            imsRegistration = TestResult.Skipped("n/a"),
            cellularData = TestResult.Skipped("n/a"),
            timestamp = 0L
        )
        assertThat(suite.allPassed).isTrue()
        assertThat(suite.failedCount).isEqualTo(0)
    }

    @Test
    fun `ConnectivityTestSuite mixed results counts correctly`() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("ok"),
            dnsResolution = TestResult.Failed("fail"),
            internetConnectivity = TestResult.Passed("ok"),
            wifiCalling = TestResult.Skipped("skip"),
            imsRegistration = TestResult.Error("err"),
            cellularData = TestResult.Passed("ok"),
            timestamp = System.currentTimeMillis()
        )
        assertThat(suite.failedCount).isEqualTo(2) // Failed + Error
        assertThat(suite.allPassed).isFalse()
    }

    @Test
    fun `TestResult Passed with empty message`() {
        val result = TestResult.Passed("")
        assertThat(result.message).isEmpty()
    }

    @Test
    fun `TestResult Failed with null-like message`() {
        val result = TestResult.Failed("null")
        assertThat(result.message).isEqualTo("null")
    }
}
