package com.supermarsx.carrierconfig.instrumentation

import android.content.Context
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Frida Instrumentation Manager
 * 
 * Manages Frida server and agent deployment for runtime hooks
 */
@Singleton
class FridaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FridaManager"
        private const val FRIDA_SERVER_PATH = "/data/local/tmp/frida-server-arm64"
        private const val AGENT_PATH = "/data/local/tmp/cco-agent.js"
        private const val PID_FILE = "/data/local/tmp/frida-server.pid"
    }
    
    data class FridaStatus(
        val isInstalled: Boolean,
        val isRunning: Boolean,
        val version: String?,
        val pid: Int?
    )
    
    data class SessionInfo(
        val target: String,
        val isActive: Boolean,
        val hooksInstalled: Int,
        val interceptCount: Int
    )
    
    /**
     * Check if Frida server is installed and running
     */
    suspend fun getStatus(): FridaStatus = withContext(Dispatchers.IO) {
        val isInstalled = checkFridaInstalled()
        val pid = getFridaPid()
        val isRunning = pid != null
        val version = if (isInstalled) getFridaVersion() else null
        
        FridaStatus(
            isInstalled = isInstalled,
            isRunning = isRunning,
            version = version,
            pid = pid
        )
    }
    
    /**
     * Install Frida server from assets
     */
    suspend fun installFridaServer(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).d("Installing Frida server...")
            
            // Copy frida-server from assets to temp
            val tempFile = File(context.cacheDir, "frida-server-arm64")
            val fridaBinary = openAssetText("frida-server-arm64", "instrumentation/frida-server-arm64")
                ?: return@withContext Result.failure(
                    IllegalStateException("Missing frida-server-arm64 asset")
                )
            fridaBinary.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Push to device
            val result = Shell.cmd(
                "su -c 'cp ${tempFile.absolutePath} $FRIDA_SERVER_PATH'",
                "su -c 'chmod 755 $FRIDA_SERVER_PATH'"
            ).exec()
            
            if (result.isSuccess) {
                Timber.tag(TAG).i("Frida server installed successfully")
                Result.success("Frida server installed at $FRIDA_SERVER_PATH")
            } else {
                val error = result.err.joinToString("\n")
                Timber.tag(TAG).e("Failed to install: $error")
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Installation failed")
            Result.failure(e)
        }
    }
    
    /**
     * Start Frida server in background
     */
    suspend fun startServer(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Check if already running
            val existingPid = getFridaPid()
            if (existingPid != null) {
                Timber.tag(TAG).d("Frida already running with PID $existingPid")
                return@withContext Result.success(existingPid)
            }
            
            Timber.tag(TAG).d("Starting Frida server...")
            
            // Start server in background
            val result = Shell.cmd(
                "su -c '$FRIDA_SERVER_PATH -D &'",
                "su -c 'sleep 2'",
                "su -c 'pgrep -f frida-server > $PID_FILE'"
            ).exec()
            
            if (!result.isSuccess) {
                val error = result.err.joinToString("\n")
                return@withContext Result.failure(Exception(error))
            }
            
            // Read PID
            val pidResult = Shell.cmd("su -c 'cat $PID_FILE'").exec()
            val pid = pidResult.out.firstOrNull()?.toIntOrNull()
            
            if (pid != null) {
                Timber.tag(TAG).i("Frida server started with PID $pid")
                Result.success(pid)
            } else {
                Result.failure(Exception("Failed to get PID"))
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to start server")
            Result.failure(e)
        }
    }
    
    /**
     * Stop Frida server
     */
    suspend fun stopServer(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pid = getFridaPid()
            if (pid == null) {
                return@withContext Result.success(Unit)
            }
            
            Timber.tag(TAG).d("Stopping Frida server (PID $pid)...")
            
            val result = Shell.cmd(
                "su -c 'kill $pid'",
                "su -c 'rm -f $PID_FILE'"
            ).exec()
            
            if (result.isSuccess) {
                Timber.tag(TAG).i("Frida server stopped")
                Result.success(Unit)
            } else {
                val error = result.err.joinToString("\n")
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to stop server")
            Result.failure(e)
        }
    }
    
    /**
     * Deploy agent script
     */
    suspend fun deployAgent(profile: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).d("Deploying agent with profile: $profile")
            
            // Copy agent from assets
            val agentContent = openAssetText(
                "frida/agent-complete.js",
                "instrumentation/agent-complete.js"
            )
            if (agentContent == null) {
                return@withContext Result.failure(
                    IllegalStateException("Missing Frida agent asset (agent-complete.js)")
                )
            }
            val agentBody = agentContent.bufferedReader().use {
                it.readText()
            }
            
            // Write to temp file
            val tempFile = File(context.cacheDir, "cco-agent.js")
            tempFile.writeText(agentBody)
            
            // Push to device
            val result = Shell.cmd(
                "su -c 'cp ${tempFile.absolutePath} $AGENT_PATH'",
                "su -c 'chmod 644 $AGENT_PATH'"
            ).exec()
            
            if (result.isSuccess) {
                Timber.tag(TAG).i("Agent deployed successfully")
                Result.success(AGENT_PATH)
            } else {
                val error = result.err.joinToString("\n")
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to deploy agent")
            Result.failure(e)
        }
    }
    
    /**
     * Start instrumentation session (requires external frida-tools)
     */
    suspend fun startSession(
        target: String = "com.sec.imsservice",
        profile: String = "oneui6_generic"
    ): Flow<String> = flow {
        emit("Checking Frida status...")
        
        val status = getStatus()
        if (!status.isInstalled) {
            throw Exception("Frida server not installed")
        }
        
        if (!status.isRunning) {
            emit("Starting Frida server...")
            startServer().getOrThrow()
        }
        
        emit("Deploying agent...")
        deployAgent(profile).getOrThrow()
        
        emit("Session ready!")
        emit("Connect using: frida -U $target -l $AGENT_PATH")
    }
    
    /**
     * Get active session info (via RPC if Frida Python tools available)
     */
    suspend fun getSessionInfo(target: String): SessionInfo? = withContext(Dispatchers.IO) {
        // This would require Frida Python tools with RPC support
        // For now, return null to indicate not implemented
        null
    }
    
    /**
     * Helper: Check if Frida is installed
     */
    private fun checkFridaInstalled(): Boolean {
        val result = Shell.cmd("su -c '[ -f $FRIDA_SERVER_PATH ] && echo 1 || echo 0'").exec()
        return result.out.firstOrNull() == "1"
    }
    
    /**
     * Helper: Get Frida version
     */
    private fun getFridaVersion(): String? {
        val result = Shell.cmd("su -c '$FRIDA_SERVER_PATH --version'").exec()
        return result.out.firstOrNull()
    }
    
    /**
     * Helper: Get Frida PID
     */
    private fun getFridaPid(): Int? {
        val result = Shell.cmd("su -c 'pgrep -f frida-server'").exec()
        return result.out.firstOrNull()?.toIntOrNull()
    }

    private fun openAssetText(vararg candidates: String): java.io.InputStream? {
        for (path in candidates) {
            try {
                return context.assets.open(path)
            } catch (_: Exception) {
                // Try next candidate
            }
        }
        return null
    }
}
