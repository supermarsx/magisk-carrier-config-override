package com.svtt.carrierconfig.data.repository

import android.content.Context
import android.os.Environment
import com.svtt.carrierconfig.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Generate diagnostic report from current state
     */
    suspend fun generateReport(
        deviceInfo: DeviceInfo?,
        simInfo: List<SimInfo>,
        imsStatus: ImsStatus?,
        wfcUiStatus: WfcUiStatus?,
        blockerAnalysis: BlockerAnalysis?
    ): DiagnosticReport = withContext(Dispatchers.IO) {
        DiagnosticReport(
            deviceInfo = deviceInfo,
            simInfo = simInfo,
            imsStatus = imsStatus,
            wfcUiStatus = wfcUiStatus,
            blockerAnalysis = blockerAnalysis
        )
    }
    
    /**
     * Export report to files
     */
    suspend fun exportReport(report: DiagnosticReport): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportDir = File(
                context.getExternalFilesDir(null),
                "svtt_reports"
            )
            
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val reportDir = File(exportDir, "report_$timestamp")
            reportDir.mkdirs()
            
            // Save JSON report
            val jsonFile = File(reportDir, "report.json")
            jsonFile.writeText(report.toJson())
            
            // Save text report
            val txtFile = File(reportDir, "report.txt")
            txtFile.writeText(report.toText())
            
            Timber.d("Report exported to: ${reportDir.absolutePath}")
            
            ExportResult(
                success = true,
                path = reportDir.absolutePath,
                files = listOf(jsonFile.name, txtFile.name)
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to export report")
            ExportResult(
                success = false,
                error = "Export failed: ${e.message}"
            )
        }
    }
    
    /**
     * Get list of exported reports
     */
    suspend fun getExportedReports(): List<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(
                context.getExternalFilesDir(null),
                "svtt_reports"
            )
            
            if (!exportDir.exists()) {
                return@withContext emptyList()
            }
            
            exportDir.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith("report_") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get exported reports")
            emptyList()
        }
    }
}

data class ExportResult(
    val success: Boolean,
    val path: String? = null,
    val files: List<String> = emptyList(),
    val error: String? = null
)
