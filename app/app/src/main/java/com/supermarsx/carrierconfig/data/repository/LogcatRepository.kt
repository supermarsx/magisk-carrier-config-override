package com.supermarsx.carrierconfig.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logcat buffer selector per spec Section 7.1
 */
enum class LogcatBuffer(val flag: String, val displayName: String) {
    MAIN("-b main", "Main"),
    RADIO("-b radio", "Radio"),
    ALL("-b main -b radio", "All Buffers")
}

/**
 * Repository for logcat monitoring and filtering
 */
@Singleton
class LogcatRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // Relevant tags for carrier config and IMS
        private val CARRIER_CONFIG_TAGS = listOf(
            "CarrierConfigLoader",
            "CarrierConfigHelper",
            "CarrierConfig",
            "CarrierSvc"
        )
        
        private val IMS_TAGS = listOf(
            "ImsManager",
            "ImsService",
            "ImsPhone",
            "ImsPhoneCallTracker",
            "ImsConnectionStateHelper",
            "ImsRegistration"
        )
        
        private val TELEPHONY_TAGS = listOf(
            "TelephonyRegistry",
            "Phone",
            "GsmCdmaPhone",
            "DataConnection",
            "DcTracker"
        )
        
        private val WFC_TAGS = listOf(
            "WifiCalling",
            "Iwlan",
            "ImsPhone",
            "Vowifi"
        )
    }
    
    /**
     * Start monitoring logcat with filters.
     *
     * Uses callbackFlow so that emissions happen safely from the IO dispatcher
     * without violating the flow invariant. The underlying logcat process is
     * destroyed when the flow collector cancels.
     */
    fun monitorLogcat(
        filterType: LogcatFilterType = LogcatFilterType.ALL,
        minLevel: LogLevel = LogLevel.DEBUG,
        buffer: LogcatBuffer = LogcatBuffer.ALL
    ): Flow<LogcatEntry> = callbackFlow {
        val tags = when (filterType) {
            LogcatFilterType.CARRIER_CONFIG -> CARRIER_CONFIG_TAGS
            LogcatFilterType.IMS -> IMS_TAGS
            LogcatFilterType.TELEPHONY -> TELEPHONY_TAGS
            LogcatFilterType.WFC -> WFC_TAGS
            LogcatFilterType.ALL -> CARRIER_CONFIG_TAGS + IMS_TAGS + TELEPHONY_TAGS + WFC_TAGS
        }

        val tagFilters = tags.joinToString(" ") { "$it:${minLevel.priority}" }
        val command = "logcat ${buffer.flag} -v threadtime $tagFilters *:S"

        val process = Runtime.getRuntime().exec(command)
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    parseLogcatLine(it)?.let { entry ->
                        trySend(entry)
                    }
                }
            }
        } catch (e: Exception) {
            trySend(
                LogcatEntry(
                    timestamp = System.currentTimeMillis(),
                    level = LogLevel.ERROR,
                    tag = "LogcatRepository",
                    message = "Error monitoring logcat: ${e.message}",
                    pid = 0,
                    tid = 0
                )
            )
        } finally {
            process.destroy()
        }

        awaitClose { process.destroy() }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get snapshot of current logs
     */
    suspend fun getLogcatSnapshot(
        filterType: LogcatFilterType = LogcatFilterType.ALL,
        lineCount: Int = 500,
        buffer: LogcatBuffer = LogcatBuffer.ALL
    ): List<LogcatEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<LogcatEntry>()
        
        val tags = when (filterType) {
            LogcatFilterType.CARRIER_CONFIG -> CARRIER_CONFIG_TAGS
            LogcatFilterType.IMS -> IMS_TAGS
            LogcatFilterType.TELEPHONY -> TELEPHONY_TAGS
            LogcatFilterType.WFC -> WFC_TAGS
            LogcatFilterType.ALL -> CARRIER_CONFIG_TAGS + IMS_TAGS + TELEPHONY_TAGS + WFC_TAGS
        }
        
        val tagFilters = tags.joinToString(" ") { "$it:D" }
        val command = "logcat ${buffer.flag} -v threadtime -t $lineCount $tagFilters *:S"
        
        val process = Runtime.getRuntime().exec(command)
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { 
                    parseLogcatLine(it)?.let { entry ->
                        entries.add(entry)
                    }
                }
            }
            
            process.waitFor()
        } catch (_: Exception) {
            // Return partial list on error
        } finally {
            process.destroy()
        }
        
        entries
    }
    
    /**
     * Parse logcat line in threadtime format
     * Format: MM-DD HH:MM:SS.mmm  PID  TID LEVEL TAG: message
     */
    private fun parseLogcatLine(line: String): LogcatEntry? {
        try {
            // Skip empty lines and separator lines
            if (line.isBlank() || line.startsWith("---")) return null
            
            // Regex for threadtime format
            val regex = """^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEF])\s+(.+?):\s+(.*)$""".toRegex()
            val match = regex.find(line) ?: return null
            
            val (timestamp, pid, tid, level, tag, message) = match.destructured
            
            return LogcatEntry(
                timestamp = System.currentTimeMillis(), // Use current time for now
                level = LogLevel.fromChar(level[0]),
                tag = tag,
                message = message,
                pid = pid.toIntOrNull() ?: 0,
                tid = tid.toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Clear logcat buffer
     */
    suspend fun clearLogcat() = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec("logcat -c")
        try {
            process.waitFor()
        } catch (_: Exception) {
            // Ignore errors
        } finally {
            process.destroy()
        }
    }

    /**
     * Get radio buffer snapshot for IMS/telephony debugging (spec Section 7.1).
     * Returns raw text suitable for inclusion in the diagnostics ZIP.
     */
    suspend fun getRadioLogSnapshot(lineCount: Int = 1000): String = withContext(Dispatchers.IO) {
        val command = "logcat -b radio -v threadtime -t $lineCount"
        val process = Runtime.getRuntime().exec(command)
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            "Error capturing radio log: ${e.message}"
        } finally {
            process.destroy()
        }
    }

    /**
     * Get IMS-related getprop output (spec Section 7.1).
     */
    suspend fun getImsProperties(): String = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "getprop | grep -i ims"))
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output.ifBlank { "(no IMS properties found)" }
        } catch (e: Exception) {
            "Error reading properties: ${e.message}"
        } finally {
            process.destroy()
        }
    }
}

/**
 * Logcat entry model
 */
data class LogcatEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val pid: Int,
    val tid: Int
)

/**
 * Log level enum
 */
enum class LogLevel(val priority: String, val displayName: String) {
    VERBOSE("V", "Verbose"),
    DEBUG("D", "Debug"),
    INFO("I", "Info"),
    WARNING("W", "Warning"),
    ERROR("E", "Error"),
    FATAL("F", "Fatal");
    
    companion object {
        fun fromChar(char: Char): LogLevel {
            return when (char.uppercaseChar()) {
                'V' -> VERBOSE
                'D' -> DEBUG
                'I' -> INFO
                'W' -> WARNING
                'E' -> ERROR
                'F' -> FATAL
                else -> DEBUG
            }
        }
    }
}

/**
 * Filter type for logcat
 */
enum class LogcatFilterType(val displayName: String) {
    ALL("All Logs"),
    CARRIER_CONFIG("CarrierConfig"),
    IMS("IMS/VoLTE"),
    TELEPHONY("Telephony"),
    WFC("Wi-Fi Calling")
}
