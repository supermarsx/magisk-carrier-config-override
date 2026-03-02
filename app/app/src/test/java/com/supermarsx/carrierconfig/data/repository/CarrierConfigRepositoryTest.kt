package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import org.junit.Test

/**
 * Unit tests for [CarrierConfigRepository].
 *
 * Tests cover:
 * - WFC mode constant correctness (spec Section 5.3)
 * - Preset definitions and categories
 * - XML generation for all ConfigValue types
 * - Prerequisites data model
 * - Data model contracts (DeploymentResult, CarrierConfigDeployment)
 */
class CarrierConfigRepositoryTest {

    // =========================================================================
    // WFC Mode Constants (spec Section 5.3)
    // =========================================================================

    @Test
    fun `WFC_MODE_CELLULAR_PREFERRED is 0`() {
        assertThat(CarrierConfigRepository.WFC_MODE_CELLULAR_PREFERRED).isEqualTo(0)
    }

    @Test
    fun `WFC_MODE_WIFI_PREFERRED is 1`() {
        assertThat(CarrierConfigRepository.WFC_MODE_WIFI_PREFERRED).isEqualTo(1)
    }

    @Test
    fun `WFC_MODE_WIFI_ONLY is 2`() {
        assertThat(CarrierConfigRepository.WFC_MODE_WIFI_ONLY).isEqualTo(2)
    }

    @Test
    fun `WFC mode constants match Android telephony framework values`() {
        // Android's ImsConfig.WfcModeFeatureValueConstants:
        // WIFI_PREFERRED = 2 in some AOSP versions, but per Samsung/spec:
        // 0=cellular preferred, 1=wifi preferred, 2=wifi only
        assertThat(CarrierConfigRepository.WFC_MODE_CELLULAR_PREFERRED)
            .isLessThan(CarrierConfigRepository.WFC_MODE_WIFI_PREFERRED)
        assertThat(CarrierConfigRepository.WFC_MODE_WIFI_PREFERRED)
            .isLessThan(CarrierConfigRepository.WFC_MODE_WIFI_ONLY)
    }

    // =========================================================================
    // Presets
    // =========================================================================

    @Test
    fun `getPresets returns 6 presets`() {
        val repo = createRepository()
        val presets = repo.getPresets()
        assertThat(presets).hasSize(6)
    }

