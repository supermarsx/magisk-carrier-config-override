package com.supermarx.carrierconfig.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import com.supermarx.carrierconfig.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for device information and system queries
 */
@Singleton
class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    
    private val subscriptionManager: SubscriptionManager by lazy {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
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
                subscriptionManager.activeSubscriptionInfoList ?: emptyList()
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
        // TODO: Implement IMS status detection via dumpsys or reflection
        // For now, return mock data
        IMSStatus(
            isRegistered = false,
            isVoLTEAvailable = false,
            isVoWiFiAvailable = false,
            registrationState = "NOT_REGISTERED"
        )
    }
    
    /**
     * Get Wi-Fi Calling UI status
     */
    suspend fun getWFCUIStatus(): WFCUIStatus = withContext(Dispatchers.IO) {
        val wfcActivityExists = checkWFCActivityExists()
        
        // TODO: More sophisticated checks
        WFCUIStatus(
            settingsActivityExists = wfcActivityExists,
            pagePopulates = false,  // Requires actual inspection
            togglePresent = false    // Requires actual inspection
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
            val intent = Intent(Settings.ACTION_WIFI_CALLING_SETTINGS).apply {
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
    suspend fun exportReport(state: DashboardState) = withContext(Dispatchers.IO) {
        // TODO: Implement report export to file
        // Generate JSON and text reports
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
            val intent = Intent(Settings.ACTION_WIFI_CALLING_SETTINGS)
            val activities = context.packageManager.queryIntentActivities(intent, 0)
            activities.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
