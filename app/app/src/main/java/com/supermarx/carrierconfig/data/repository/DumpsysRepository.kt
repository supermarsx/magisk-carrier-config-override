package com.supermarx.carrierconfig.data.repository

import android.content.Context
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for dumpsys system service information
 */
@Singleton
class DumpsysRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get IMS dumpsys information
     */
    suspend fun getDumpsysIms(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("ims")
    }
    
    /**
     * Get phone service dumpsys information
     */
    suspend fun getDumpsysPhone(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("phone")
    }
    
    /**
     * Get carrier config dumpsys information
     */
    suspend fun getDumpsysCarrierConfig(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("carrier_config")
    }
    
    /**
     * Get telecom service dumpsys information
     */
    suspend fun getDumpsysTelecom(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("telecom")
    }
    
    /**
     * Get connectivity service dumpsys information
     */
    suspend fun getDumpsysConnectivity(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("connectivity")
    }
    
    /**
     * Get network stats dumpsys information
     */
    suspend fun getDumpsysNetstats(): DumpsysResult = withContext(Dispatchers.IO) {
        executeDumpsys("netstats")
    }
    
    /**
     * Execute dumpsys command
     */
    private fun executeDumpsys(service: String): DumpsysResult {
        return try {
            val result = Shell.cmd("dumpsys $service").exec()
            
            if (result.isSuccess) {
                DumpsysResult.Success(
                    service = service,
                    output = result.out.joinToString("\n"),
                    lineCount = result.out.size
                )
            } else {
                DumpsysResult.Error(
                    service = service,
                    message = "Command failed: ${result.err.joinToString("\n")}"
                )
            }
        } catch (e: Exception) {
            DumpsysResult.Error(
                service = service,
                message = "Exception: ${e.message}"
            )
        }
    }
    
    /**
     * Get all relevant dumpsys outputs
     */
    suspend fun getAllDumpsys(): Map<String, DumpsysResult> = withContext(Dispatchers.IO) {
        mapOf(
            "ims" to getDumpsysIms(),
            "phone" to getDumpsysPhone(),
            "carrier_config" to getDumpsysCarrierConfig(),
            "telecom" to getDumpsysTelecom(),
            "connectivity" to getDumpsysConnectivity()
        )
    }
    
    /**
     * Extract specific information from IMS dumpsys
     */
    suspend fun extractImsInfo(): ImsExtractedInfo = withContext(Dispatchers.IO) {
        val result = getDumpsysIms()
        
        if (result is DumpsysResult.Success) {
            val output = result.output
            
            ImsExtractedInfo(
                registered = output.contains("mRegistered=true", ignoreCase = true),
                voiceCapable = output.contains("VOICE", ignoreCase = true),
                videoCapable = output.contains("VIDEO", ignoreCase = true),
                voWifiCapable = output.contains("ePDG", ignoreCase = true) || 
                               output.contains("WIFI_CALLING", ignoreCase = true),
                registrationType = extractRegistrationType(output),
                imsFeatures = extractImsFeatures(output)
            )
        } else {
            ImsExtractedInfo(
                registered = false,
                voiceCapable = false,
                videoCapable = false,
                voWifiCapable = false,
                registrationType = "Unknown",
                imsFeatures = emptyList()
            )
        }
    }
    
    /**
     * Extract registration type from IMS output
     */
    private fun extractRegistrationType(output: String): String {
        return when {
            output.contains("TYPE_WIFI", ignoreCase = true) -> "Wi-Fi"
            output.contains("TYPE_LTE", ignoreCase = true) -> "LTE"
            output.contains("TYPE_NR", ignoreCase = true) -> "5G NR"
            else -> "Unknown"
        }
    }
    
    /**
     * Extract IMS features from output
     */
    private fun extractImsFeatures(output: String): List<String> {
        val features = mutableListOf<String>()
        
        if (output.contains("MMTEL", ignoreCase = true)) {
            features.add("MMTEL")
        }
        if (output.contains("RCS", ignoreCase = true)) {
            features.add("RCS")
        }
        if (output.contains("UT", ignoreCase = true)) {
            features.add("UT (XCAP)")
        }
        if (output.contains("SMS", ignoreCase = true)) {
            features.add("SMS over IP")
        }
        
        return features
    }
}

/**
 * Dumpsys result sealed class
 */
sealed class DumpsysResult {
    data class Success(
        val service: String,
        val output: String,
        val lineCount: Int
    ) : DumpsysResult()
    
    data class Error(
        val service: String,
        val message: String
    ) : DumpsysResult()
}

/**
 * Extracted IMS information from dumpsys
 */
data class ImsExtractedInfo(
    val registered: Boolean,
    val voiceCapable: Boolean,
    val videoCapable: Boolean,
    val voWifiCapable: Boolean,
    val registrationType: String,
    val imsFeatures: List<String>
)
