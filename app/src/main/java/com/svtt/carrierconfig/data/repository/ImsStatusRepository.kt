package com.svtt.carrierconfig.data.repository

import com.topjohnwu.superuser.Shell
import com.svtt.carrierconfig.data.model.ImsStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImsStatusRepository @Inject constructor() {
    
    suspend fun getImsStatus(): ImsStatus = withContext(Dispatchers.IO) {
        try {
            val dumpsysOutput = getDumpsysIms()
            parseImsStatus(dumpsysOutput)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get IMS status")
            ImsStatus(
                isRegistered = false,
                isVoLteAvailable = false,
                isVoWiFiAvailable = false,
                imsFeatures = emptyList(),
                registrationTech = null
            )
        }
    }
    
    private suspend fun getDumpsysIms(): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("dumpsys ims").exec()
            if (result.isSuccess) {
                result.out.joinToString("\n")
            } else {
                Timber.w("Failed to execute dumpsys ims: ${result.code}")
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception executing dumpsys ims")
            ""
        }
    }
    
    private fun parseImsStatus(dumpsysOutput: String): ImsStatus {
        if (dumpsysOutput.isBlank()) {
            return ImsStatus(
                isRegistered = false,
                isVoLteAvailable = false,
                isVoWiFiAvailable = false,
                imsFeatures = emptyList(),
                registrationTech = null
            )
        }
        
        val lines = dumpsysOutput.lines()
        var isRegistered = false
        var isVoLteAvailable = false
        var isVoWiFiAvailable = false
        val imsFeatures = mutableListOf<String>()
        var registrationTech: String? = null
        
        // Parse dumpsys output
        // Note: This is a best-effort parser. Actual format may vary by device/firmware
        for (line in lines) {
            val trimmed = line.trim().lowercase()
            
            // Check registration status
            if (trimmed.contains("registered") && trimmed.contains("true")) {
                isRegistered = true
            }
            if (trimmed.contains("ims registered") || trimmed.contains("imsregistered: true")) {
                isRegistered = true
            }
            
            // Check VoLTE
            if (trimmed.contains("volte") && 
                (trimmed.contains("enabled") || trimmed.contains("available") || trimmed.contains("true"))) {
                isVoLteAvailable = true
                imsFeatures.add("VoLTE")
            }
            
            // Check VoWiFi/WFC
            if ((trimmed.contains("vowifi") || trimmed.contains("wfc")) && 
                (trimmed.contains("enabled") || trimmed.contains("available") || trimmed.contains("true"))) {
                isVoWiFiAvailable = true
                imsFeatures.add("VoWiFi")
            }
            
            // Check registration technology
            if (trimmed.contains("rat:") || trimmed.contains("registration tech")) {
                when {
                    trimmed.contains("lte") -> registrationTech = "LTE"
                    trimmed.contains("nr") || trimmed.contains("5g") -> registrationTech = "5G NR"
                    trimmed.contains("iwlan") || trimmed.contains("wifi") -> registrationTech = "Wi-Fi"
                    trimmed.contains("umts") || trimmed.contains("3g") -> registrationTech = "UMTS"
                }
            }
        }
        
        return ImsStatus(
            isRegistered = isRegistered,
            isVoLteAvailable = isVoLteAvailable,
            isVoWiFiAvailable = isVoWiFiAvailable,
            imsFeatures = imsFeatures.distinct(),
            registrationTech = registrationTech
        )
    }
}
