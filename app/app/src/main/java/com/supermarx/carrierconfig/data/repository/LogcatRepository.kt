package com.supermarx.carrierconfig.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

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
     * Start monitoring logcat with filters
     */
    fun monitorLogcat(
        filterType: LogcatFilterType = LogcatFilterType.ALL,
        minLevel: LogLevel = LogLevel.DEBUG
    ): Flow<LogcatEntry> = flow {
        withContext(Dispatchers.IO) {
            val tags = when (filterType) {
                LogcatFilterType.CARRIER_CONFIG -> CARRIER_CONFIG_TAGS
                LogcatFilterType.IMS -> IMS_TAGS
                LogcatFilterType.TELEPHONY -> TELEPHONY_TAGS
                LogcatFilterType.WFC -> WFC_TAGS
                LogcatFilterType.ALL -> CARRIER_CONFIG_TAGS + IMS_TAGS + TELEPHONY_TAGS + WFC_TAGS
            }
            
            try {
                // Clear logcat first
                Runtime.getRuntime().exec("logcat -c").waitFor()
                
                // Build logcat command with tag filters
                val tagFilters = tags.joinToString(" ") { "$it:${minLevel.priority}" }
                val command = "logcat -v threadtime $tagFilters *:S"
                
                val process = Runtime.getRuntime().exec(command)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { 
                        parseLogcatLine(it)?.let { entry ->
                            emit(entry)
                        }
                    }
                }
            } catch (e: Exception) {
                // Error handling - emit error entry
                emit(LogcatEntry(
                    timestamp = System.currentTimeMillis(),
                    level = LogLevel.ERROR,
                    tag = "LogcatRepository",
                    message = "Error monitoring logcat: ${e.message}",
                    pid = 0,
                    tid = 0
                ))
            }
        }
    }
    
    /**
     * Get snapshot of current logs
     */
    suspend fun getLogcatSnapshot(
        filterType: LogcatFilterType = LogcatFilterType.ALL,
        lineCount: Int = 500
    ): List<LogcatEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<LogcatEntry>()
        
        try {
            val tags = when (filterType) {
                LogcatFilterType.CARRIER_CONFIG -> CARRIER_CONFIG_TAGS
                LogcatFilterType.IMS -> IMS_TAGS
                LogcatFilterType.TELEPHONY -> TELEPHONY_TAGS
                LogcatFilterType.WFC -> WFC_TAGS
                LogcatFilterType.ALL -> CARRIER_CONFIG_TAGS + IMS_TAGS + TELEPHONY_TAGS + WFC_TAGS
            }
            
            val tagFilters = tags.joinToString(" ") { "$it:D" }
            val command = "logcat -v threadtime -t $lineCount $tagFilters *:S"
            
            val process = Runtime.getRuntime().exec(command)
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
            reader.close()
        } catch (e: Exception) {
            // Return empty list on error
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
        try {
            Runtime.getRuntime().exec("logcat -c").waitFor()
        } catch (e: Exception) {
            // Ignore errors
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
