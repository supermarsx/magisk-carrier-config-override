package com.supermarsx.carrierconfig.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supermarsx.carrierconfig.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for repository data models and pure-logic helpers.
 *
 * Tests that touch Shell / root / dumpsys are skipped when root is unavailable.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // =========================================================================
    // ExportRepository — file I/O with real context
    // =========================================================================

    @Test
    fun exportRepository_exportConfiguration_createsFile() = runTest {
        val repo = ExportRepository(context)
        val result = repo.exportConfiguration(includePresets = true, includeSettings = true)

        assertTrue("Expected ExportResult.Success", result is ExportResult.Success)
        val path = (result as ExportResult.Success).filePath
        assertTrue("File should exist", java.io.File(path).exists())
        // Cleanup
        java.io.File(path).delete()
    }

    @Test
    fun exportRepository_exportDiagnostics_createsFile() = runTest {
        val repo = ExportRepository(context)
        val result = repo.exportDiagnostics(
            deviceInfo = "Model: Test",
            simInfo = "SIM: Test",
            imsStatus = "Registered"
        )

        assertTrue(result is ExportResult.Success)
        val path = (result as ExportResult.Success).filePath
        assertTrue(java.io.File(path).exists())
        java.io.File(path).delete()
    }

    @Test
    fun exportRepository_exportTextFile_createsFile() = runTest {
        val repo = ExportRepository(context)
        val result = repo.exportTextFile("test_export", "txt", "Hello CCO")

        assertTrue(result is ExportResult.Success)
        val path = (result as ExportResult.Success).filePath
        val file = java.io.File(path)
        assertTrue(file.exists())
        assertEquals("Hello CCO", file.readText())
        file.delete()
    }

    @Test
    fun exportRepository_importConfigurationFromString_validJson() = runTest {
        val repo = ExportRepository(context)
        val json = """
        {
            "version": "1.0.0",
            "exportDate": 0,
            "settings": {
                "autoRefresh": true,
                "enableNotifications": false,
                "debugMode": false,
                "theme": "dark"
            },
            "customKeys": []
        }
        """.trimIndent()

        val result = repo.importConfigurationFromString(json)
        assertTrue(result is ImportResult.Success)
        assertEquals("1.0.0", (result as ImportResult.Success).version)
    }

    @Test
    fun exportRepository_importConfigurationFromString_invalidJson() = runTest {
        val repo = ExportRepository(context)
        val result = repo.importConfigurationFromString("not json at all")
        assertTrue(result is ImportResult.Error)
    }

    @Test
    fun exportRepository_listExports_returnsEmptyOrList() = runTest {
        val repo = ExportRepository(context)
        val exports = repo.listExports()
        // Just verify it doesn't throw
        assertNotNull(exports)
    }

    @Test
    fun exportRepository_getExportDirectory_returnsPath() {
        val repo = ExportRepository(context)
        val dir = repo.getExportDirectory()
        assertTrue("Path should contain CCO", dir.contains("CCO"))
    }

    // =========================================================================
    // ConnectivityTestRepository — model verification
    // =========================================================================

    @Test
    fun testResult_sealed_class_variants() {
        val passed: TestResult = TestResult.Passed("OK")
        val failed: TestResult = TestResult.Failed("DNS timeout")
        val error: TestResult = TestResult.Error("Exception")
        val skipped: TestResult = TestResult.Skipped("No Wi-Fi")

        assertTrue(passed is TestResult.Passed)
        assertTrue(failed is TestResult.Failed)
        assertTrue(error is TestResult.Error)
        assertTrue(skipped is TestResult.Skipped)
    }

    @Test
    fun connectivityTestSuite_allPassed() {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Passed("OK"),
            internetConnectivity = TestResult.Passed("OK"),
            wifiCalling = TestResult.Skipped("No Wi-Fi"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Passed("OK"),
            timestamp = System.currentTimeMillis()
        )
        assertTrue(suite.allPassed)
        assertEquals(0, suite.failedCount)
    }

    // =========================================================================
    // DumpsysRepository — model verification
    // =========================================================================

    @Test
    fun dumpsysResult_success_model() {
        val result = DumpsysResult.Success("ims", "output data", 42)
        assertEquals("ims", result.service)
        assertEquals(42, result.lineCount)
    }

    @Test
    fun imsExtractedInfo_model() {
        val info = ImsExtractedInfo(
            registered = true,
            voiceCapable = true,
            videoCapable = false,
            voWifiCapable = true,
            registrationType = "Wi-Fi",
            imsFeatures = listOf("MMTEL")
        )
        assertTrue(info.registered)
        assertTrue(info.voWifiCapable)
        assertEquals("Wi-Fi", info.registrationType)
    }

    // =========================================================================
    // LogcatRepository — enum / model verification
    // =========================================================================

    @Test
    fun logLevel_fromChar_integration() {
        assertEquals(LogLevel.VERBOSE, LogLevel.fromChar('V'))
        assertEquals(LogLevel.ERROR, LogLevel.fromChar('E'))
        assertEquals(LogLevel.DEBUG, LogLevel.fromChar('Z')) // unknown → DEBUG
    }

    @Test
    fun logcatFilterType_values() {
        assertEquals(5, LogcatFilterType.values().size)
    }

    @Test
    fun logcatBuffer_values() {
        assertEquals(3, LogcatBuffer.values().size)
        assertEquals("-b radio", LogcatBuffer.RADIO.flag)
    }
}
