package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.lang.reflect.Method

/**
 * Tests for [LogcatRepository.parseLogcatLine] — the private threadtime parser.
 *
 * We use reflection to access the private method. These tests verify the regex
 * handles real-world logcat output correctly.
 */
class LogcatParseTest {

    private val repository = LogcatRepository(org.mockito.kotlin.mock())

    private val parseMethod: Method = LogcatRepository::class.java
        .getDeclaredMethod("parseLogcatLine", String::class.java)
        .also { it.isAccessible = true }

    private fun parse(line: String): LogcatEntry? {
        @Suppress("UNCHECKED_CAST")
        return parseMethod.invoke(repository, line) as? LogcatEntry
    }

    // =========================================================================
    // Valid threadtime lines
    // =========================================================================

    @Test
    fun `parses standard threadtime format`() {
        val entry = parse("01-15 10:30:45.123  1234  5678 I CarrierConfigLoader: Config loaded for subId=1")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.INFO)
        assertThat(entry.tag).isEqualTo("CarrierConfigLoader")
        assertThat(entry.message).isEqualTo("Config loaded for subId=1")
        assertThat(entry.pid).isEqualTo(1234)
        assertThat(entry.tid).isEqualTo(5678)
    }

    @Test
    fun `parses debug level`() {
        val entry = parse("02-04 08:00:00.000   100   200 D ImsManager: initialize()")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.DEBUG)
        assertThat(entry.tag).isEqualTo("ImsManager")
    }

    @Test
    fun `parses warning level`() {
        val entry = parse("12-31 23:59:59.999  9999  9999 W ImsPhone: Fallback to cellular")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.WARNING)
    }

    @Test
    fun `parses error level`() {
        val entry = parse("06-15 12:00:00.500   500   600 E TelephonyRegistry: Registration failed")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.ERROR)
    }

    @Test
    fun `parses fatal level`() {
        val entry = parse("03-01 00:00:01.001  1000  1001 F Phone: FATAL crash")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.FATAL)
    }

    @Test
    fun `parses verbose level`() {
        val entry = parse("01-01 00:00:00.000     1     1 V WifiCalling: verbose trace")
        assertThat(entry).isNotNull()
        assertThat(entry!!.level).isEqualTo(LogLevel.VERBOSE)
    }

    @Test
    fun `parses message with colons`() {
        val entry = parse("01-01 12:00:00.000  1000  2000 I TestTag: key:value:extra")
        assertThat(entry).isNotNull()
        assertThat(entry!!.message).isEqualTo("key:value:extra")
    }

    @Test
    fun `parses tag with dots`() {
        val entry = parse("01-01 12:00:00.000  1000  2000 I com.sec.ims: started")
        assertThat(entry).isNotNull()
        assertThat(entry!!.tag).isEqualTo("com.sec.ims")
    }

    @Test
    fun `parses empty message after tag`() {
        val entry = parse("01-01 12:00:00.000  1000  2000 I Tag: ")
        assertThat(entry).isNotNull()
        assertThat(entry!!.message).isEmpty()
    }

    // =========================================================================
    // Invalid / skippable lines
    // =========================================================================

    @Test
    fun `returns null for blank line`() {
        assertThat(parse("")).isNull()
        assertThat(parse("   ")).isNull()
    }

    @Test
    fun `returns null for separator line`() {
        assertThat(parse("--------- beginning of main")).isNull()
        assertThat(parse("--- switch to radio")).isNull()
    }

    @Test
    fun `returns null for malformed line`() {
        assertThat(parse("this is not a logcat line")).isNull()
    }

    @Test
    fun `returns null for line missing PID and TID`() {
        assertThat(parse("01-01 12:00:00.000 I Tag: message")).isNull()
    }

    @Test
    fun `returns null for line missing level`() {
        assertThat(parse("01-01 12:00:00.000  1000  2000 Tag: message")).isNull()
    }

    // =========================================================================
    // Edge cases
    // =========================================================================

    @Test
    fun `handles large PID and TID`() {
        val entry = parse("01-01 00:00:00.000 32767 32767 I Tag: msg")
        assertThat(entry).isNotNull()
        assertThat(entry!!.pid).isEqualTo(32767)
        assertThat(entry.tid).isEqualTo(32767)
    }

    @Test
    fun `handles single-digit PID`() {
        val entry = parse("01-01 00:00:00.000     1     1 I Tag: msg")
        assertThat(entry).isNotNull()
        assertThat(entry!!.pid).isEqualTo(1)
    }

    @Test
    fun `handles message with special characters`() {
        val entry = parse("01-01 12:00:00.000  1000  2000 I Tag: <xml>&amp;\"quotes\"</xml>")
        assertThat(entry).isNotNull()
        assertThat(entry!!.message).contains("<xml>")
    }

    @Test
    fun `timestamp field extracted`() {
        val entry = parse("07-20 15:30:45.678  1000  2000 I CarrierConfig: loaded")
        assertThat(entry).isNotNull()
        // parseLogcatLine uses System.currentTimeMillis() so just check it's set
        assertThat(entry!!.timestamp).isGreaterThan(0L)
    }
}
