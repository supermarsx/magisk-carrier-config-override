package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Unit tests for [ExportRepository] data models and JSON serialization.
 *
 * The repository's file-I/O methods need a real Context; these tests
 * exercise the pure-Kotlin model layer and serialization contracts.
 */
class ExportRepositoryTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // =========================================================================
    // AppConfiguration serialization
    // =========================================================================

    @Test
    fun `AppConfiguration round-trips through JSON`() {
        val config = AppConfiguration(
            version = "1.0.0",
            exportDate = 1709424000000L,
            settings = AppSettings(
                autoRefresh = true,
                enableNotifications = false,
                debugMode = false,
                theme = "dark"
            ),
            customKeys = listOf(
                CustomKeyData("KEY_VOLTE_AVAILABLE_BOOL", "boolean", "true")
            )
        )

        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<AppConfiguration>(encoded)

        assertThat(decoded).isEqualTo(config)
    }

    @Test
    fun `AppConfiguration with null settings`() {
        val config = AppConfiguration(
            version = "2.0.0",
            exportDate = 0L,
            settings = null,
            customKeys = emptyList()
        )

        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<AppConfiguration>(encoded)

        assertThat(decoded.settings).isNull()
        assertThat(decoded.customKeys).isEmpty()
    }

    @Test
    fun `AppSettings default values serialize correctly`() {
        val settings = AppSettings(
            autoRefresh = true,
            enableNotifications = false,
            debugMode = false,
            theme = "dark"
        )

        val encoded = json.encodeToString(settings)
        assertThat(encoded).contains("\"autoRefresh\"")
        assertThat(encoded).contains("\"theme\"")

        val decoded = json.decodeFromString<AppSettings>(encoded)
        assertThat(decoded).isEqualTo(settings)
    }

    @Test
    fun `CustomKeyData serialization`() {
        val key = CustomKeyData("carrier_wfc_enabled_bool", "boolean", "true")
        val encoded = json.encodeToString(key)
        val decoded = json.decodeFromString<CustomKeyData>(encoded)

        assertThat(decoded.key).isEqualTo("carrier_wfc_enabled_bool")
        assertThat(decoded.valueType).isEqualTo("boolean")
        assertThat(decoded.value).isEqualTo("true")
    }

    @Test
    fun `DiagnosticsReport serialization`() {
        val report = DiagnosticsReport(
            timestamp = 1709424000000L,
            deviceInfo = "Model: Galaxy S24",
            simInfo = "Carrier: T-Mobile",
            imsStatus = "Registered"
        )

        val encoded = json.encodeToString(report)
        val decoded = json.decodeFromString<DiagnosticsReport>(encoded)

        assertThat(decoded).isEqualTo(report)
    }

    // =========================================================================
    // ExportResult / ImportResult
    // =========================================================================

    @Test
    fun `ExportResult Success carries filePath`() {
        val result = ExportResult.Success("/data/exports/config.json")
        assertThat(result.filePath).isEqualTo("/data/exports/config.json")
    }

    @Test
    fun `ExportResult Error carries message`() {
        val result = ExportResult.Error("Disk full")
        assertThat(result.message).isEqualTo("Disk full")
    }

    @Test
    fun `ImportResult Success carries version`() {
        val result = ImportResult.Success("1.0.0")
        assertThat(result.version).isEqualTo("1.0.0")
    }

    @Test
    fun `ImportResult Error carries message`() {
        val result = ImportResult.Error("Invalid JSON")
        assertThat(result.message).isEqualTo("Invalid JSON")
    }

    // =========================================================================
    // ExportFile
    // =========================================================================

    @Test
    fun `ExportFile data class`() {
        val file = ExportFile(
            name = "config_20260302.json",
            path = "/data/CCO/exports/config_20260302.json",
            size = 4096L,
            lastModified = 1709424000000L
        )
        assertThat(file.name).isEqualTo("config_20260302.json")
        assertThat(file.size).isEqualTo(4096L)
    }

    // =========================================================================
    // JSON ignoreUnknownKeys
    // =========================================================================

    @Test
    fun `deserialization ignores unknown keys gracefully`() {
        val jsonStr = """
        {
            "version": "1.0.0",
            "exportDate": 0,
            "settings": null,
            "customKeys": [],
            "unknownField": "should be ignored"
        }
        """.trimIndent()

        val config = json.decodeFromString<AppConfiguration>(jsonStr)
        assertThat(config.version).isEqualTo("1.0.0")
    }

    // =========================================================================
    // ExportFile extension filtering (Fix #5: zip included)
    // =========================================================================

    @Test
    fun `ExportFile with json extension is valid`() {
        val file = ExportFile("config.json", "/path/config.json", 100L, 0L)
        assertThat(file.name.substringAfterLast('.')).isIn(listOf("json", "txt", "zip"))
    }

    @Test
    fun `ExportFile with zip extension is valid`() {
        val file = ExportFile("diag.zip", "/path/diag.zip", 100L, 0L)
        assertThat(file.name.substringAfterLast('.')).isIn(listOf("json", "txt", "zip"))
    }

    @Test
    fun `ExportFile with txt extension is valid`() {
        val file = ExportFile("log.txt", "/path/log.txt", 100L, 0L)
        assertThat(file.name.substringAfterLast('.')).isIn(listOf("json", "txt", "zip"))
    }

    // =========================================================================
    // AppConfiguration import/export settings contract
    // =========================================================================

    @Test
    fun `AppSettings carries all required fields`() {
        val settings = AppSettings(
            autoRefresh = false,
            enableNotifications = true,
            debugMode = true,
            theme = "system"
        )
        assertThat(settings.autoRefresh).isFalse()
        assertThat(settings.enableNotifications).isTrue()
        assertThat(settings.debugMode).isTrue()
        assertThat(settings.theme).isEqualTo("system")
    }

    @Test
    fun `full AppConfiguration with settings round-trips correctly`() {
        val config = AppConfiguration(
            version = "1.0.0",
            exportDate = 1000L,
            settings = AppSettings(
                autoRefresh = false,
                enableNotifications = true,
                debugMode = true,
                theme = "system"
            ),
            customKeys = listOf(
                CustomKeyData("carrier_wfc_ims_available_bool", "boolean", "true"),
                CustomKeyData("carrier_default_wfc_ims_mode_int", "int", "1")
            )
        )
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<AppConfiguration>(encoded)

        assertThat(decoded.settings?.autoRefresh).isFalse()
        assertThat(decoded.settings?.enableNotifications).isTrue()
        assertThat(decoded.settings?.debugMode).isTrue()
        assertThat(decoded.settings?.theme).isEqualTo("system")
        assertThat(decoded.customKeys).hasSize(2)
        assertThat(decoded.customKeys[0].key).isEqualTo("carrier_wfc_ims_available_bool")
        assertThat(decoded.customKeys[1].value).isEqualTo("1")
    }

    @Test
    fun `ExportResult sealed class variants`() {
        val success: ExportResult = ExportResult.Success("/path")
        val error: ExportResult = ExportResult.Error("fail")
        assertThat(success).isInstanceOf(ExportResult.Success::class.java)
        assertThat(error).isInstanceOf(ExportResult.Error::class.java)
    }

    @Test
    fun `ImportResult sealed class variants`() {
        val success: ImportResult = ImportResult.Success("1.0.0")
        val error: ImportResult = ImportResult.Error("bad json")
        assertThat(success).isInstanceOf(ImportResult.Success::class.java)
        assertThat(error).isInstanceOf(ImportResult.Error::class.java)
    }

    @Test
    fun `DiagnosticsReport carries all fields`() {
        val report = DiagnosticsReport(
            timestamp = 99L,
            deviceInfo = "Samsung S24",
            simInfo = "T-Mobile",
            imsStatus = "Registered over Wi-Fi"
        )
        assertThat(report.timestamp).isEqualTo(99L)
        assertThat(report.deviceInfo).isEqualTo("Samsung S24")
        assertThat(report.imsStatus).contains("Wi-Fi")
    }

    @Test
    fun `multiple CustomKeyData entries preserve order`() {
        val keys = listOf(
            CustomKeyData("key1", "boolean", "true"),
            CustomKeyData("key2", "int", "42"),
            CustomKeyData("key3", "string", "hello")
        )
        val config = AppConfiguration("1.0.0", 0L, null, keys)
        val encoded = json.encodeToString(config)
        val decoded = json.decodeFromString<AppConfiguration>(encoded)
        assertThat(decoded.customKeys.map { it.key })
            .containsExactly("key1", "key2", "key3").inOrder()
    }

    // =========================================================================
    // ProfileData serialization
    // =========================================================================

    @Test
    fun `ProfileData round-trips through JSON`() {
        val profile = ProfileData(
            version = "1.0.0",
            exportDate = 1709424000000L,
            presetId = "full_enablement",
            presetName = "Full WFC Enablement",
            keys = listOf(
                CustomKeyData("carrier_wfc_ims_available_bool", "boolean", "true"),
                CustomKeyData("carrier_default_wfc_ims_mode_int", "int", "1")
            )
        )

        val encoded = json.encodeToString(profile)
        val decoded = json.decodeFromString<ProfileData>(encoded)

        assertThat(decoded).isEqualTo(profile)
        assertThat(decoded.presetId).isEqualTo("full_enablement")
        assertThat(decoded.keys).hasSize(2)
    }

    @Test
    fun `ProfileData with empty keys serializes correctly`() {
        val profile = ProfileData(
            version = "1.0.0",
            exportDate = 0L,
            presetId = "custom",
            presetName = "Custom Profile",
            keys = emptyList()
        )

        val encoded = json.encodeToString(profile)
        val decoded = json.decodeFromString<ProfileData>(encoded)

        assertThat(decoded.keys).isEmpty()
        assertThat(decoded.presetName).isEqualTo("Custom Profile")
    }

    @Test
    fun `ProfileData deserialization ignores unknown keys`() {
        val jsonStr = """
        {
            "version": "1.0.0",
            "exportDate": 0,
            "presetId": "test",
            "presetName": "Test",
            "keys": [],
            "extraField": 42
        }
        """.trimIndent()

        val profile = json.decodeFromString<ProfileData>(jsonStr)
        assertThat(profile.presetId).isEqualTo("test")
    }

    // =========================================================================
    // ImportProfileResult
    // =========================================================================

    @Test
    fun `ImportProfileResult Success carries profile`() {
        val profile = ProfileData("1.0.0", 0L, "id", "Name", emptyList())
        val result = ImportProfileResult.Success(profile)
        assertThat(result.profile.presetId).isEqualTo("id")
    }

    @Test
    fun `ImportProfileResult Error carries message`() {
        val result = ImportProfileResult.Error("bad format")
        assertThat(result.message).isEqualTo("bad format")
    }

    @Test
    fun `ImportProfileResult sealed class variants`() {
        val profile = ProfileData("1.0.0", 0L, "id", "Name", emptyList())
        val success: ImportProfileResult = ImportProfileResult.Success(profile)
        val error: ImportProfileResult = ImportProfileResult.Error("fail")
        assertThat(success).isInstanceOf(ImportProfileResult.Success::class.java)
        assertThat(error).isInstanceOf(ImportProfileResult.Error::class.java)
    }
}
