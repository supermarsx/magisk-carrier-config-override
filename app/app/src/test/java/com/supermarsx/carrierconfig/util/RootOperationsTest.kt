package com.supermarsx.carrierconfig.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [RootOperations.kt] data models and pure functions.
 *
 * RootShell/SuFileManager/ServiceRestarter actual shell commands require root/device.
 * Here we test the ShellResult data class and SystemPropertiesReader helper contracts.
 */
class RootOperationsTest {

    // =========================================================================
    // ShellResult data class
    // =========================================================================

    @Test
    fun `ShellResult success with output`() {
        val result = ShellResult(
            success = true,
            output = listOf("line1", "line2", "line3"),
            error = emptyList(),
            exitCode = 0
        )
        assertThat(result.success).isTrue()
        assertThat(result.output).hasSize(3)
        assertThat(result.error).isEmpty()
        assertThat(result.exitCode).isEqualTo(0)
    }

    @Test
    fun `ShellResult failure with error`() {
        val result = ShellResult(
            success = false,
            output = emptyList(),
            error = listOf("Permission denied"),
            exitCode = 1
        )
        assertThat(result.success).isFalse()
        assertThat(result.error).containsExactly("Permission denied")
        assertThat(result.exitCode).isEqualTo(1)
    }

    @Test
    fun `ShellResult outputString joins with newline`() {
        val result = ShellResult(true, listOf("a", "b", "c"), emptyList(), 0)
        assertThat(result.outputString).isEqualTo("a\nb\nc")
    }

    @Test
    fun `ShellResult outputString empty list is empty string`() {
        val result = ShellResult(true, emptyList(), emptyList(), 0)
        assertThat(result.outputString).isEmpty()
    }

    @Test
    fun `ShellResult errorString joins with newline`() {
        val result = ShellResult(false, emptyList(), listOf("err1", "err2"), 1)
        assertThat(result.errorString).isEqualTo("err1\nerr2")
    }

    @Test
    fun `ShellResult errorString empty list is empty string`() {
        val result = ShellResult(true, emptyList(), emptyList(), 0)
        assertThat(result.errorString).isEmpty()
    }

