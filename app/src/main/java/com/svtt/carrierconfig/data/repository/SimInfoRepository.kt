package com.svtt.carrierconfig.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.svtt.carrierconfig.data.model.SimInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimInfoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    
    private val subscriptionManager: SubscriptionManager? by lazy {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
    }
    
    suspend fun getSimInfo(): List<SimInfo> = withContext(Dispatchers.IO) {
        if (!hasPhoneStatePermission()) {
            Timber.w("Phone state permission not granted")
            return@withContext emptyList()
        }
        
        try {
            val simInfoList = mutableListOf<SimInfo>()
            
            subscriptionManager?.activeSubscriptionInfoList?.forEachIndexed { index, subInfo ->
                try {
                    val carrierName = subInfo.carrierName?.toString() ?: "Unknown"
                    val mcc = subInfo.mccString ?: "Unknown"
                    val mnc = subInfo.mncString ?: "Unknown"
                    val slot = subInfo.simSlotIndex
                    
                    // Get network type
                    val networkType = getNetworkTypeName(telephonyManager.dataNetworkType)
                    
                    // Check roaming status
                    val isRoaming = telephonyManager.isNetworkRoaming
                    
                    // Get SIM state
                    val simState = getSimStateName(telephonyManager.simState)
                    
                    simInfoList.add(
                        SimInfo(
                            slot = slot,
                            carrierName = carrierName,
                            mcc = mcc,
                            mnc = mnc,
                            networkType = networkType,
                            isRoaming = isRoaming,
                            simState = simState,
                            phoneNumber = null // Privacy: don't retrieve phone number
                        )
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to get SIM info for slot $index")
                }
            }
            
            simInfoList
        } catch (e: Exception) {
            Timber.e(e, "Failed to get SIM info")
            emptyList()
        }
    }
    
    private fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun getNetworkTypeName(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "Wi-Fi"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS/3G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
            else -> "Other ($networkType)"
        }
    }
    
    private fun getSimStateName(simState: Int): String {
        return when (simState) {
            TelephonyManager.SIM_STATE_ABSENT -> "Absent"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently Disabled"
            TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card I/O Error"
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Restricted"
            else -> "Unknown"
        }
    }
}
