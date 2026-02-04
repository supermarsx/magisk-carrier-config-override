package com.svtt.carrierconfig.data.repository

import com.svtt.carrierconfig.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepository @Inject constructor() {
    
    /**
     * Get all predefined presets
     */
    fun getPresets(): List<CarrierConfigPreset> {
        return listOf(
            createExposeWfcUiPreset(),
            createWfcDefaultEnabledPreset(),
            createEditableWfcModePreset(),
            createWifiPreferredPreset(),
            createWifiOnlyPreset(),
            createFullWfcEnablementPreset()
        )
    }
    
    /**
     * Preset: Expose WFC UI
     * Makes Wi-Fi Calling settings visible in the UI
     */
    private fun createExposeWfcUiPreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "expose_wfc_ui",
            name = "Expose WFC UI",
            description = "Makes Wi-Fi Calling settings visible in Samsung Settings. This is the minimum required to see the WFC menu.",
            keys = listOf(
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature availability",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.EDITABLE_WFC_MODE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows user to change WFC mode",
                    category = ConfigCategory.WFC
                )
            )
        )
    }
    
    /**
     * Preset: WFC Default Enabled
     * Enables WFC by default when available
     */
    private fun createWfcDefaultEnabledPreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "wfc_default_enabled",
            name = "WFC Default Enabled",
            description = "Enables Wi-Fi Calling by default. WFC will be automatically enabled when conditions are met.",
            keys = listOf(
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ENABLED,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC by default",
                    category = ConfigCategory.WFC
                )
            )
        )
    }
    
    /**
     * Preset: Editable WFC Mode
     * Allows user to change WFC mode and roaming mode
     */
    private fun createEditableWfcModePreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "editable_wfc_mode",
            name = "Editable WFC Mode",
            description = "Allows changing WFC mode (Wi-Fi Preferred, Cellular Preferred, Wi-Fi Only) and roaming behavior.",
            keys = listOf(
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.EDITABLE_WFC_MODE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows WFC mode editing",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.EDITABLE_WFC_ROAMING_MODE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows WFC roaming mode editing",
                    category = ConfigCategory.WFC
                )
            )
        )
    }
    
    /**
     * Preset: Wi-Fi Preferred
     * Sets WFC mode to Wi-Fi Preferred
     */
    private fun createWifiPreferredPreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "wifi_preferred",
            name = "Wi-Fi Preferred",
            description = "Prefers Wi-Fi for calls when available, falls back to cellular. Recommended for most users.",
            keys = listOf(
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ENABLED,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC by default",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_PREFERRED,
                    description = "Sets mode to Wi-Fi Preferred",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_PREFERRED,
                    description = "Sets roaming mode to Wi-Fi Preferred",
                    category = ConfigCategory.WFC
                )
            )
        )
    }
    
    /**
     * Preset: Wi-Fi Only
     * Sets WFC mode to Wi-Fi Only (no cellular fallback)
     */
    private fun createWifiOnlyPreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "wifi_only",
            name = "Wi-Fi Only",
            description = "Forces calls over Wi-Fi only. No cellular fallback. Use only if you have reliable Wi-Fi coverage.",
            keys = listOf(
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ENABLED,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC by default",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_SUPPORTS_WIFI_ONLY,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables Wi-Fi Only mode support",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_ONLY,
                    description = "Sets mode to Wi-Fi Only",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_ONLY,
                    description = "Sets roaming mode to Wi-Fi Only",
                    category = ConfigCategory.WFC
                )
            )
        )
    }
    
    /**
     * Preset: Full WFC Enablement
     * Comprehensive preset with all WFC features enabled
     */
    private fun createFullWfcEnablementPreset(): CarrierConfigPreset {
        return CarrierConfigPreset(
            id = "full_wfc_enablement",
            name = "Full WFC Enablement",
            description = "Comprehensive preset enabling all WFC features with maximum flexibility and Wi-Fi Preferred mode.",
            keys = listOf(
                // WFC availability
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC feature",
                    category = ConfigCategory.WFC
                ),
                // Default enabled
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ENABLED,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables WFC by default",
                    category = ConfigCategory.WFC
                ),
                // Editable modes
                CarrierConfigKey(
                    key = WfcConfigKeys.EDITABLE_WFC_MODE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows WFC mode editing",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.EDITABLE_WFC_ROAMING_MODE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows WFC roaming mode editing",
                    category = ConfigCategory.WFC
                ),
                // Wi-Fi Only support
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_WFC_SUPPORTS_WIFI_ONLY,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables Wi-Fi Only mode",
                    category = ConfigCategory.WFC
                ),
                // Default modes
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_PREFERRED,
                    description = "Sets default mode to Wi-Fi Preferred",
                    category = ConfigCategory.WFC
                ),
                CarrierConfigKey(
                    key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE,
                    type = ConfigValueType.INTEGER,
                    value = WfcConfigKeys.WFC_MODE_WIFI_PREFERRED,
                    description = "Sets default roaming mode to Wi-Fi Preferred",
                    category = ConfigCategory.WFC
                ),
                // VoLTE enablement (often required for WFC)
                CarrierConfigKey(
                    key = VoLteConfigKeys.CARRIER_VOLTE_AVAILABLE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables VoLTE (often required for WFC)",
                    category = ConfigCategory.VOLTE
                ),
                CarrierConfigKey(
                    key = VoLteConfigKeys.ENHANCED_4G_LTE_ON_BY_DEFAULT,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Enables VoLTE by default",
                    category = ConfigCategory.VOLTE
                ),
                CarrierConfigKey(
                    key = VoLteConfigKeys.EDITABLE_ENHANCED_4G_LTE,
                    type = ConfigValueType.BOOLEAN,
                    value = true,
                    description = "Allows VoLTE toggle editing",
                    category = ConfigCategory.VOLTE
                )
            )
        )
    }
    
    /**
     * Get all available config keys (catalog)
     */
    fun getAllAvailableKeys(): List<CarrierConfigKey> {
        return listOf(
            // WFC keys
            CarrierConfigKey(
                key = WfcConfigKeys.CARRIER_WFC_IMS_AVAILABLE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Controls WFC feature availability",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ENABLED,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Controls default WFC enabled state",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.EDITABLE_WFC_MODE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Allows user to change WFC mode",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.EDITABLE_WFC_ROAMING_MODE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Allows user to change WFC roaming mode",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.CARRIER_WFC_SUPPORTS_WIFI_ONLY,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Enables Wi-Fi Only mode support",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_MODE,
                type = ConfigValueType.INTEGER,
                value = 0,
                description = "Default WFC mode (0=Cellular, 1=Wi-Fi, 2=Wi-Fi Only)",
                category = ConfigCategory.WFC
            ),
            CarrierConfigKey(
                key = WfcConfigKeys.CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE,
                type = ConfigValueType.INTEGER,
                value = 0,
                description = "Default WFC roaming mode",
                category = ConfigCategory.WFC
            ),
            // VoLTE keys
            CarrierConfigKey(
                key = VoLteConfigKeys.CARRIER_VOLTE_AVAILABLE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Controls VoLTE feature availability",
                category = ConfigCategory.VOLTE
            ),
            CarrierConfigKey(
                key = VoLteConfigKeys.ENHANCED_4G_LTE_ON_BY_DEFAULT,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Enables VoLTE by default",
                category = ConfigCategory.VOLTE
            ),
            CarrierConfigKey(
                key = VoLteConfigKeys.EDITABLE_ENHANCED_4G_LTE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Allows user to toggle VoLTE",
                category = ConfigCategory.VOLTE
            ),
            CarrierConfigKey(
                key = VoLteConfigKeys.HIDE_ENHANCED_4G_LTE,
                type = ConfigValueType.BOOLEAN,
                value = false,
                description = "Hides VoLTE toggle in settings",
                category = ConfigCategory.VOLTE
            )
        )
    }
}
