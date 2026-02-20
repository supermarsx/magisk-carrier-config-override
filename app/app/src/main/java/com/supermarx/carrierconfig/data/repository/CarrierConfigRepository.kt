package dev.mars.carrierconfig.data.repository

import android.content.Context
import dev.mars.carrierconfig.data.model.*
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for CarrierConfig operations
 */
@Singleton
class CarrierConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val CARRIER_CONFIG_PATHS = listOf(
            "/system/etc/CarrierConfig",
            "/system/etc/carrier_config",
            "/vendor/etc/CarrierConfig",
            "/vendor/etc/carrier_config"
        )
    }
    
    /**
     * Get all available presets
     */
    fun getPresets(): List<CarrierConfigPreset> {
        return listOf(
            CarrierConfigPreset(
                id = "wfc_ui_only",
                name = "Expose WFC UI Only",
                description = "Minimal changes - just makes Wi-Fi Calling menu visible in Settings",
                category = PresetCategory.WFC_ENABLE,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true)
                ),
                recommendedFor = "Testing visibility without functionality changes"
            ),
            CarrierConfigPreset(
                id = "wfc_default_enabled",
                name = "WFC Default Enabled",
                description = "Enables WFC by default with carrier preferred mode",
                category = PresetCategory.WFC_ENABLE,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_enabled_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(1) // Cellular preferred
                ),
                recommendedFor = "Users who want WFC enabled on boot"
            ),
            CarrierConfigPreset(
                id = "wfc_editable_mode",
                name = "Editable WFC Mode",
                description = "Allows changing WFC preference (Wi-Fi/Cellular preferred)",
                category = PresetCategory.WFC_PREFERENCE,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_roaming_mode_bool" to ConfigValue.BooleanValue(true)
                ),
                recommendedFor = "Users who want control over WFC mode"
            ),
            CarrierConfigPreset(
                id = "wifi_preferred",
                name = "Wi-Fi Preferred",
                description = "WFC enabled with Wi-Fi preferred over cellular",
                category = PresetCategory.WFC_PREFERENCE,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_enabled_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(2), // Wi-Fi preferred
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(2)
                ),
                recommendedFor = "Poor cellular coverage, good Wi-Fi"
            ),
            CarrierConfigPreset(
                id = "wifi_only",
                name = "Wi-Fi Only Mode",
                description = "Forces all calls through Wi-Fi, disables cellular fallback",
                category = PresetCategory.WFC_PREFERENCE,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_enabled_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(0), // Wi-Fi only
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(0)
                ),
                recommendedFor = "No cellular coverage, Wi-Fi only environments"
            ),
            CarrierConfigPreset(
                id = "full_enablement",
                name = "Full WFC Enablement",
                description = "Complete WFC enablement with all features unlocked (RECOMMENDED)",
                category = PresetCategory.ADVANCED,
                keys = mapOf(
                    "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                    "editable_wfc_roaming_mode_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_enabled_bool" to ConfigValue.BooleanValue(true),
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(2),
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(2),
                    "carrier_wfc_supports_wifi_only_bool" to ConfigValue.BooleanValue(true),
                    "carrier_wfc_supports_cellular_preferred_bool" to ConfigValue.BooleanValue(true),
                    "carrier_wfc_supports_wifi_preferred_bool" to ConfigValue.BooleanValue(true)
                ),
                recommendedFor = "Most users - provides full control and flexibility"
            )
        )
    }
    
    /**
     * Check prerequisites for deployment
     */
    suspend fun checkPrerequisites(): Prerequisites = withContext(Dispatchers.IO) {
        val hasRoot = Shell.isAppGrantedRoot() == true
        val magiskVersion = if (hasRoot) {
            val result = Shell.cmd("magisk -V").exec()
            if (result.isSuccess) result.out.firstOrNull() else null
        } else null
        
        val hasMagisk = magiskVersion != null
        
        val carrierConfigPath = if (hasRoot) {
            detectCarrierConfigPath()
        } else null
        
        val pathWritable = if (hasRoot && carrierConfigPath != null) {
            checkPathWritable(carrierConfigPath)
        } else false
        
        Prerequisites(
            hasRoot = hasRoot,
            hasMagisk = hasMagisk,
            magiskVersion = magiskVersion,
            carrierConfigPath = carrierConfigPath,
            pathWritable = pathWritable
        )
    }
    
    /**
     * Detect the correct CarrierConfig path for this device
     */
    private suspend fun detectCarrierConfigPath(): String? = withContext(Dispatchers.IO) {
        for (path in CARRIER_CONFIG_PATHS) {
            val result = Shell.cmd("test -d $path && echo 'exists'").exec()
            if (result.isSuccess && result.out.firstOrNull() == "exists") {
                return@withContext path
            }
        }
        null
    }
    
    /**
     * Check if path is writable (via bind mount)
     */
    private suspend fun checkPathWritable(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("test -w $path && echo 'writable'").exec()
        result.isSuccess && result.out.firstOrNull() == "writable"
    }
    
    /**
     * Generate XML content for selected keys
     */
    fun generateXML(keys: List<ConfigKey>): String {
        val xmlBuilder = StringBuilder()
        xmlBuilder.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        xmlBuilder.appendLine("<carrier_config>")
        
        keys.forEach { key ->
            when (val value = key.value) {
                is ConfigValue.BooleanValue -> {
                    xmlBuilder.appendLine("    <boolean name=\"${key.key}\" value=\"${value.value}\" />")
                }
                is ConfigValue.IntValue -> {
                    xmlBuilder.appendLine("    <int name=\"${key.key}\" value=\"${value.value}\" />")
                }
                is ConfigValue.StringValue -> {
                    xmlBuilder.appendLine("    <string name=\"${key.key}\">${value.value}</string>")
                }
                is ConfigValue.StringArrayValue -> {
                    xmlBuilder.appendLine("    <string-array name=\"${key.key}\">")
                    value.values.forEach { item ->
                        xmlBuilder.appendLine("        <item>$item</item>")
                    }
                    xmlBuilder.appendLine("    </string-array>")
                }
            }
        }
        
        xmlBuilder.appendLine("</carrier_config>")
        return xmlBuilder.toString()
    }
    
    /**
     * Deploy CarrierConfig override
     */
    suspend fun deployOverride(
        preset: CarrierConfigPreset,
        customKeys: List<ConfigKey>,
        targetPath: String
    ): DeploymentResult = withContext(Dispatchers.IO) {
        try {
            // Combine preset keys and custom keys
            val allKeys = preset.keys.map { (key, value) ->
                ConfigKey(key, value, "")
            } + customKeys
            
            // Generate XML
            val xml = generateXML(allKeys)
            
            // Create temporary file
            val tempFile = "/data/local/tmp/carrierconfig_override.xml"
            val writeResult = Shell.cmd("echo '$xml' > $tempFile").exec()
            if (!writeResult.isSuccess) {
                return@withContext DeploymentResult.Error("Failed to create XML file")
            }
            
            // Backup original if exists
            val overrideFile = "$targetPath/carrierconfig_override.xml"
            val backupResult = Shell.cmd(
                "test -f $overrideFile && cp $overrideFile ${overrideFile}.backup || true"
            ).exec()
            
            // Copy to target (would use Magisk bind mount in production)
            val deployResult = Shell.cmd("cp $tempFile $overrideFile").exec()
            if (!deployResult.isSuccess) {
                return@withContext DeploymentResult.Error("Failed to deploy override")
            }
            
            // Set permissions
            Shell.cmd("chmod 644 $overrideFile").exec()
            
            // Cleanup
            Shell.cmd("rm $tempFile").exec()
            
            DeploymentResult.Success
        } catch (e: Exception) {
            DeploymentResult.Error("Deployment failed: ${e.message}", e.stackTraceToString())
        }
    }
    
    /**
     * Revert CarrierConfig override
     */
    suspend fun revertOverride(targetPath: String): DeploymentResult = withContext(Dispatchers.IO) {
        try {
            val overrideFile = "$targetPath/carrierconfig_override.xml"
            val backupFile = "${overrideFile}.backup"
            
            // Check if backup exists
            val backupCheck = Shell.cmd("test -f $backupFile && echo 'exists'").exec()
            if (backupCheck.isSuccess && backupCheck.out.firstOrNull() == "exists") {
                // Restore from backup
                val restoreResult = Shell.cmd("cp $backupFile $overrideFile").exec()
                if (!restoreResult.isSuccess) {
                    return@withContext DeploymentResult.Error("Failed to restore from backup")
                }
            } else {
                // Remove override file
                val removeResult = Shell.cmd("rm -f $overrideFile").exec()
                if (!removeResult.isSuccess) {
                    return@withContext DeploymentResult.Error("Failed to remove override")
                }
            }
            
            DeploymentResult.Success
        } catch (e: Exception) {
            DeploymentResult.Error("Revert failed: ${e.message}", e.stackTraceToString())
        }
    }
    
    /**
     * Get current deployment status
     */
    suspend fun getDeploymentStatus(targetPath: String): CarrierConfigDeployment = withContext(Dispatchers.IO) {
        val overrideFile = "$targetPath/carrierconfig_override.xml"
        val checkResult = Shell.cmd("test -f $overrideFile && echo 'exists'").exec()
        val isDeployed = checkResult.isSuccess && checkResult.out.firstOrNull() == "exists"
        
        val backupCheck = Shell.cmd("test -f ${overrideFile}.backup && echo 'exists'").exec()
        val backupExists = backupCheck.isSuccess && backupCheck.out.firstOrNull() == "exists"
        
        CarrierConfigDeployment(
            isDeployed = isDeployed,
            deploymentPath = if (isDeployed) targetPath else null,
            backupExists = backupExists
        )
    }
}
