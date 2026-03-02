package com.supermarsx.carrierconfig.data.repository

import android.content.Context
import com.supermarsx.carrierconfig.data.model.*
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
        /**
         * WFC mode integer mappings (per spec Section 5.3):
         *   0 = Cellular preferred
         *   1 = Wi-Fi preferred
         *   2 = Wi-Fi only
         */
        const val WFC_MODE_CELLULAR_PREFERRED = 0
        const val WFC_MODE_WIFI_PREFERRED = 1
        const val WFC_MODE_WIFI_ONLY = 2

        /** Runtime override paths where CarrierConfigManager reads overrides (spec Section 5.2) */
        private val CARRIER_CONFIG_OVERRIDE_PATHS = listOf(
            "/data/vendor/carrierconfig/override.xml",
            "/data/vendor/carrierconfig/override_carrier.xml",
            "/data/misc/carrierconfig/override.xml",
            "/data/user_de/0/com.android.phone/files/carrierconfig_override.xml"
        )

        /** CCO managed override storage */
        private const val CCO_ACTIVE_OVERRIDE = "/data/adb/cco/active/override.xml"
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
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(WFC_MODE_CELLULAR_PREFERRED) // 0 = Cellular preferred
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
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_PREFERRED), // 1 = Wi-Fi preferred
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_PREFERRED)
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
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_ONLY), // 2 = Wi-Fi only
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_ONLY)
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
                    "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_PREFERRED),
                    "carrier_default_wfc_ims_roaming_mode_int" to ConfigValue.IntValue(WFC_MODE_WIFI_PREFERRED),
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
     * Detect the correct runtime override path for this device.
     * Checks the spec-defined candidate paths where CarrierConfigManager reads overrides.
     */
    private suspend fun detectCarrierConfigPath(): String? = withContext(Dispatchers.IO) {
        // First check if a file already exists at any candidate path (indicates system uses it)
        for (path in CARRIER_CONFIG_OVERRIDE_PATHS) {
            val result = Shell.cmd("test -f $path && echo 'exists'").exec()
            if (result.isSuccess && result.out.firstOrNull() == "exists") {
                return@withContext path
            }
        }
        // Next check if the parent directory exists
        for (path in CARRIER_CONFIG_OVERRIDE_PATHS) {
            val dir = path.substringBeforeLast('/')
            val result = Shell.cmd("test -d $dir && echo 'exists'").exec()
            if (result.isSuccess && result.out.firstOrNull() == "exists") {
                return@withContext path
            }
        }
        // Default fallback to most common Samsung path
        CARRIER_CONFIG_OVERRIDE_PATHS.firstOrNull()
    }
    
    /**
     * Check if the override path (or its parent directory) is writable.
     * When the file doesn't exist yet we test the parent directory instead.
     */
    private suspend fun checkPathWritable(path: String): Boolean = withContext(Dispatchers.IO) {
        // If the file exists, test it directly
        val fileResult = Shell.cmd("test -f $path && test -w $path && echo 'writable'").exec()
        if (fileResult.isSuccess && fileResult.out.firstOrNull() == "writable") {
            return@withContext true
        }
        // Otherwise test the parent directory
        val dir = path.substringBeforeLast('/')
        val dirResult = Shell.cmd("test -d $dir && test -w $dir && echo 'writable'").exec()
        dirResult.isSuccess && dirResult.out.firstOrNull() == "writable"
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
     * Deploy CarrierConfig override.
     *
     * Per spec Section 5.4:
     *  1. App collects desired keys.
     *  2. App writes override.xml into app-private storage.
     *  3. App copies into /data/adb/cco/active/override.xml.
     *  4. Module's service.sh bind-mounts at boot.
     *  5. App prompts reboot.
     */
    suspend fun deployOverride(
        preset: CarrierConfigPreset,
        customKeys: List<ConfigKey>,
        targetPath: String,
        simSlot: Int? = null
    ): DeploymentResult = withContext(Dispatchers.IO) {
        try {
            // Combine preset keys and custom keys
            val allKeys = preset.keys.map { (key, value) ->
                ConfigKey(key, value, "")
            } + customKeys
            
            // Generate XML
            val xml = generateXML(allKeys)
            
            // Write to app-private temp file first
            val tempFile = File(context.cacheDir, "carrierconfig_override.xml")
            tempFile.writeText(xml)
            
            // Determine CCO active override path (per-slot support)
            val activeOverride = if (simSlot != null) {
                "/data/adb/cco/active/override_sim${simSlot}.xml"
            } else {
                CCO_ACTIVE_OVERRIDE
            }
            
            // Ensure directory structure
            Shell.cmd("mkdir -p /data/adb/cco/active /data/adb/cco/overrides /data/adb/cco/logs /data/adb/cco/backup").exec()
            
            // Backup original override if exists
            Shell.cmd(
                "test -f $activeOverride && cp $activeOverride /data/adb/cco/backup/override_backup_$(date +%Y%m%d_%H%M%S).xml || true"
            ).exec()
            
            // Copy to CCO active location
            val deployResult = Shell.cmd("cp ${tempFile.absolutePath} $activeOverride").exec()
            if (!deployResult.isSuccess) {
                return@withContext DeploymentResult.Error("Failed to deploy override to $activeOverride")
            }
            
            // Also copy a named copy to overrides directory
            Shell.cmd("cp ${tempFile.absolutePath} /data/adb/cco/overrides/${preset.id}.xml").exec()
            
            // Set permissions
            Shell.cmd("chmod 644 $activeOverride").exec()
            
            // Cleanup temp
            tempFile.delete()
            
            DeploymentResult.Success
        } catch (e: Exception) {
            DeploymentResult.Error("Deployment failed: ${e.message}", e.stackTraceToString())
        }
    }
    
    /**
     * Revert CarrierConfig override — unmounts bind mount and removes active override file.
     * A reboot is still recommended to fully clear the cached carrier config.
     */
    suspend fun revertOverride(targetPath: String): DeploymentResult = withContext(Dispatchers.IO) {
        try {
            // Unmount the bind mount first (so the target reverts to its original content)
            Shell.cmd("umount $targetPath 2>/dev/null || true").exec()

            // Remove the CCO active override
            val removeResult = Shell.cmd("rm -f $CCO_ACTIVE_OVERRIDE").exec()
            if (!removeResult.isSuccess) {
                return@withContext DeploymentResult.Error("Failed to remove active override")
            }

            // Also clean per-slot overrides
            Shell.cmd("rm -f /data/adb/cco/active/override_sim*.xml").exec()

            DeploymentResult.Success
        } catch (e: Exception) {
            DeploymentResult.Error("Revert failed: ${e.message}", e.stackTraceToString())
        }
    }
    
    /**
     * Get current deployment status by checking CCO active override.
     */
    suspend fun getDeploymentStatus(targetPath: String): CarrierConfigDeployment = withContext(Dispatchers.IO) {
        val checkResult = Shell.cmd("test -f $CCO_ACTIVE_OVERRIDE && echo 'exists'").exec()
        val isDeployed = checkResult.isSuccess && checkResult.out.firstOrNull() == "exists"
        
        val backupCheck = Shell.cmd("ls /data/adb/cco/backup/*.xml 2>/dev/null | head -1").exec()
        val backupExists = backupCheck.isSuccess && backupCheck.out.isNotEmpty()
        
        CarrierConfigDeployment(
            isDeployed = isDeployed,
            deploymentPath = if (isDeployed) CCO_ACTIVE_OVERRIDE else null,
            backupExists = backupExists
        )
    }
}
