package com.svtt.carrierconfig.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a CarrierConfig key with its type and value
 */
@Parcelize
data class CarrierConfigKey(
    val key: String,
    val type: ConfigValueType,
    val value: Any,
    val description: String = "",
    val category: ConfigCategory = ConfigCategory.OTHER
) : Parcelable

enum class ConfigValueType {
    BOOLEAN,
    INTEGER,
    STRING,
    STRING_ARRAY
}

enum class ConfigCategory {
    WFC,
    VOLTE,
    IMS,
    NETWORK,
    OTHER
}

/**
 * Predefined preset configurations
 */
@Parcelize
data class CarrierConfigPreset(
    val id: String,
    val name: String,
    val description: String,
    val keys: List<CarrierConfigKey>,
    val isCustom: Boolean = false
) : Parcelable

/**
 * Deployment configuration
 */
@Parcelize
data class DeploymentConfig(
    val targetSlot: Int? = null, // null = all slots
    val createBackup: Boolean = true,
    val restartServices: Boolean = false,
    val targetPath: String? = null
) : Parcelable

/**
 * Deployment result
 */
@Parcelize
data class DeploymentResult(
    val success: Boolean,
    val message: String,
    val backupPath: String? = null,
    val deployedPath: String? = null
) : Parcelable

/**
 * Method 1 state
 */
@Parcelize
data class Method1State(
    val presets: List<CarrierConfigPreset> = emptyList(),
    val customKeys: List<CarrierConfigKey> = emptyList(),
    val selectedPreset: CarrierConfigPreset? = null,
    val isRootAvailable: Boolean = false,
    val isMagiskInstalled: Boolean = false,
    val detectedPaths: List<String> = emptyList(),
    val activeOverride: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) : Parcelable

/**
 * Common WFC-related CarrierConfig keys
 */
object WfcConfigKeys {
    const val CARRIER_WFC_IMS_AVAILABLE = "carrier_wfc_ims_available_bool"
    const val CARRIER_DEFAULT_WFC_IMS_ENABLED = "carrier_default_wfc_ims_enabled_bool"
    const val EDITABLE_WFC_MODE = "editable_wfc_mode_bool"
    const val EDITABLE_WFC_ROAMING_MODE = "editable_wfc_roaming_mode_bool"
    const val CARRIER_WFC_SUPPORTS_WIFI_ONLY = "carrier_wfc_supports_wifi_only_bool"
    const val CARRIER_DEFAULT_WFC_IMS_MODE = "carrier_default_wfc_ims_mode_int"
    const val CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE = "carrier_default_wfc_ims_roaming_mode_int"
    const val WFC_OPERATOR_ERROR_CODES = "wfc_operator_error_codes_string_array"
    
    // WFC mode values
    const val WFC_MODE_CELLULAR_PREFERRED = 0
    const val WFC_MODE_WIFI_PREFERRED = 1
    const val WFC_MODE_WIFI_ONLY = 2
}

/**
 * Common VoLTE-related CarrierConfig keys
 */
object VoLteConfigKeys {
    const val CARRIER_VOLTE_AVAILABLE = "carrier_volte_available_bool"
    const val ENHANCED_4G_LTE_ON_BY_DEFAULT = "enhanced_4g_lte_on_by_default_bool"
    const val EDITABLE_ENHANCED_4G_LTE = "editable_enhanced_4g_lte_bool"
    const val HIDE_ENHANCED_4G_LTE = "hide_enhanced_4g_lte_bool"
}