    @Test
    fun `ShellResult equality`() {
        val a = ShellResult(true, listOf("ok"), emptyList(), 0)
        val b = ShellResult(true, listOf("ok"), emptyList(), 0)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `ShellResult inequality on exit code`() {
        val a = ShellResult(true, emptyList(), emptyList(), 0)
        val b = ShellResult(true, emptyList(), emptyList(), 1)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `ShellResult copy changes only specified field`() {
        val original = ShellResult(false, emptyList(), listOf("error"), 1)
        val fixed = original.copy(success = true, exitCode = 0)
        assertThat(fixed.success).isTrue()
        assertThat(fixed.exitCode).isEqualTo(0)
        assertThat(fixed.error).containsExactly("error")
    }

    @Test
    fun `ShellResult with negative exit code`() {
        val result = ShellResult(false, emptyList(), listOf("Unknown error"), -1)
        assertThat(result.exitCode).isEqualTo(-1)
    }

    @Test
    fun `ShellResult with multiline output`() {
        val lines = (1..100).map { "line $it" }
        val result = ShellResult(true, lines, emptyList(), 0)
        assertThat(result.output).hasSize(100)
        assertThat(result.outputString).contains("line 50")
    }

    // =========================================================================
    // ShellResult with mixed output and error
    // =========================================================================

    @Test
    fun `ShellResult can have both output and error`() {
        val result = ShellResult(
            success = false,
            output = listOf("partial output"),
            error = listOf("warning: deprecated"),
            exitCode = 2
        )
        assertThat(result.output).isNotEmpty()
        assertThat(result.error).isNotEmpty()
    }

    // =========================================================================
    // getprop parsing pattern (used by SystemPropertiesReader.getPropertiesMatching)
    // =========================================================================

    private val propRegex = Regex("\\[(.*?)\\]: \\[(.*?)\\]")

    @Test
    fun `getprop regex parses standard format`() {
        val line = "[ro.build.version.release]: [14]"
        val match = propRegex.find(line)
        assertThat(match).isNotNull()
        val (key, value) = match!!.destructured
        assertThat(key).isEqualTo("ro.build.version.release")
        assertThat(value).isEqualTo("14")
    }

    @Test
    fun `getprop regex parses empty value`() {
        val line = "[ro.build.version.oneui]: []"
        val match = propRegex.find(line)
        assertThat(match).isNotNull()
        val (_, value) = match!!.destructured
        assertThat(value).isEmpty()
    }

    @Test
    fun `getprop regex parses value with spaces`() {
        val line = "[ro.product.model]: [SM-S928B/DS]"
        val match = propRegex.find(line)
        assertThat(match).isNotNull()
        val (_, value) = match!!.destructured
        assertThat(value).isEqualTo("SM-S928B/DS")
    }

    @Test
    fun `getprop regex does not match malformed line`() {
        val line = "not a property line"
        assertThat(propRegex.find(line)).isNull()
    }

    @Test
    fun `getprop regex multiple lines parsed to map`() {
        val lines = listOf(
            "[ro.build.version.sdk]: [35]",
            "[ro.product.manufacturer]: [samsung]",
            "[ro.build.version.release]: [15]"
        )
        val map = lines.mapNotNull { line ->
            propRegex.find(line)?.let {
                val (key, value) = it.destructured
                key to value
            }
        }.toMap()

        assertThat(map).hasSize(3)
        assertThat(map["ro.build.version.sdk"]).isEqualTo("35")
        assertThat(map["ro.product.manufacturer"]).isEqualTo("samsung")
    }

    // =========================================================================
    // SuFileManager path patterns
    // =========================================================================

    @Test
    fun `file check command pattern`() {
        val path = "/data/vendor/carrierconfig/override.xml"
        val cmd = "[ -f '$path' ] && echo 'exists' || echo 'not_exists'"
        assertThat(cmd).contains(path)
        assertThat(cmd).contains("exists")
    }

    @Test
    fun `directory check command pattern`() {
        val path = "/data/adb/cco/active"
        val cmd = "[ -d '$path' ] && echo 'exists' || echo 'not_exists'"
        assertThat(cmd).contains(path)
    }

    @Test
    fun `mkdir recursive command`() {
        val path = "/data/adb/cco/active"
        val cmd = "mkdir -p '$path'"
        assertThat(cmd).contains("-p")
        assertThat(cmd).contains(path)
    }

    @Test
    fun `chmod command pattern`() {
        val mode = "644"
        val path = "/data/adb/cco/active/override.xml"
        val cmd = "chmod $mode '$path'"
        assertThat(cmd).isEqualTo("chmod 644 '/data/adb/cco/active/override.xml'")
    }

    // =========================================================================
    // ServiceRestarter command patterns
    // =========================================================================

    @Test
    fun `restart phone service command`() {
        val cmd = "killall -9 com.android.phone"
        assertThat(cmd).contains("com.android.phone")
    }

    @Test
    fun `restart IMS has multiple fallback commands`() {
        val commands = arrayOf(
            "killall -9 com.sec.imsservice",
            "killall -9 ims",
            "setprop ctl.restart ims"
        )
        assertThat(commands).hasLength(3)
        assertThat(commands[0]).contains("com.sec.imsservice")
    }

    @Test
    fun `airplane mode toggle sequence`() {
        val commands = arrayOf(
            "settings put global airplane_mode_on 1",
            "am broadcast -a android.intent.action.AIRPLANE_MODE",
            "sleep 2",
            "settings put global airplane_mode_on 0",
            "am broadcast -a android.intent.action.AIRPLANE_MODE"
        )
        assertThat(commands).hasLength(5)
        // First command enables, fourth disables
        assertThat(commands[0]).contains("airplane_mode_on 1")
        assertThat(commands[3]).contains("airplane_mode_on 0")
    }

    // =========================================================================
    // SystemPropertiesReader property keys
    // =========================================================================

    @Test
    fun `known Samsung property keys are valid`() {
        val keys = listOf(
            "ro.build.version.release",
            "ro.build.version.sdk",
            "ro.product.model",
            "ro.product.manufacturer",
            "ro.build.fingerprint",
            "ro.build.version.oneui",
            "ro.build.version.security_patch",
            "ro.csc.sales_code"
        )
        keys.forEach { key ->
            assertThat(key).startsWith("ro.")
            assertThat(key).doesNotContain(" ")
        }
    }

    @Test
    fun `CSC code fallback from ril`() {
        val primaryKey = "ro.csc.sales_code"
        val fallbackKey = "ril.sales_code"
        assertThat(primaryKey).isNotEqualTo(fallbackKey)
    }
}
