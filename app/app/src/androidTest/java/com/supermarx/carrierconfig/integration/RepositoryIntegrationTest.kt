package com.supermarsx.carrierconfig.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supermarsx.carrierconfig.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun logcatRepository_parseLogEntry_integration() {
        val repository = LogcatRepository()
        val testLog = "01-15 10:30:45.123  1234  5678 I TestTag: Integration test message"

        val entry = repository.parseLogEntry(testLog)

        assertNotNull(entry)
        assertEquals("TestTag", entry?.tag)
        assertEquals("Integration test message", entry?.message)
    }

    @Test
    fun dumpsysRepository_extractImsInfo_integration() {
        val repository = DumpsysRepository()
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            IMS Voice: true
            IMS Video: true
            VoLTE enabled: true
            VoWiFi enabled: true
            Registration type: WLAN
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
        assertTrue(info.contains("Type: Wi-Fi"))
    }

    @Test
    fun connectivityTestRepository_testCase_enum_integration() {
        val testCases = ConnectivityTestRepository.TestCase.values()

        // Verify all test cases have proper metadata
        testCases.forEach { testCase ->
            assertNotNull(testCase.displayName)
            assertNotNull(testCase.description)
            assertTrue(testCase.displayName.isNotEmpty())
            assertTrue(testCase.description.isNotEmpty())
        }
    }

    @Test
    fun exportRepository_exportFormat_integration() {
        val formats = ExportRepository.ExportFormat.values()

        formats.forEach { format ->
            assertNotNull(format.extension)
            assertNotNull(format.mimeType)
            assertTrue(format.mimeType.contains("/"))
        }
    }

    @Test
    fun logcatRepository_category_matching_integration() {
        val repository = LogcatRepository()

        // Test IMS category matching
        assertTrue(repository.matchesCategory("ImsManager", LogcatRepository.LogCategory.IMS))
        assertTrue(repository.matchesCategory("ImsPhone", LogcatRepository.LogCategory.IMS))
        assertTrue(repository.matchesCategory("ImsService", LogcatRepository.LogCategory.IMS))

        // Test CarrierConfig category matching
        assertTrue(repository.matchesCategory("CarrierConfigManager", LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertTrue(repository.matchesCategory("CarrierConfigLoader", LogcatRepository.LogCategory.CARRIER_CONFIG))

        // Test Telephony category matching
        assertTrue(repository.matchesCategory("TelephonyManager", LogcatRepository.LogCategory.TELEPHONY))
        assertTrue(repository.matchesCategory("GsmCdmaPhone", LogcatRepository.LogCategory.TELEPHONY))

        // Test WFC category matching
        assertTrue(repository.matchesCategory("WifiCalling", LogcatRepository.LogCategory.WFC))

        // Test ALL category matches everything
        assertTrue(repository.matchesCategory("AnyTag", LogcatRepository.LogCategory.ALL))
    }

    @Test
    fun logcatRepository_logLevel_filtering_integration() {
        val repository = LogcatRepository()

        // INFO level should filter V and D
        assertFalse(repository.matchesLogLevel("V", LogcatRepository.LogLevel.INFO))
        assertFalse(repository.matchesLogLevel("D", LogcatRepository.LogLevel.INFO))
        assertTrue(repository.matchesLogLevel("I", LogcatRepository.LogLevel.INFO))
        assertTrue(repository.matchesLogLevel("W", LogcatRepository.LogLevel.INFO))
        assertTrue(repository.matchesLogLevel("E", LogcatRepository.LogLevel.INFO))

        // ERROR level should only show E and F
        assertFalse(repository.matchesLogLevel("V", LogcatRepository.LogLevel.ERROR))
        assertFalse(repository.matchesLogLevel("D", LogcatRepository.LogLevel.ERROR))
        assertFalse(repository.matchesLogLevel("I", LogcatRepository.LogLevel.ERROR))
        assertFalse(repository.matchesLogLevel("W", LogcatRepository.LogLevel.ERROR))
        assertTrue(repository.matchesLogLevel("E", LogcatRepository.LogLevel.ERROR))
        assertTrue(repository.matchesLogLevel("F", LogcatRepository.LogLevel.ERROR))
    }

    @Test
    fun dumpsysRepository_ims_extraction_variations_integration() {
        val repository = DumpsysRepository()

        // Test with WLAN registration
        val wlanOutput = """
            IMS Registration: REGISTERED
            Registration type: WLAN
            IMS Voice: true
        """.trimIndent()
        val wlanInfo = repository.extractImsInfo(wlanOutput)
        assertTrue(wlanInfo.contains("Type: Wi-Fi"))

        // Test with CELLULAR registration
        val cellularOutput = """
            IMS Registration: REGISTERED
            Registration type: CELLULAR
            IMS Voice: true
        """.trimIndent()
        val cellularInfo = repository.extractImsInfo(cellularOutput)
        assertTrue(cellularInfo.contains("Type: Cellular"))

        // Test with NOT_REGISTERED
        val notRegisteredOutput = """
            IMS Registration: NOT_REGISTERED
        """.trimIndent()
        val notRegisteredInfo = repository.extractImsInfo(notRegisteredOutput)
        assertTrue(notRegisteredInfo.contains("Status: Not Registered"))
    }

    @Test
    fun logcatRepository_multiline_log_parsing_integration() {
        val repository = LogcatRepository()
        val multilineLog = """
            01-15 10:30:45.123  1234  5678 E TestTag: Exception occurred:
            	at com.example.Class.method(Class.java:123)
            	at com.example.Main.main(Main.java:45)
        """.trimIndent()

        val entry = repository.parseLogEntry(multilineLog)

        assertNotNull(entry)
        assertEquals("E", entry?.level)
        assertTrue(entry?.message?.contains("Exception occurred") == true)
    }

    @Test
    fun exportRepository_data_serialization_integration() {
        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = mapOf(
                "manufacturer" to "Samsung",
                "model" to "SM-S911U",
                "android" to "14"
            ),
            carrierConfig = mapOf(
                "carrier_volte_available_bool" to "true",
                "carrier_vt_available_bool" to "true"
            ),
            preferences = mapOf(
                "theme_mode" to "dark",
                "auto_refresh_enabled" to "true"
            ),
            diagnostics = mapOf(
                "ims_registered" to "true",
                "volte_enabled" to "true",
                "vowifi_enabled" to "true"
            )
        )

        // Verify all data is present
        assertEquals(3, exportData.deviceInfo.size)
        assertEquals(2, exportData.carrierConfig.size)
        assertEquals(2, exportData.preferences.size)
        assertEquals(3, exportData.diagnostics.size)

        assertTrue(exportData.deviceInfo.containsKey("manufacturer"))
        assertTrue(exportData.carrierConfig.containsKey("carrier_volte_available_bool"))
        assertTrue(exportData.preferences.containsKey("theme_mode"))
        assertTrue(exportData.diagnostics.containsKey("ims_registered"))
    }

    @Test
    fun connectivity_test_result_types_integration() {
        // Test Passed result
        val passed = ConnectivityTestRepository.TestResult.Passed("Test passed")
        assertTrue(passed is ConnectivityTestRepository.TestResult.Passed)
        assertEquals("Test passed", passed.message)

        // Test Failed result
        val failed = ConnectivityTestRepository.TestResult.Failed("Test failed")
        assertTrue(failed is ConnectivityTestRepository.TestResult.Failed)
        assertEquals("Test failed", failed.message)

        // Test Error result
        val error = ConnectivityTestRepository.TestResult.Error(Exception("Error"))
        assertTrue(error is ConnectivityTestRepository.TestResult.Error)
        assertNotNull(error.exception)

        // Test Skipped result
        val skipped = ConnectivityTestRepository.TestResult.Skipped("Test skipped")
        assertTrue(skipped is ConnectivityTestRepository.TestResult.Skipped)
        assertEquals("Test skipped", skipped.reason)
    }

    @Test
    fun logcatRepository_special_characters_integration() {
        val repository = LogcatRepository()
        
        val specialLogs = listOf(
            "01-15 10:30:45.123  1234  5678 I TestTag: Message with [brackets]",
            "01-15 10:30:45.123  1234  5678 I TestTag: Message with (parentheses)",
            "01-15 10:30:45.123  1234  5678 I TestTag: Message with {braces}",
            "01-15 10:30:45.123  1234  5678 I TestTag: Message with <angles>",
            "01-15 10:30:45.123  1234  5678 I TestTag: Message with 'quotes'"
        )

        specialLogs.forEach { log ->
            val entry = repository.parseLogEntry(log)
            assertNotNull("Failed to parse: $log", entry)
            assertTrue(entry?.message?.isNotEmpty() == true)
        }
    }

    @Test
    fun dumpsysRepository_empty_output_handling_integration() {
        val repository = DumpsysRepository()

        val emptyInfo = repository.extractImsInfo("")
        assertTrue(emptyInfo.contains("Status: Unknown"))

        val whitespaceInfo = repository.extractImsInfo("   \n\n   ")
        assertTrue(whitespaceInfo.contains("Status: Unknown"))
    }

    @Test
    fun end_to_end_diagnostic_flow_simulation() = runTest {
        // Simulate a complete diagnostic workflow
        
        // 1. Parse logcat entries
        val logcatRepo = LogcatRepository()
        val logEntry = logcatRepo.parseLogEntry(
            "01-15 10:30:45.123  1234  5678 I ImsManager: Registration successful"
        )
        assertNotNull(logEntry)
        assertTrue(logcatRepo.matchesCategory(logEntry!!.tag, LogcatRepository.LogCategory.IMS))

        // 2. Extract dumpsys info
        val dumpsysRepo = DumpsysRepository()
        val dumpsysOutput = """
            IMS Registration: REGISTERED
            IMS Voice: true
            Registration type: WLAN
        """.trimIndent()
        val imsInfo = dumpsysRepo.extractImsInfo(dumpsysOutput)
        assertTrue(imsInfo.contains("Registered"))

        // 3. Create export data
        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = mapOf("test" to "device"),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = mapOf("ims_info" to imsInfo)
        )
        
        assertTrue(exportData.diagnostics.containsKey("ims_info"))
    }
}
