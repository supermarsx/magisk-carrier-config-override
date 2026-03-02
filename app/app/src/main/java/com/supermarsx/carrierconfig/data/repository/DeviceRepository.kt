package com.supermarsx.carrierconfig.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import com.supermarsx.carrierconfig.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Not in the public SDK – use string literal to avoid compile errors. */
private const val ACTION_WIFI_CALLING_SETTINGS = "android.settings.WIFI_CALLING_SETTINGS"

/**
 * Repository for device information and system queries
 */
@Singleton
class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // Regex patterns for privacy-sensitive data (spec Section 8.2)
        private val PHONE_NUMBER_REGEX = Regex("""\+?\d[\d\-\s]{8,14}\d""")
        private val IMSI_REGEX = Regex("""\b\d{15}\b""")
    }

    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    
    private val subscriptionManager: SubscriptionManager? by lazy {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
    }
    
    /**
     * Get device information
     */
    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        val isRooted = checkRootAccess()
        val oneUIVersion = getOneUIVersion()
        
        DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            buildFingerprint = Build.FINGERPRINT,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            oneUIVersion = oneUIVersion,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            isRooted = isRooted
        )
    }
    
    /**
     * Get SIM card information for all slots
     */
    suspend fun getSIMInfo(): List<SIMInfo> = withContext(Dispatchers.IO) {
        try {
            val simInfoList = mutableListOf<SIMInfo>()
            
            // Try to get subscription info (requires READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, permission handling required
                // For now, return mock data if no permission
            }
            
            // Get active subscriptions
            val activeSubscriptions = try {
                subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
            } catch (e: SecurityException) {
                emptyList()
            }
            
            activeSubscriptions.forEachIndexed { index, subInfo ->
                simInfoList.add(
                    SIMInfo(
                        slotIndex = subInfo.simSlotIndex,
                        carrierName = subInfo.carrierName?.toString(),
                        mcc = subInfo.mccString,
                        mnc = subInfo.mncString,
                        iccid = "••••${subInfo.iccId?.takeLast(4) ?: "••••"}", // Redacted for privacy
                        isActive = true
                    )
                )
            }
            
            // If no SIMs found, return placeholder
            if (simInfoList.isEmpty()) {
                simInfoList.add(
                    SIMInfo(
                        slotIndex = 0,
                        carrierName = "No SIM / No Permission",
                        mcc = "000",
                        mnc = "00",
                        iccid = "••••",
                        isActive = false
                    )
                )
            }
            
            simInfoList
        } catch (e: Exception) {
            listOf(
                SIMInfo(
                    slotIndex = 0,
                    carrierName = "Error: ${e.message}",
                    mcc = null,
                    mnc = null,
                    iccid = null,
                    isActive = false
                )
            )
        }
    }
    
    /**
     * Get IMS registration status
     */
    suspend fun getIMSStatus(): IMSStatus = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("dumpsys ims").exec()
            if (result.isSuccess) {
                val output = result.out.joinToString("\n")
                val isRegistered = output.contains("mRegistered=true", ignoreCase = true) ||
                                 output.contains("ImsRegistered", ignoreCase = true)
                val voLTEAvailable = output.contains("VOICE", ignoreCase = true) ||
                                    output.contains("VoLTE", ignoreCase = true)
                val voWiFiAvailable = output.contains("ePDG", ignoreCase = true) ||
                                     output.contains("WIFI_CALLING", ignoreCase = true) ||
                                     output.contains("VoWiFi", ignoreCase = true)
                
                val registrationState = when {
                    isRegistered && voWiFiAvailable -> "REGISTERED_WIFI"
                    isRegistered -> "REGISTERED_LTE"
                    else -> "NOT_REGISTERED"
                }
                
                return@withContext IMSStatus(
                    isRegistered = isRegistered,
                    isVoLTEAvailable = voLTEAvailable,
                    isVoWiFiAvailable = voWiFiAvailable,
                    registrationState = registrationState
                )
            }
        } catch (e: Exception) {
            // Return default status on error
        }
        
        // Default status if dumpsys fails
        IMSStatus(
            isRegistered = false,
            isVoLTEAvailable = false,
            isVoWiFiAvailable = false,
            registrationState = "UNKNOWN"
        )
    }
    
    /**
     * Get Wi-Fi Calling UI status
     */
    suspend fun getWFCUIStatus(): WFCUIStatus = withContext(Dispatchers.IO) {
        val wfcActivityExists = checkWFCActivityExists()
        
        // Check if settings page populates (basic heuristic)
        val pagePopulates = wfcActivityExists && try {
            val result = Shell.cmd("dumpsys activity top | grep -i wifi").exec()
            result.isSuccess && result.out.isNotEmpty()
        } catch (e: Exception) {
            false
        }
        
        // Check for toggle presence via carrier config
        val togglePresent = try {
            val result = Shell.cmd("dumpsys carrier_config | grep -i wfc").exec()
            result.isSuccess && result.out.any { line ->
                line.contains("carrier_wfc_ims_available_bool", ignoreCase = true) &&
                line.contains("true", ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
        
        WFCUIStatus(
            settingsActivityExists = wfcActivityExists,
            pagePopulates = pagePopulates,
            togglePresent = togglePresent
        )
    }
    
    /**
     * Detect likely blocker based on collected data
     */
    fun detectBlocker(imsStatus: IMSStatus?, wfcStatus: WFCUIStatus?): WFCBlocker {
        if (imsStatus == null || wfcStatus == null) {
            return WFCBlocker.UNKNOWN
        }
        
        // Check IMS registration first
        if (!imsStatus.isRegistered) {
            return WFCBlocker.IMS_NOT_REGISTERED
        }
        
        // Check if settings activity exists
        if (!wfcStatus.settingsActivityExists) {
            return WFCBlocker.SETTINGS_MISSING
        }
        
        // Check if IMS is registered but VoWiFi not available
        if (imsStatus.isRegistered && !imsStatus.isVoWiFiAvailable) {
            return WFCBlocker.CARRIER_CONFIG_GATE
        }
        
        // Check if settings exist but page doesn't populate
        if (wfcStatus.settingsActivityExists && !wfcStatus.pagePopulates) {
            return WFCBlocker.CSC_GATE
        }
        
        // If everything looks good
        if (imsStatus.isVoWiFiAvailable && wfcStatus.togglePresent) {
            return WFCBlocker.NONE
        }
        
        return WFCBlocker.UNKNOWN
    }
    
    /**
     * Open Wi-Fi Calling settings
     */
    suspend fun openWiFiCallingSettings() = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(ACTION_WIFI_CALLING_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to wireless settings
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * Export diagnostic report
     */
    suspend fun exportReport(state: DashboardState): ExportResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(java.util.Date())
            
            val reportText = buildString {
                appendLine("=== CCO Diagnostic Report ===")
                appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
                appendLine()
                
                // Device info
                state.deviceInfo?.let { device ->
                    appendLine("[Device Information]")
                    appendLine("Manufacturer: ${device.manufacturer}")
                    appendLine("Model: ${device.model}")
                    appendLine("Android: ${device.androidVersion}")
                    device.oneUIVersion?.let { appendLine("One UI: $it") }
                    appendLine("Security Patch: ${device.securityPatch}")
                    appendLine("Root Status: ${if (device.isRooted) "Rooted" else "Not Rooted"}")
                    appendLine()
                }
                
                // SIM info
                if (state.simInfo.isNotEmpty()) {
                    appendLine("[SIM Information]")
                    state.simInfo.forEach { sim ->
                        appendLine("Slot ${sim.slotIndex}: ${sim.carrierName ?: "Unknown"}")
                        sim.mcc?.let { appendLine("  MCC/MNC: $it/${sim.mnc}") }
                        appendLine("  Active: ${sim.isActive}")
                    }
                    appendLine()
                }
                
                // IMS status
                state.imsStatus?.let { ims ->
                    appendLine("[IMS Status]")
                    appendLine("Registered: ${ims.isRegistered}")
                    appendLine("VoLTE Available: ${ims.isVoLTEAvailable}")
                    appendLine("VoWiFi Available: ${ims.isVoWiFiAvailable}")
                    appendLine("Registration State: ${ims.registrationState}")
                    appendLine()
                }
                
                // WFC UI status
                state.wfcUIStatus?.let { wfc ->
                    appendLine("[WFC UI Status]")
                    appendLine("Settings Activity: ${wfc.settingsActivityExists}")
                    appendLine("Page Populates: ${wfc.pagePopulates}")
                    appendLine("Toggle Present: ${wfc.togglePresent}")
                    appendLine()
                }
                
                // Blocker analysis
                appendLine("[Analysis]")
                appendLine("Detected Blocker: ${state.detectedBlocker}")
                appendLine()
                appendLine("=== End Report ===")
            }
            
            // Save to app-scoped external storage (scoped storage compliant)
            val externalDir = context.getExternalFilesDir(null)
                ?: context.filesDir
            val exportDir = java.io.File(externalDir, "reports")
            exportDir.mkdirs()
            
            val reportFile = java.io.File(exportDir, "diagnostic_report_$timestamp.txt")
            reportFile.writeText(redactSensitiveData(reportText))
            
            ExportResult.Success(reportFile.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export report: ${e.message}")
        }
    }
    
    /**
     * Check if device has root access
     */
    private fun checkRootAccess(): Boolean {
        return try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Redact privacy-sensitive data from text (spec Section 8.2).
     * Removes phone numbers, IMSI, and ensures ICCID is already masked.
     */
    fun redactSensitiveData(text: String): String {
        var result = text
        // Redact phone numbers (replace digits keeping last 4)
        result = PHONE_NUMBER_REGEX.replace(result) { match ->
            val digits = match.value.filter { it.isDigit() }
            if (digits.length >= 7) "••••••${digits.takeLast(4)}" else match.value
        }
        // Redact IMSI (15-digit numeric strings)
        result = IMSI_REGEX.replace(result) { match ->
            "•••••••••••${match.value.takeLast(4)}"
        }
        return result
    }
    
    /**
     * Detect One UI version from system properties
     */
    private fun getOneUIVersion(): String? {
        return try {
            val result = Shell.cmd("getprop ro.build.version.oneui").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                "One UI ${result.out[0]}"
            } else {
                // Try alternative property
                val result2 = Shell.cmd("getprop ro.csc.sales_code").exec()
                if (result2.isSuccess) {
                    "Samsung (One UI)"
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if Wi-Fi Calling settings activity exists
     */
    private fun checkWFCActivityExists(): Boolean {
        return try {
            val intent = Intent(ACTION_WIFI_CALLING_SETTINGS)
            val activities = context.packageManager.queryIntentActivities(intent, 0)
            activities.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
