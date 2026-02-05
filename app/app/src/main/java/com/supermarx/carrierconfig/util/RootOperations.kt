package com.supermarx.carrierconfig.util

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Centralized root shell operations wrapper
 * Provides safe, consistent access to root commands
 */
object RootShell {
    
    /**
     * Check if root access is available
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Execute a single root command
     */
    suspend fun execute(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd(command).exec()
            ShellResult(
                success = result.isSuccess,
                output = result.out,
                error = result.err,
                exitCode = result.code
            )
        } catch (e: Exception) {
            ShellResult(
                success = false,
                output = emptyList(),
                error = listOf(e.message ?: "Unknown error"),
                exitCode = -1
            )
        }
    }
    
    /**
     * Execute multiple root commands
     */
    suspend fun executeMultiple(vararg commands: String): List<ShellResult> = withContext(Dispatchers.IO) {
        commands.map { execute(it) }
    }
    
    /**
     * Execute command and get first line of output
     */
    suspend fun executeForFirstLine(command: String): String? = withContext(Dispatchers.IO) {
        val result = execute(command)
        if (result.success && result.output.isNotEmpty()) {
            result.output.first()
        } else {
            null
        }
    }
    
    /**
     * Check if a command is available
     */
    suspend fun isCommandAvailable(command: String): Boolean = withContext(Dispatchers.IO) {
        val result = execute("which $command")
        result.success && result.output.isNotEmpty()
    }
    
    /**
     * Read system property
     */
    suspend fun getSystemProperty(key: String): String? = withContext(Dispatchers.IO) {
        executeForFirstLine("getprop $key")
    }
    
    /**
     * Set system property (requires root)
     */
    suspend fun setSystemProperty(key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        execute("setprop $key $value").success
    }
}

/**
 * Result of a shell command execution
 */
data class ShellResult(
    val success: Boolean,
    val output: List<String>,
    val error: List<String>,
    val exitCode: Int
) {
    val outputString: String get() = output.joinToString("\n")
    val errorString: String get() = error.joinToString("\n")
}

/**
 * Safe file manager for root operations
 */
object SuFileManager {
    
    /**
     * Check if file exists (root)
     */
    suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("[ -f '$path' ] && echo 'exists' || echo 'not_exists'")
        result.success && result.outputString.contains("exists")
    }
    
    /**
     * Check if directory exists (root)
     */
    suspend fun directoryExists(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("[ -d '$path' ] && echo 'exists' || echo 'not_exists'")
        result.success && result.outputString.contains("exists")
    }
    
    /**
     * Read file content (root)
     */
    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        val result = RootShell.execute("cat '$path'")
        if (result.success) result.outputString else null
    }
    
    /**
     * Write content to file (root)
     */
    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("cco_", ".tmp")
        try {
            tempFile.writeText(content)
            val result = RootShell.execute("cat '${tempFile.absolutePath}' > '$path'")
            result.success
        } finally {
            tempFile.delete()
        }
    }
    
    /**
     * Copy file (root)
     */
    suspend fun copyFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("cp -f '$source' '$destination'")
        result.success
    }
    
    /**
     * Move file (root)
     */
    suspend fun moveFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("mv -f '$source' '$destination'")
        result.success
    }
    
    /**
     * Delete file (root)
     */
    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("rm -f '$path'")
        result.success
    }
    
    /**
     * Create directory (root)
     */
    suspend fun createDirectory(path: String, recursive: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        val command = if (recursive) "mkdir -p '$path'" else "mkdir '$path'"
        val result = RootShell.execute(command)
        result.success
    }
    
    /**
     * Set file permissions (root)
     */
    suspend fun setPermissions(path: String, mode: String): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("chmod $mode '$path'")
        result.success
    }
    
    /**
     * Set file owner (root)
     */
    suspend fun setOwner(path: String, owner: String, group: String? = null): Boolean = withContext(Dispatchers.IO) {
        val ownerStr = if (group != null) "$owner:$group" else owner
        val result = RootShell.execute("chown $ownerStr '$path'")
        result.success
    }
    
    /**
     * Get file size
     */
    suspend fun getFileSize(path: String): Long? = withContext(Dispatchers.IO) {
        val result = RootShell.execute("stat -c %s '$path'")
        if (result.success && result.output.isNotEmpty()) {
            result.output.first().toLongOrNull()
        } else {
            null
        }
    }
    
    /**
     * List directory contents
     */
    suspend fun listDirectory(path: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootShell.execute("ls -1 '$path'")
        if (result.success) result.output else emptyList()
    }
}

