package com.svtt.carrierconfig.data.repository

import android.content.Context
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MagiskRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val MAGISK_PATH = "/data/adb/magisk"
        private const val MODULES_PATH = "/data/adb/modules"
        private const val SVTT_MODULE_ID = "svtt-carrierconfig"
        private const val SVTT_DATA_PATH = "/data/adb/svtt"
        
        // Candidate override paths for Samsung devices
        private val CANDIDATE_PATHS = listOf(
            "/data/vendor/carrierconfig/override.xml",
            "/data/vendor/carrierconfig/override_carrier.xml",
            "/data/misc/carrierconfig/override.xml",
            "/data/user_de/0/com.android.phone/files/carrierconfig_override.xml"
        )
    }
    
    /**
     * Check if Magisk is installed
     */
    suspend fun isMagiskInstalled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val magiskFile = SuFile.open(MAGISK_PATH)
            magiskFile.exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check Magisk installation")
            false
        }
    }
    
    /**
     * Check if SVTT module is installed
     */
    suspend fun isModuleInstalled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modulePath = SuFile.open("$MODULES_PATH/$SVTT_MODULE_ID")
            modulePath.exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check module installation")
            false
        }
    }
    
    /**
     * Detect which override path is used on this device
     */
    suspend fun detectOverridePath(): String? = withContext(Dispatchers.IO) {
        try {
            // Check if any candidate path exists and is readable
            for (path in CANDIDATE_PATHS) {
                val file = SuFile.open(path)
                if (file.parent()?.exists() == true) {
                    Timber.d("Detected potential override path: $path")
                    return@withContext path
                }
            }
            
            // Default to first candidate if none found
            Timber.w("No override path detected, using default")
            CANDIDATE_PATHS.first()
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect override path")
            CANDIDATE_PATHS.first()
        }
    }
    
    /**
     * Get all detected paths that exist
     */
    suspend fun getAllDetectedPaths(): List<String> = withContext(Dispatchers.IO) {
        try {
            CANDIDATE_PATHS.filter { path ->
                val file = SuFile.open(path)
                file.parent()?.exists() == true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get detected paths")
            emptyList()
        }
    }
    
    /**
     * Create SVTT data directory structure
     */
    suspend fun createDataDirectories(): Boolean = withContext(Dispatchers.IO) {
        try {
            val directories = listOf(
                SVTT_DATA_PATH,
                "$SVTT_DATA_PATH/active",
                "$SVTT_DATA_PATH/overrides",
                "$SVTT_DATA_PATH/backups",
                "$SVTT_DATA_PATH/logs"
            )
            
            for (dir in directories) {
                val result = Shell.cmd("mkdir -p $dir").exec()
                if (!result.isSuccess) {
                    Timber.e("Failed to create directory: $dir")
                    return@withContext false
                }
            }
            
            // Set permissions
            Shell.cmd("chmod 755 $SVTT_DATA_PATH").exec()
            
            Timber.d("Created SVTT data directories")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to create data directories")
            false
        }
    }
    
    /**
     * Save override XML to app-private storage
     */
    suspend fun saveOverrideToAppStorage(xml: String, filename: String = "override.xml"): File? = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                file.writeText(xml)
                Timber.d("Saved override XML to: ${file.absolutePath}")
                file
            } catch (e: Exception) {
                Timber.e(e, "Failed to save override XML")
                null
            }
        }
    
    /**
     * Copy override file to SVTT data directory
     */
    suspend fun copyOverrideToSvttData(sourceFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val targetPath = "$SVTT_DATA_PATH/active/override.xml"
            
            // Create backup if exists
            val existingFile = SuFile.open(targetPath)
            if (existingFile.exists()) {
                val timestamp = System.currentTimeMillis()
                val backupPath = "$SVTT_DATA_PATH/backups/override_$timestamp.xml"
                Shell.cmd("cp $targetPath $backupPath").exec()
                Timber.d("Created backup at: $backupPath")
            }
            
            // Copy new file
            val result = Shell.cmd(
                "cp ${sourceFile.absolutePath} $targetPath",
                "chmod 644 $targetPath"
            ).exec()
            
            if (result.isSuccess) {
                Timber.d("Copied override to SVTT data: $targetPath")
                true
            } else {
                Timber.e("Failed to copy override: ${result.err.joinToString()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy override")
            false
        }
    }
    
    /**
     * Check if override is currently active
     */
    suspend fun isOverrideActive(): Boolean = withContext(Dispatchers.IO) {
        try {
            val activeFile = SuFile.open("$SVTT_DATA_PATH/active/override.xml")
            activeFile.exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check override status")
            false
        }
    }
    
    /**
     * Get module log
     */
    suspend fun getModuleLog(): String? = withContext(Dispatchers.IO) {
        try {
            val logFile = SuFile.open("$SVTT_DATA_PATH/logs/module.log")
            if (logFile.exists()) {
                Shell.cmd("cat ${logFile.path}").exec().out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get module log")
            null
        }
    }
    
    /**
     * Remove override (revert)
     */
    suspend fun removeOverride(): Boolean = withContext(Dispatchers.IO) {
        try {
            val activeFile = "$SVTT_DATA_PATH/active/override.xml"
            
            // Create backup before removing
            if (SuFile.open(activeFile).exists()) {
                val timestamp = System.currentTimeMillis()
                val backupPath = "$SVTT_DATA_PATH/backups/override_removed_$timestamp.xml"
                Shell.cmd("cp $activeFile $backupPath").exec()
            }
            
            // Remove active override
            val result = Shell.cmd("rm -f $activeFile").exec()
            
            if (result.isSuccess) {
                Timber.d("Removed active override")
                true
            } else {
                Timber.e("Failed to remove override")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove override")
            false
        }
    }
}
