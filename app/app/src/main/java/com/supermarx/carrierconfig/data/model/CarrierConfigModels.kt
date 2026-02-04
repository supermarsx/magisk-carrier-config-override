package com.supermarx.carrierconfig.data.model

/**
 * Preset configurations for CarrierConfig keys
 */
data class CarrierConfigPreset(
    val id: String,
    val name: String,
    val description: String,
    val category: PresetCategory,
    val keys: Map<String, ConfigValue>,
    val recommendedFor: String = "All devices"
)

enum class PresetCategory {
    WFC_ENABLE,
    WFC_PREFERENCE,
    ADVANCED,
    CUSTOM
}

/**
 * Configuration value with type information
 */
sealed class ConfigValue {
    abstract val displayValue: String
    
    data class BooleanValue(val value: Boolean) : ConfigValue() {
        override val displayValue: String = value.toString()
    }
    
    data class IntValue(val value: Int) : ConfigValue() {
        override val displayValue: String = value.toString()
    }
    
    data class StringValue(val value: String) : ConfigValue() {
        override val displayValue: String = value
    }
    
    data class StringArrayValue(val values: List<String>) : ConfigValue() {
        override val displayValue: String = values.joinToString(", ")
    }
}

/**
 * Configuration key definition
 */
data class ConfigKey(
    val key: String,
    val value: ConfigValue,
    val description: String = "",
    val isCustom: Boolean = false
)

/**
 * Deployment state for CarrierConfig override
 */
data class CarrierConfigDeployment(
    val isDeployed: Boolean = false,
    val deployedPresetId: String? = null,
    val deployedKeys: List<ConfigKey> = emptyList(),
    val deploymentPath: String? = null,
    val timestamp: Long? = null,
    val backupExists: Boolean = false
)

/**
 * Prerequisites check result
 */
data class Prerequisites(
    val hasRoot: Boolean = false,
    val hasMagisk: Boolean = false,
    val magiskVersion: String? = null,
    val carrierConfigPath: String? = null,
    val pathWritable: Boolean = false
) {
    val allMet: Boolean
        get() = hasRoot && hasMagisk && !carrierConfigPath.isNullOrEmpty() && pathWritable
}

/**
 * Deployment result
 */
sealed class DeploymentResult {
    object Success : DeploymentResult()
    data class Error(val message: String, val details: String? = null) : DeploymentResult()
    object PrerequisitesNotMet : DeploymentResult()
}

/**
 * State for CarrierConfig screen
 */
data class CarrierConfigState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val presets: List<CarrierConfigPreset> = emptyList(),
    val selectedPreset: CarrierConfigPreset? = null,
    val customKeys: List<ConfigKey> = emptyList(),
    val prerequisites: Prerequisites? = null,
    val deployment: CarrierConfigDeployment? = null,
    val currentTab: Int = 0
)