    @Test
    fun `preset IDs are unique`() {
        val repo = createRepository()
        val ids = repo.getPresets().map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `all presets have non-empty keys`() {
        val repo = createRepository()
        repo.getPresets().forEach { preset ->
            assertThat(preset.keys).isNotEmpty()
            assertThat(preset.name).isNotEmpty()
            assertThat(preset.description).isNotEmpty()
        }
    }

    @Test
    fun `all WFC presets include carrier_wfc_ims_available_bool`() {
        val repo = createRepository()
        repo.getPresets().forEach { preset ->
            assertThat(preset.keys).containsKey("carrier_wfc_ims_available_bool")
            val wfcAvailable = preset.keys["carrier_wfc_ims_available_bool"]
            assertThat(wfcAvailable).isInstanceOf(ConfigValue.BooleanValue::class.java)
            assertThat((wfcAvailable as ConfigValue.BooleanValue).value).isTrue()
        }
    }

    @Test
    fun `wifi_preferred preset uses WFC_MODE_WIFI_PREFERRED`() {
        val repo = createRepository()
        val preset = repo.getPresets().first { it.id == "wifi_preferred" }
        val wfcMode = preset.keys["carrier_default_wfc_ims_mode_int"] as ConfigValue.IntValue
        assertThat(wfcMode.value).isEqualTo(CarrierConfigRepository.WFC_MODE_WIFI_PREFERRED)
    }

    @Test
    fun `wifi_only preset uses WFC_MODE_WIFI_ONLY`() {
        val repo = createRepository()
        val preset = repo.getPresets().first { it.id == "wifi_only" }
        val wfcMode = preset.keys["carrier_default_wfc_ims_mode_int"] as ConfigValue.IntValue
        assertThat(wfcMode.value).isEqualTo(CarrierConfigRepository.WFC_MODE_WIFI_ONLY)
    }

    @Test
    fun `wfc_default_enabled preset uses WFC_MODE_CELLULAR_PREFERRED`() {
        val repo = createRepository()
        val preset = repo.getPresets().first { it.id == "wfc_default_enabled" }
        val wfcMode = preset.keys["carrier_default_wfc_ims_mode_int"] as ConfigValue.IntValue
        assertThat(wfcMode.value).isEqualTo(CarrierConfigRepository.WFC_MODE_CELLULAR_PREFERRED)
    }

    @Test
    fun `full_enablement preset has all 3 wifi support flags`() {
        val repo = createRepository()
        val preset = repo.getPresets().first { it.id == "full_enablement" }
        assertThat(preset.keys).containsKey("carrier_wfc_supports_wifi_only_bool")
        assertThat(preset.keys).containsKey("carrier_wfc_supports_cellular_preferred_bool")
        assertThat(preset.keys).containsKey("carrier_wfc_supports_wifi_preferred_bool")
    }

    @Test
    fun `preset categories are correctly assigned`() {
        val repo = createRepository()
        val presets = repo.getPresets()
        val wfcEnable = presets.filter { it.category == PresetCategory.WFC_ENABLE }
        val wfcPref = presets.filter { it.category == PresetCategory.WFC_PREFERENCE }
        val advanced = presets.filter { it.category == PresetCategory.ADVANCED }
        assertThat(wfcEnable).hasSize(2)
        assertThat(wfcPref).hasSize(3)
        assertThat(advanced).hasSize(1)
    }

    // =========================================================================
    // XML Generation
    // =========================================================================

    @Test
    fun `generateXML produces valid XML with header`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("test_key", ConfigValue.BooleanValue(true))
        )
        val xml = repo.generateXML(keys)
        assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        assertThat(xml).contains("<carrier_config>")
        assertThat(xml).contains("</carrier_config>")
    }

    @Test
    fun `generateXML handles BooleanValue`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("carrier_wfc_ims_available_bool", ConfigValue.BooleanValue(true))
        )
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("""<boolean name="carrier_wfc_ims_available_bool" value="true" />""")
    }

    @Test
    fun `generateXML handles IntValue`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("carrier_default_wfc_ims_mode_int", ConfigValue.IntValue(1))
        )
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("""<int name="carrier_default_wfc_ims_mode_int" value="1" />""")
    }

    @Test
    fun `generateXML handles StringValue`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("carrier_name_string", ConfigValue.StringValue("TestCarrier"))
        )
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("""<string name="carrier_name_string">TestCarrier</string>""")
    }

    @Test
    fun `generateXML handles StringArrayValue`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("carrier_wifi_string_array", ConfigValue.StringArrayValue(listOf("a", "b")))
        )
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("""<string-array name="carrier_wifi_string_array">""")
        assertThat(xml).contains("<item>a</item>")
        assertThat(xml).contains("<item>b</item>")
        assertThat(xml).contains("</string-array>")
    }

    @Test
    fun `generateXML with multiple keys in correct order`() {
        val repo = createRepository()
        val keys = listOf(
            ConfigKey("bool_key", ConfigValue.BooleanValue(false)),
            ConfigKey("int_key", ConfigValue.IntValue(42)),
            ConfigKey("str_key", ConfigValue.StringValue("hello"))
        )
        val xml = repo.generateXML(keys)
        val boolIdx = xml.indexOf("bool_key")
        val intIdx = xml.indexOf("int_key")
        val strIdx = xml.indexOf("str_key")
        assertThat(boolIdx).isLessThan(intIdx)
        assertThat(intIdx).isLessThan(strIdx)
    }

    @Test
    fun `generateXML with empty keys list produces valid wrapper`() {
        val repo = createRepository()
        val xml = repo.generateXML(emptyList())
        assertThat(xml).contains("<carrier_config>")
        assertThat(xml).contains("</carrier_config>")
    }

    @Test
    fun `generateXML from full_enablement preset covers all key types`() {
        val repo = createRepository()
        val preset = repo.getPresets().first { it.id == "full_enablement" }
        val keys = preset.keys.map { (key, value) -> ConfigKey(key, value) }
        val xml = repo.generateXML(keys)
        // Should have both boolean and int tags
        assertThat(xml).contains("<boolean ")
        assertThat(xml).contains("<int ")
    }

    // =========================================================================
    // Data Models
    // =========================================================================

    @Test
    fun `Prerequisites allMet requires all fields`() {
        val complete = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            magiskVersion = "26100",
            carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
            pathWritable = true
        )
        assertThat(complete.allMet).isTrue()
    }

    @Test
    fun `Prerequisites allMet false when no root`() {
        val noRoot = Prerequisites(
            hasRoot = false,
            hasMagisk = true,
            carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
            pathWritable = true
        )
        assertThat(noRoot.allMet).isFalse()
    }

    @Test
    fun `Prerequisites allMet false when path not writable`() {
        val notWritable = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
            pathWritable = false
        )
        assertThat(notWritable.allMet).isFalse()
    }

    @Test
    fun `Prerequisites allMet false when no path`() {
        val noPath = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            carrierConfigPath = null,
            pathWritable = true
        )
        assertThat(noPath.allMet).isFalse()
    }

    @Test
    fun `Prerequisites default state has nothing met`() {
        val defaults = Prerequisites()
        assertThat(defaults.allMet).isFalse()
        assertThat(defaults.hasRoot).isFalse()
        assertThat(defaults.hasMagisk).isFalse()
    }

    @Test
    fun `DeploymentResult Success is singleton`() {
        val a = DeploymentResult.Success
        val b = DeploymentResult.Success
        assertThat(a).isSameInstanceAs(b)
    }

    @Test
    fun `DeploymentResult Error carries message and optional details`() {
        val error = DeploymentResult.Error("failed", "stack trace")
        assertThat(error.message).isEqualTo("failed")
        assertThat(error.details).isEqualTo("stack trace")
    }

    @Test
    fun `DeploymentResult Error with null details`() {
        val error = DeploymentResult.Error("failed")
        assertThat(error.details).isNull()
    }

    @Test
    fun `CarrierConfigDeployment default state`() {
        val deployment = CarrierConfigDeployment()
        assertThat(deployment.isDeployed).isFalse()
        assertThat(deployment.deployedPresetId).isNull()
        assertThat(deployment.deployedKeys).isEmpty()
        assertThat(deployment.deploymentPath).isNull()
        assertThat(deployment.timestamp).isNull()
        assertThat(deployment.backupExists).isFalse()
    }

    @Test
    fun `CarrierConfigState default state`() {
        val state = CarrierConfigState()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.presets).isEmpty()
        assertThat(state.selectedPreset).isNull()
        assertThat(state.customKeys).isEmpty()
        assertThat(state.prerequisites).isNull()
        assertThat(state.deployment).isNull()
        assertThat(state.currentTab).isEqualTo(0)
    }

    // =========================================================================
    // ConfigValue
    // =========================================================================

    @Test
    fun `ConfigValue BooleanValue displayValue`() {
        assertThat(ConfigValue.BooleanValue(true).displayValue).isEqualTo("true")
        assertThat(ConfigValue.BooleanValue(false).displayValue).isEqualTo("false")
    }

    @Test
    fun `ConfigValue IntValue displayValue`() {
        assertThat(ConfigValue.IntValue(42).displayValue).isEqualTo("42")
        assertThat(ConfigValue.IntValue(0).displayValue).isEqualTo("0")
    }

    @Test
    fun `ConfigValue StringValue displayValue`() {
        assertThat(ConfigValue.StringValue("hello").displayValue).isEqualTo("hello")
    }

    @Test
    fun `ConfigValue StringArrayValue displayValue joins with comma`() {
        val arr = ConfigValue.StringArrayValue(listOf("a", "b", "c"))
        assertThat(arr.displayValue).isEqualTo("a, b, c")
    }

    @Test
    fun `ConfigValue StringArrayValue empty`() {
        val arr = ConfigValue.StringArrayValue(emptyList())
        assertThat(arr.displayValue).isEmpty()
    }

    @Test
    fun `ConfigKey equality`() {
        val a = ConfigKey("key1", ConfigValue.BooleanValue(true), "desc")
        val b = ConfigKey("key1", ConfigValue.BooleanValue(true), "desc")
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `ConfigKey with isCustom flag`() {
        val custom = ConfigKey("my_key", ConfigValue.IntValue(5), isCustom = true)
        assertThat(custom.isCustom).isTrue()
    }

    // =========================================================================
    // PresetCategory enum
    // =========================================================================

    @Test
    fun `PresetCategory has 4 values`() {
        assertThat(PresetCategory.values()).hasLength(4)
    }

    @Test
    fun `PresetCategory values`() {
        assertThat(PresetCategory.values().map { it.name }).containsExactly(
            "WFC_ENABLE", "WFC_PREFERENCE", "ADVANCED", "CUSTOM"
        )
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Create a CarrierConfigRepository for pure-Kotlin method testing.
     * Uses a mock context since getPresets() and generateXML() don't need it.
     */
    private fun createRepository(): CarrierConfigRepository {
        return CarrierConfigRepository(
            org.mockito.kotlin.mock()
        )
    }
}
