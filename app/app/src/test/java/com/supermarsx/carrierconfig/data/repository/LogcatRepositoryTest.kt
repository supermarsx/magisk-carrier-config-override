package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [LogcatRepository] public model types:
 * [LogcatEntry], [LogLevel], [LogcatFilterType], [LogcatBuffer].
 *
 * Runtime logcat capture needs a device/emulator — those tests belong
 * in androidTest/.  Here we exercise the pure-Kotlin helpers.
 */
class LogcatRepositoryTest {

    // =========================================================================
    // LogLevel
    // =========================================================================

    @Test
    fun `LogLevel fromChar maps standard log letters`() {
        assertThat(LogLevel.fromChar('V')).isEqualTo(LogLevel.VERBOSE)
        assertThat(LogLevel.fromChar('D')).isEqualTo(LogLevel.DEBUG)
        assertThat(LogLevel.fromChar('I')).isEqualTo(LogLevel.INFO)
        assertThat(LogLevel.fromChar('W')).isEqualTo(LogLevel.WARNING)
        assertThat(LogLevel.fromChar('E')).isEqualTo(LogLevel.ERROR)
        assertThat(LogLevel.fromChar('F')).isEqualTo(LogLevel.FATAL)
    }

    @Test
    fun `LogLevel fromChar with lowercase`() {
        assertThat(LogLevel.fromChar('d')).isEqualTo(LogLevel.DEBUG)
        assertThat(LogLevel.fromChar('e')).isEqualTo(LogLevel.ERROR)
    }

    @Test
    fun `LogLevel fromChar unknown defaults to DEBUG`() {
        assertThat(LogLevel.fromChar('X')).isEqualTo(LogLevel.DEBUG)
        assertThat(LogLevel.fromChar('?')).isEqualTo(LogLevel.DEBUG)
    }

    @Test
    fun `LogLevel priority strings`() {
        assertThat(LogLevel.VERBOSE.priority).isEqualTo("V")
        assertThat(LogLevel.DEBUG.priority).isEqualTo("D")
        assertThat(LogLevel.INFO.priority).isEqualTo("I")
        assertThat(LogLevel.WARNING.priority).isEqualTo("W")
        assertThat(LogLevel.ERROR.priority).isEqualTo("E")
        assertThat(LogLevel.FATAL.priority).isEqualTo("F")
    }

    @Test
    fun `LogLevel displayNames are human-readable`() {
        assertThat(LogLevel.VERBOSE.displayName).isEqualTo("Verbose")
        assertThat(LogLevel.DEBUG.displayName).isEqualTo("Debug")
        assertThat(LogLevel.INFO.displayName).isEqualTo("Info")
        assertThat(LogLevel.WARNING.displayName).isEqualTo("Warning")
        assertThat(LogLevel.ERROR.displayName).isEqualTo("Error")
        assertThat(LogLevel.FATAL.displayName).isEqualTo("Fatal")
    }

    @Test
    fun `LogLevel values contains all 6 levels`() {
        assertThat(LogLevel.values()).hasLength(6)
    }

    // =========================================================================
    // LogcatFilterType
    // =========================================================================

    @Test
    fun `LogcatFilterType values`() {
        val types = LogcatFilterType.values()
        assertThat(types).hasLength(5)
        assertThat(types.map { it.name }).containsExactly(
            "ALL", "CARRIER_CONFIG", "IMS", "TELEPHONY", "WFC"
        )
    }

    @Test
    fun `LogcatFilterType displayNames`() {
        assertThat(LogcatFilterType.ALL.displayName).isEqualTo("All Logs")
        assertThat(LogcatFilterType.CARRIER_CONFIG.displayName).isEqualTo("CarrierConfig")
        assertThat(LogcatFilterType.IMS.displayName).isEqualTo("IMS/VoLTE")
        assertThat(LogcatFilterType.TELEPHONY.displayName).isEqualTo("Telephony")
        assertThat(LogcatFilterType.WFC.displayName).isEqualTo("Wi-Fi Calling")
    }

    // =========================================================================
    // LogcatBuffer
    // =========================================================================

    @Test
    fun `LogcatBuffer flags`() {
        assertThat(LogcatBuffer.MAIN.flag).isEqualTo("-b main")
        assertThat(LogcatBuffer.RADIO.flag).isEqualTo("-b radio")
        assertThat(LogcatBuffer.ALL.flag).isEqualTo("-b main -b radio")
    }

    @Test
    fun `LogcatBuffer displayNames`() {
        assertThat(LogcatBuffer.MAIN.displayName).isEqualTo("Main")
        assertThat(LogcatBuffer.RADIO.displayName).isEqualTo("Radio")
        assertThat(LogcatBuffer.ALL.displayName).isEqualTo("All Buffers")
    }

    // =========================================================================
    // LogcatEntry
    // =========================================================================

    @Test
    fun `LogcatEntry data class`() {
        val entry = LogcatEntry(
            timestamp = 1709424000000L,
            level = LogLevel.INFO,
            tag = "CarrierConfigLoader",
            message = "Config loaded for subId=1",
            pid = 1234,
            tid = 5678
        )
        assertThat(entry.tag).isEqualTo("CarrierConfigLoader")
        assertThat(entry.level).isEqualTo(LogLevel.INFO)
        assertThat(entry.pid).isEqualTo(1234)
        assertThat(entry.tid).isEqualTo(5678)
    }

    @Test
    fun `LogcatEntry equality`() {
        val a = LogcatEntry(100L, LogLevel.DEBUG, "Tag", "msg", 1, 1)
        val b = LogcatEntry(100L, LogLevel.DEBUG, "Tag", "msg", 1, 1)
        assertThat(a).isEqualTo(b)
    }
}
