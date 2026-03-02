package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import org.junit.Test

/**
 * Extended tests for [CarrierConfigRepository] covering:
 * - escapeXml() via reflection
 * - XML generation edge cases
 * - DeploymentResult.PrerequisitesNotMet
 * - Preset key completeness validation
 */
class CarrierConfigExtendedTest {

    private val repository = CarrierConfigRepository(org.mockito.kotlin.mock())

    // =========================================================================
    // escapeXml via reflection
    // =========================================================================

    private fun callEscapeXml(s: String): String {
        val method = CarrierConfigRepository::class.java
            .getDeclaredMethod("escapeXml", String::class.java)
        method.isAccessible = true
        return method.invoke(repository, s) as String
    }

    @Test
    fun `escapeXml escapes ampersand`() {
        assertThat(callEscapeXml("A&B")).isEqualTo("A&amp;B")
    }

    @Test
    fun `escapeXml escapes less than`() {
        assertThat(callEscapeXml("a<b")).isEqualTo("a&lt;b")
    }

    @Test
    fun `escapeXml escapes greater than`() {
        assertThat(callEscapeXml("a>b")).isEqualTo("a&gt;b")
    }

    @Test
    fun `escapeXml escapes double quote`() {
        assertThat(callEscapeXml("key=\"value\"")).isEqualTo("key=&quot;value&quot;")
    }

    @Test
    fun `escapeXml escapes single quote`() {
        assertThat(callEscapeXml("it's")).isEqualTo("it&apos;s")
    }

    @Test
    fun `escapeXml handles all special chars together`() {
        assertThat(callEscapeXml("<tag attr=\"a&b\" x='c'>"))
            .isEqualTo("&lt;tag attr=&quot;a&amp;b&quot; x=&apos;c&apos;&gt;")
    }

    @Test
    fun `escapeXml leaves plain text unchanged`() {
        assertThat(callEscapeXml("carrier_wfc_ims_available_bool"))
            .isEqualTo("carrier_wfc_ims_available_bool")
    }

    @Test
    fun `escapeXml handles empty string`() {
        assertThat(callEscapeXml("")).isEmpty()
    }

    // =========================================================================
    // XML generation with special characters
    // =========================================================================

    @Test
    fun `generateXML escapes string value content`() {
        val keys = listOf(
            ConfigKey("test_key", ConfigValue.StringValue("value<with>&special'chars\""))
        )
        val xml = repository.generateXML(keys)
        assertThat(xml).contains("value&lt;with&gt;&amp;special&apos;chars&quot;")
        assertThat(xml).doesNotContain("<with>")
    }

    @Test
    fun `generateXML escapes string array items`() {
        val keys = listOf(
            ConfigKey("arr_key", ConfigValue.StringArrayValue(listOf("a&b", "c<d")))
        )
        val xml = repository.generateXML(keys)
        assertThat(xml).contains("<item>a&amp;b</item>")
        assertThat(xml).contains("<item>c&lt;d</item>")
    }

    @Test
    fun `generateXML escapes key names`() {
        val keys = listOf(
            ConfigKey("key\"with'quotes", ConfigValue.BooleanValue(true))
        )
        val xml = repository.generateXML(keys)
        assertThat(xml).contains("key&quot;with&apos;quotes")
    }

    // =========================================================================
    // DeploymentResult.PrerequisitesNotMet
    // =========================================================================

    @Test
    fun `DeploymentResult PrerequisitesNotMet is singleton`() {
        val a = DeploymentResult.PrerequisitesNotMet
        val b = DeploymentResult.PrerequisitesNotMet
        assertThat(a).isSameInstanceAs(b)
    }

    @Test
    fun `DeploymentResult covers all three variants`() {
        val results: List<DeploymentResult> = listOf(
            DeploymentResult.Success,
            DeploymentResult.Error("err"),
            DeploymentResult.PrerequisitesNotMet
        )
        assertThat(results).hasSize(3)
        assertThat(results[0]).isInstanceOf(DeploymentResult.Success::class.java)
        assertThat(results[1]).isInstanceOf(DeploymentResult.Error::class.java)
        assertThat(results[2]).isInstanceOf(DeploymentResult.PrerequisitesNotMet::class.java)
    }

    // =========================================================================
    // Preset validation — every preset has required fields
    // =========================================================================

    @Test
    fun `all presets have non-empty recommendedFor`() {
        repository.getPresets().forEach { preset ->
            assertThat(preset.recommendedFor).isNotEmpty()
        }
    }

