package dev.mars.carrierconfig.data.repository

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ExportRepositoryTest {

    @Test
    fun `ExportFormat enum contains expected formats`() {
        val formats = ExportRepository.ExportFormat.values()

        assertEquals(3, formats.size)
        assertTrue(formats.contains(ExportRepository.ExportFormat.JSON))
        assertTrue(formats.contains(ExportRepository.ExportFormat.XML))
        assertTrue(formats.contains(ExportRepository.ExportFormat.CSV))
    }

    @Test
    fun `ExportFormat has correct extensions`() {
        assertEquals("json", ExportRepository.ExportFormat.JSON.extension)
        assertEquals("xml", ExportRepository.ExportFormat.XML.extension)
        assertEquals("csv", ExportRepository.ExportFormat.CSV.extension)
    }

    @Test
    fun `ExportFormat has correct MIME types`() {
        assertEquals("application/json", ExportRepository.ExportFormat.JSON.mimeType)
        assertEquals("application/xml", ExportRepository.ExportFormat.XML.mimeType)
        assertEquals("text/csv", ExportRepository.ExportFormat.CSV.mimeType)
    }

    @Test
    fun `ExportData data class has all required fields`() {
        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = mapOf("model" to "Test Device"),
            carrierConfig = mapOf("key1" to "value1"),
            preferences = mapOf("theme" to "dark"),
            diagnostics = mapOf("ims" to "registered")
        )

        assertTrue(exportData.timestamp > 0)
        assertEquals("1.0.0", exportData.appVersion)
        assertEquals(1, exportData.deviceInfo.size)
        assertEquals(1, exportData.carrierConfig.size)
        assertEquals(1, exportData.preferences.size)
        assertEquals(1, exportData.diagnostics.size)
    }

    @Test
    fun `ExportData handles empty maps`() {
        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = emptyMap(),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertTrue(exportData.deviceInfo.isEmpty())
        assertTrue(exportData.carrierConfig.isEmpty())
        assertTrue(exportData.preferences.isEmpty())
        assertTrue(exportData.diagnostics.isEmpty())
    }

    @Test
    fun `ExportData with complex nested data`() {
        val complexConfig = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "nested" to "complex_value",
            "array_key" to "[1,2,3]"
        )

        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = mapOf("model" to "Test"),
            carrierConfig = complexConfig,
            preferences = mapOf("setting" to "value"),
            diagnostics = mapOf("status" to "ok")
        )

        assertEquals(4, exportData.carrierConfig.size)
        assertTrue(exportData.carrierConfig.containsKey("nested"))
        assertTrue(exportData.carrierConfig.containsKey("array_key"))
    }

    @Test
    fun `ExportData timestamp is valid`() {
        val now = System.currentTimeMillis()
        val exportData = ExportRepository.ExportData(
            timestamp = now,
            appVersion = "1.0.0",
            deviceInfo = emptyMap(),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        // Timestamp should be close to current time (within 1 second)
        assertTrue(Math.abs(exportData.timestamp - now) < 1000)
    }

    @Test
    fun `ExportData appVersion format`() {
        val versions = listOf("1.0.0", "1.0.0-beta", "1.0.0-debug", "2.5.3")

        versions.forEach { version ->
            val exportData = ExportRepository.ExportData(
                timestamp = System.currentTimeMillis(),
                appVersion = version,
                deviceInfo = emptyMap(),
                carrierConfig = emptyMap(),
                preferences = emptyMap(),
                diagnostics = emptyMap()
            )

            assertEquals(version, exportData.appVersion)
            assertTrue(exportData.appVersion.isNotEmpty())
        }
    }

    @Test
    fun `ExportData with special characters in values`() {
        val specialChars = mapOf(
            "key1" to "value with spaces",
            "key2" to "value,with,commas",
            "key3" to "value\"with\"quotes",
            "key4" to "value'with'apostrophes",
            "key5" to "value<with>brackets"
        )

        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = specialChars,
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertEquals(5, exportData.deviceInfo.size)
        assertTrue(exportData.deviceInfo["key1"]?.contains(" ") == true)
        assertTrue(exportData.deviceInfo["key2"]?.contains(",") == true)
    }

    @Test
    fun `ExportData with Unicode characters`() {
        val unicodeData = mapOf(
            "emoji" to "📱✓",
            "chinese" to "中文",
            "arabic" to "العربية",
            "symbols" to "©®™"
        )

        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = unicodeData,
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertEquals(4, exportData.deviceInfo.size)
        assertTrue(exportData.deviceInfo["emoji"]?.contains("📱") == true)
        assertTrue(exportData.deviceInfo["chinese"]?.contains("中文") == true)
    }

    @Test
    fun `ExportData with large data sets`() {
        val largeConfig = (1..1000).associate { "key$it" to "value$it" }

        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = emptyMap(),
            carrierConfig = largeConfig,
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertEquals(1000, exportData.carrierConfig.size)
        assertTrue(exportData.carrierConfig.containsKey("key1"))
        assertTrue(exportData.carrierConfig.containsKey("key1000"))
    }

    @Test
    fun `ExportData with null-like string values`() {
        val nullishData = mapOf(
            "key1" to "null",
            "key2" to "undefined",
            "key3" to "",
            "key4" to "   "
        )

        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = nullishData,
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertEquals(4, exportData.deviceInfo.size)
        assertEquals("null", exportData.deviceInfo["key1"])
        assertEquals("", exportData.deviceInfo["key3"])
    }

    @Test
    fun `ExportFormat extensions are lowercase`() {
        ExportRepository.ExportFormat.values().forEach { format ->
            assertEquals(format.extension, format.extension.lowercase())
            assertFalse(format.extension.contains("."))
        }
    }

    @Test
    fun `ExportFormat MIME types are valid`() {
        ExportRepository.ExportFormat.values().forEach { format ->
            assertTrue(format.mimeType.contains("/"))
            val parts = format.mimeType.split("/")
            assertEquals(2, parts.size)
            assertTrue(parts[0].isNotEmpty())
            assertTrue(parts[1].isNotEmpty())
        }
    }

    @Test
    fun `ExportFormat matches file extension to MIME type`() {
        assertEquals("json", ExportRepository.ExportFormat.JSON.extension)
        assertTrue(ExportRepository.ExportFormat.JSON.mimeType.contains("json"))

        assertEquals("xml", ExportRepository.ExportFormat.XML.extension)
        assertTrue(ExportRepository.ExportFormat.XML.mimeType.contains("xml"))

        assertEquals("csv", ExportRepository.ExportFormat.CSV.extension)
        assertTrue(ExportRepository.ExportFormat.CSV.mimeType.contains("csv"))
    }

    @Test
    fun `ExportData equality and hashCode`() {
        val timestamp = System.currentTimeMillis()
        val data1 = ExportRepository.ExportData(
            timestamp = timestamp,
            appVersion = "1.0.0",
            deviceInfo = mapOf("key" to "value"),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        val data2 = ExportRepository.ExportData(
            timestamp = timestamp,
            appVersion = "1.0.0",
            deviceInfo = mapOf("key" to "value"),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertEquals(data1, data2)
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun `ExportData with different timestamps are not equal`() {
        val data1 = ExportRepository.ExportData(
            timestamp = 1000L,
            appVersion = "1.0.0",
            deviceInfo = emptyMap(),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        val data2 = ExportRepository.ExportData(
            timestamp = 2000L,
            appVersion = "1.0.0",
            deviceInfo = emptyMap(),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        assertNotEquals(data1, data2)
    }

    @Test
    fun `ExportData toString provides useful information`() {
        val exportData = ExportRepository.ExportData(
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            deviceInfo = mapOf("model" to "Test"),
            carrierConfig = mapOf("key" to "value"),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        val toString = exportData.toString()
        assertTrue(toString.contains("ExportData") || toString.contains("1.0.0"))
    }

    @Test
    fun `ExportData copy works correctly`() {
        val original = ExportRepository.ExportData(
            timestamp = 1000L,
            appVersion = "1.0.0",
            deviceInfo = mapOf("key" to "value"),
            carrierConfig = emptyMap(),
            preferences = emptyMap(),
            diagnostics = emptyMap()
        )

        val copy = original.copy(appVersion = "2.0.0")

        assertEquals(original.timestamp, copy.timestamp)
        assertEquals("2.0.0", copy.appVersion)
        assertEquals(original.deviceInfo, copy.deviceInfo)
    }

    @Test
    fun `ExportFormat enum order is sensible`() {
        val formats = ExportRepository.ExportFormat.values()

        // JSON should be first (most common)
        assertEquals(ExportRepository.ExportFormat.JSON, formats[0])
        
        // XML second (structured)
        assertEquals(ExportRepository.ExportFormat.XML, formats[1])
        
        // CSV last (simple)
        assertEquals(ExportRepository.ExportFormat.CSV, formats[2])
    }
}
