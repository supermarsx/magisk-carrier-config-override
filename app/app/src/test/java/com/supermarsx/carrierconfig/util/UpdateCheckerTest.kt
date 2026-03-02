package com.supermarsx.carrierconfig.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [UpdateChecker].
 *
 * Tests cover:
 * - Version parsing and comparison
 * - UpdateCheckResult sealed class variants
 * - Edge cases in version strings
 */
class UpdateCheckerTest {

    // =========================================================================
    // UpdateCheckResult sealed class
    // =========================================================================

    @Test
    fun `UpdateAvailable carries all fields`() {
        val result = UpdateCheckResult.UpdateAvailable(
            currentVersion = "1.0.0",
            latestVersion = "v1.1.0",
            downloadUrl = "https://github.com/test/releases/download/v1.1.0/app.apk",
            releaseNotes = "Bug fixes",
            publishedAt = "2026-02-04T10:00:00Z"
        )
        assertThat(result.currentVersion).isEqualTo("1.0.0")
        assertThat(result.latestVersion).isEqualTo("v1.1.0")
        assertThat(result.downloadUrl).contains("v1.1.0")
        assertThat(result.releaseNotes).isEqualTo("Bug fixes")
        assertThat(result.publishedAt).isNotNull()
    }

    @Test
    fun `UpdateAvailable with null optionals`() {
        val result = UpdateCheckResult.UpdateAvailable(
            currentVersion = "1.0.0",
            latestVersion = "v2.0.0",
            downloadUrl = "https://example.com",
            releaseNotes = null,
            publishedAt = null
        )
        assertThat(result.releaseNotes).isNull()
        assertThat(result.publishedAt).isNull()
    }

    @Test
    fun `UpToDate carries version`() {
        val result = UpdateCheckResult.UpToDate("1.0.0")
        assertThat(result.currentVersion).isEqualTo("1.0.0")
    }

    @Test
    fun `Error carries message`() {
        val result = UpdateCheckResult.Error("Network timeout")
        assertThat(result.message).isEqualTo("Network timeout")
    }

    @Test
    fun `all three variants are distinct types`() {
        val available: UpdateCheckResult = UpdateCheckResult.UpdateAvailable(
            "1.0.0", "2.0.0", "url", null, null
        )
        val upToDate: UpdateCheckResult = UpdateCheckResult.UpToDate("1.0.0")
        val error: UpdateCheckResult = UpdateCheckResult.Error("err")

        assertThat(available).isInstanceOf(UpdateCheckResult.UpdateAvailable::class.java)
        assertThat(upToDate).isInstanceOf(UpdateCheckResult.UpToDate::class.java)
        assertThat(error).isInstanceOf(UpdateCheckResult.Error::class.java)
    }

    // =========================================================================
    // Version parsing (via reflection to test private parseVersion)
    // We test the behavior through comparisons since Version is private
    // =========================================================================

    @Test
    fun `UpdateAvailable equality`() {
        val a = UpdateCheckResult.UpdateAvailable("1.0.0", "2.0.0", "url", null, null)
        val b = UpdateCheckResult.UpdateAvailable("1.0.0", "2.0.0", "url", null, null)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `UpToDate equality`() {
        val a = UpdateCheckResult.UpToDate("1.0.0")
        val b = UpdateCheckResult.UpToDate("1.0.0")
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `Error equality`() {
        val a = UpdateCheckResult.Error("err")
        val b = UpdateCheckResult.Error("err")
        assertThat(a).isEqualTo(b)
    }

    // =========================================================================
    // Version comparison via reflection
    // The Version class is private, so we use reflection to test it
    // =========================================================================

    @Test
    fun `parseVersion via reflection - simple version`() {
        val version = invokeParseVersion("1.2.3")
        assertThat(version).isNotNull()
    }

    @Test
    fun `parseVersion strips v prefix`() {
        val v1 = invokeParseVersion("v1.2.3")
        val v2 = invokeParseVersion("1.2.3")
        assertThat(v1).isEqualTo(v2)
    }

    @Test
    fun `parseVersion strips pre-release suffix`() {
        val v1 = invokeParseVersion("1.2.3-beta")
        val v2 = invokeParseVersion("1.2.3")
        assertThat(v1).isEqualTo(v2)
    }

    @Test
    fun `version comparison major difference`() {
        val v1 = invokeParseVersion("1.0.0") as Comparable<Any>
        val v2 = invokeParseVersion("2.0.0")!!
        assertThat(v1.compareTo(v2)).isLessThan(0)
    }

    @Test
    fun `version comparison minor difference`() {
        val v1 = invokeParseVersion("1.0.0") as Comparable<Any>
        val v2 = invokeParseVersion("1.1.0")!!
        assertThat(v1.compareTo(v2)).isLessThan(0)
    }

    @Test
    fun `version comparison patch difference`() {
        val v1 = invokeParseVersion("1.0.0") as Comparable<Any>
        val v2 = invokeParseVersion("1.0.1")!!
        assertThat(v1.compareTo(v2)).isLessThan(0)
    }

    @Test
    fun `version comparison equal`() {
        val v1 = invokeParseVersion("1.0.0") as Comparable<Any>
        val v2 = invokeParseVersion("1.0.0")!!
        assertThat(v1.compareTo(v2)).isEqualTo(0)
    }

    @Test
    fun `version comparison higher is positive`() {
        val v1 = invokeParseVersion("2.0.0") as Comparable<Any>
        val v2 = invokeParseVersion("1.0.0")!!
        assertThat(v1.compareTo(v2)).isGreaterThan(0)
    }

    @Test
    fun `parseVersion handles missing patch`() {
        val v = invokeParseVersion("1.0")
        assertThat(v).isNotNull()
    }

    @Test
    fun `parseVersion handles single number`() {
        val v = invokeParseVersion("1")
        assertThat(v).isNotNull()
    }

    // =========================================================================
    // Helper: Invoke private parseVersion via reflection
    // =========================================================================

    private fun invokeParseVersion(version: String): Any? {
        val method = UpdateChecker::class.java.getDeclaredMethod("parseVersion", String::class.java)
        method.isAccessible = true
        return method.invoke(UpdateChecker, version)
    }
}