    @Test
    fun `no preset has duplicate key names`() {
        repository.getPresets().forEach { preset ->
            val keyNames = preset.keys.keys.toList()
            assertThat(keyNames).containsNoDuplicates()
        }
    }

    @Test
    fun `all presets with mode_int use valid WFC mode values`() {
        val validModes = setOf(0, 1, 2)
        repository.getPresets().forEach { preset ->
            preset.keys.forEach { (key, value) ->
                if (key.contains("wfc_ims_mode_int") && value is ConfigValue.IntValue) {
                    assertThat(value.value).isIn(validModes)
                }
            }
        }
    }

    @Test
    fun `wfc_ui_only has minimal key set`() {
        val preset = repository.getPresets().first { it.id == "wfc_ui_only" }
        assertThat(preset.keys).hasSize(2)
        assertThat(preset.keys).containsKey("carrier_wfc_ims_available_bool")
        assertThat(preset.keys).containsKey("editable_wfc_mode_bool")
    }

    @Test
    fun `full_enablement has maximum key set`() {
        val preset = repository.getPresets().first { it.id == "full_enablement" }
        assertThat(preset.keys.size).isAtLeast(9)
    }

    // =========================================================================
    // XML generation with preset combinations
    // =========================================================================

    @Test
    fun `generateXML from each preset produces valid XML`() {
        repository.getPresets().forEach { preset ->
            val keys = preset.keys.map { (key, value) -> ConfigKey(key, value) }
            val xml = repository.generateXML(keys)
            assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            assertThat(xml).contains("<carrier_config>")
            assertThat(xml).contains("</carrier_config>")
        }
    }

    @Test
    fun `generateXML combined preset and custom keys`() {
        val preset = repository.getPresets().first { it.id == "wfc_default_enabled" }
        val presetKeys = preset.keys.map { (key, value) -> ConfigKey(key, value) }
        val customKeys = listOf(
            ConfigKey("my_custom_bool", ConfigValue.BooleanValue(true), isCustom = true),
            ConfigKey("my_custom_int", ConfigValue.IntValue(99), isCustom = true)
        )
        val allKeys = presetKeys + customKeys
        val xml = repository.generateXML(allKeys)
        assertThat(xml).contains("my_custom_bool")
        assertThat(xml).contains("my_custom_int")
        assertThat(xml).contains("carrier_wfc_ims_available_bool")
    }

    // =========================================================================
    // CarrierConfigState transitions
    // =========================================================================

    @Test
    fun `CarrierConfigState with loading`() {
        val state = CarrierConfigState(isLoading = true)
        assertThat(state.isLoading).isTrue()
        assertThat(state.error).isNull()
    }

    @Test
    fun `CarrierConfigState with error`() {
        val state = CarrierConfigState(error = "No root access")
        assertThat(state.error).isEqualTo("No root access")
    }

    @Test
    fun `CarrierConfigState with presets loaded`() {
        val presets = repository.getPresets()
        val state = CarrierConfigState(presets = presets)
        assertThat(state.presets).hasSize(6)
    }

    @Test
    fun `CarrierConfigState with selected preset and custom keys`() {
        val preset = repository.getPresets().first()
        val state = CarrierConfigState(
            selectedPreset = preset,
            customKeys = listOf(
                ConfigKey("custom_key", ConfigValue.BooleanValue(true), isCustom = true)
            )
        )
        assertThat(state.selectedPreset).isNotNull()
        assertThat(state.customKeys).hasSize(1)
    }

    @Test
    fun `CarrierConfigState tab navigation`() {
        val state = CarrierConfigState(currentTab = 2)
        assertThat(state.currentTab).isEqualTo(2)
    }

    // =========================================================================
    // Prerequisites edge cases
    // =========================================================================

    @Test
    fun `Prerequisites allMet with all conditions true`() {
        val prereqs = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            magiskVersion = "27000",
            carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
            pathWritable = true
        )
        assertThat(prereqs.allMet).isTrue()
    }

    @Test
    fun `Prerequisites allMet false with empty path`() {
        val prereqs = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            magiskVersion = "27000",
            carrierConfigPath = "",
            pathWritable = true
        )
        assertThat(prereqs.allMet).isFalse()
    }

    @Test
    fun `Prerequisites allMet false when hasMagisk is false`() {
        val prereqs = Prerequisites(
            hasRoot = true,
            hasMagisk = false,
            magiskVersion = null,
            carrierConfigPath = "/some/path",
            pathWritable = true
        )
        assertThat(prereqs.allMet).isFalse()
    }
}
