package com.supermarsx.carrierconfig.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for data export/import operations
 */
@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val EXPORT_DIR = "CCO/exports"
        private const val CONFIG_DIR = "CCO/configs"
    }
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Export app configuration to JSON
     */
    suspend fun exportConfiguration(
        includePresets: Boolean = true,
        includeSettings: Boolean = true
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val config = AppConfiguration(
                version = "1.0.0",
                exportDate = System.currentTimeMillis(),
                settings = if (includeSettings) {
                    AppSettings(
                        autoRefresh = true,
                        enableNotifications = false,
                        debugMode = false,
                        theme = "dark"
                    )
                } else null,
                customKeys = emptyList() // Custom keys stored separately in preset system
            )
            
            val jsonString = json.encodeToString(config)
            val file = saveToFile(jsonString, "config", "json")
            
            ExportResult.Success(file.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    /**
     * Import app configuration from JSON
     */
    suspend fun importConfiguration(filePath: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext ImportResult.Error("File not found")
            }
            
            val jsonString = file.readText()
            importConfigurationFromString(jsonString)
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message}")
        }
    }
    
    /**
     * Import configuration from JSON string
     */
    suspend fun importConfigurationFromString(jsonString: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val config = json.decodeFromString<AppConfiguration>(jsonString)
            
            // Apply configuration settings
            config.settings?.let { settings ->
                // Settings would be applied via PreferencesManager
                // For now, just validate the import worked
                if (settings.theme.isNotEmpty()) {
                    // Successfully imported settings
                }
            }
            
            ImportResult.Success(config.version)
        } catch (e: Exception) {
            ImportResult.Error("Invalid configuration format: ${e.message}")
        }
    }
    
    /**
     * Export diagnostics report
     */
    suspend fun exportDiagnostics(
        deviceInfo: String,
        simInfo: String,
        imsStatus: String
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val report = DiagnosticsReport(
                timestamp = System.currentTimeMillis(),
                deviceInfo = deviceInfo,
                simInfo = simInfo,
                imsStatus = imsStatus
            )
            
            // Save JSON
            val jsonString = json.encodeToString(report)
            val jsonFile = saveToFile(jsonString, "diagnostics", "json", EXPORT_DIR)
            
            // Save TXT
            val txtContent = buildString {
                appendLine("CCO Diagnostics Report")
                appendLine("Generated: ${Date(report.timestamp)}")
                appendLine()
                appendLine("=== Device Info ===")
                appendLine(deviceInfo)
                appendLine()
                appendLine("=== SIM Info ===")
                appendLine(simInfo)
                appendLine()
                appendLine("=== IMS Status ===")
                appendLine(imsStatus)
            }
            saveToFile(txtContent, "diagnostics", "txt", EXPORT_DIR)
            
            ExportResult.Success(jsonFile.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    /**
     * Save content to file with timestamp
     */
    private fun saveToFile(
        content: String,
        prefix: String,
        extension: String,
        directory: String = CONFIG_DIR
    ): File {
        val externalDir = context.getExternalFilesDir(null)
        val dir = File(externalDir, directory)
        dir.mkdirs()
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "${prefix}_$timestamp.$extension"
        val file = File(dir, filename)
        
        file.writeText(content)
        return file
    }
    
    /**
     * Get export directory path
     */
    fun getExportDirectory(): String {
        val externalDir = context.getExternalFilesDir(null)
        return File(externalDir, EXPORT_DIR).absolutePath
    }
    
    /**
     * List exported files
     */
    suspend fun listExports(): List<ExportFile> = withContext(Dispatchers.IO) {
        val externalDir = context.getExternalFilesDir(null)
        val dir = File(externalDir, EXPORT_DIR)
        
        if (!dir.exists()) {
            return@withContext emptyList()
        }
        
        dir.listFiles()
            ?.filter { it.isFile && (it.extension == "json" || it.extension == "txt") }
            ?.map { file ->
                ExportFile(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            }
            ?.sortedByDescending { it.lastModified }
            ?: emptyList()
    }
}

/**
 * App configuration for export/import
 */
@Serializable
data class AppConfiguration(
    val version: String,
    val exportDate: Long,
    val settings: AppSettings?,
    val customKeys: List<CustomKeyData>
)

@Serializable
data class AppSettings(
    val autoRefresh: Boolean,
    val enableNotifications: Boolean,
    val debugMode: Boolean,
    val theme: String
)

@Serializable
data class CustomKeyData(
    val key: String,
    val valueType: String,
    val value: String
)

@Serializable
data class DiagnosticsReport(
    val timestamp: Long,
    val deviceInfo: String,
    val simInfo: String,
    val imsStatus: String
)

/**
 * Export file metadata
 */
data class ExportFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)

/**
 * Export result
 */
sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * Import result
 */
sealed class ImportResult {
    data class Success(val version: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