/**
 * Android service restart helper
 */
object ServiceRestarter {
    
    /**
     * Restart Phone service
     */
    suspend fun restartPhoneService(): Boolean = withContext(Dispatchers.IO) {
        val result = RootShell.execute("killall -9 com.android.phone")
        result.success
    }
    
    /**
     * Restart IMS service
     */
    suspend fun restartImsService(): Boolean = withContext(Dispatchers.IO) {
        // Try multiple methods
        val commands = arrayOf(
            "killall -9 com.sec.imsservice",
            "killall -9 ims",
            "setprop ctl.restart ims"
        )
        
        RootShell.executeMultiple(*commands).any { it.success }
    }
    
    /**
     * Restart Telephony stack (requires reboot on some devices)
     */
    suspend fun restartTelephony(): Boolean = withContext(Dispatchers.IO) {
        val commands = arrayOf(
            "setprop ctl.restart radio",
            "setprop ctl.restart phone"
        )
        
        RootShell.executeMultiple(*commands).any { it.success }
    }
    
    /**
     * Toggle airplane mode (soft restart)
     */
    suspend fun toggleAirplaneMode(): Boolean = withContext(Dispatchers.IO) {
        val commands = arrayOf(
            "settings put global airplane_mode_on 1",
            "am broadcast -a android.intent.action.AIRPLANE_MODE",
            "sleep 2",
            "settings put global airplane_mode_on 0",
            "am broadcast -a android.intent.action.AIRPLANE_MODE"
        )
        
        RootShell.executeMultiple(*commands).all { it.success }
    }
}

/**
 * System properties reader helper
 */
object SystemPropertiesReader {
    
    /**
     * Get Android version
     */
    suspend fun getAndroidVersion(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.build.version.release")
    }
    
    /**
     * Get API level
     */
    suspend fun getApiLevel(): Int? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.build.version.sdk")?.toIntOrNull()
    }
    
    /**
     * Get device model
     */
    suspend fun getDeviceModel(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.product.model")
    }
    
    /**
     * Get device manufacturer
     */
    suspend fun getManufacturer(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.product.manufacturer")
    }
    
    /**
     * Get build fingerprint
     */
    suspend fun getBuildFingerprint(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.build.fingerprint")
    }
    
    /**
     * Get One UI version
     */
    suspend fun getOneUIVersion(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.build.version.oneui")
    }
    
    /**
     * Get security patch date
     */
    suspend fun getSecurityPatch(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.build.version.security_patch")
    }
    
    /**
     * Get CSC code (Samsung)
     */
    suspend fun getCscCode(): String? = withContext(Dispatchers.IO) {
        RootShell.getSystemProperty("ro.csc.sales_code") 
            ?: RootShell.getSystemProperty("ril.sales_code")
    }
    
    /**
     * Get all properties matching pattern
     */
    suspend fun getPropertiesMatching(pattern: String): Map<String, String> = withContext(Dispatchers.IO) {
        val result = RootShell.execute("getprop | grep '$pattern'")
        if (!result.success) return@withContext emptyMap()
        
        result.output.mapNotNull { line ->
            val match = Regex("\\[(.*?)\\]: \\[(.*?)\\]").find(line)
            match?.let {
                val (key, value) = it.destructured
                key to value
            }
        }.toMap()
    }
}
