package com.svtt.carrierconfig.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diagnostic report data
 */
@Parcelize
data class DiagnosticReport(
    val timestamp: Long = System.currentTimeMillis(),
    val deviceInfo: DeviceInfo?,
    val simInfo: List<SimInfo>,
    val imsStatus: ImsStatus?,
    val wfcUiStatus: WfcUiStatus?,
    val blockerAnalysis: BlockerAnalysis?,
    val logs: Map<String, String> = emptyMap()
) : Parcelable {
    
    fun toJson(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val date = dateFormat.format(Date(timestamp))
        
        return buildString {
            appendLine("{")
            appendLine("  \"timestamp\": \"$date\",")
            appendLine("  \"device\": {")
            deviceInfo?.let {
                appendLine("    \"model\": \"${it.model}\",")
                appendLine("    \"manufacturer\": \"${it.manufacturer}\",")
                appendLine("    \"oneui_version\": \"${it.oneUiVersion}\",")
                appendLine("    \"android_version\": \"${it.androidVersion}\",")
                appendLine("    \"build_fingerprint\": \"${it.buildFingerprint}\",")
                appendLine("    \"is_rooted\": ${it.isRooted}")
            }
            appendLine("  },")
            appendLine("  \"sim_cards\": [")
            simInfo.forEachIndexed { index, sim ->
                appendLine("    {")
                appendLine("      \"slot\": ${sim.slot},")
                appendLine("      \"carrier\": \"${sim.carrierName}\",")
                appendLine("      \"mcc\": \"${sim.mcc}\",")
                appendLine("      \"mnc\": \"${sim.mnc}\",")
                appendLine("      \"network_type\": \"${sim.networkType}\",")
                appendLine("      \"is_roaming\": ${sim.isRoaming}")
                append("    }")
                if (index < simInfo.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ],")
            appendLine("  \"ims_status\": {")
            imsStatus?.let {
                appendLine("    \"registered\": ${it.isRegistered},")
                appendLine("    \"volte_available\": ${it.isVoLteAvailable},")
                appendLine("    \"vowifi_available\": ${it.isVoWiFiAvailable},")
                appendLine("    \"features\": ${it.imsFeatures.toJsonArray()}")
            }
            appendLine("  },")
            appendLine("  \"wfc_ui_status\": {")
            wfcUiStatus?.let {
                appendLine("    \"settings_activity_exists\": ${it.settingsActivityExists},")
                appendLine("    \"page_populates\": ${it.pagePopulates},")
                appendLine("    \"toggle_present\": ${it.togglePresent}")
            }
            appendLine("  },")
            appendLine("  \"blocker_analysis\": {")
            blockerAnalysis?.let {
                appendLine("    \"blocker_type\": \"${it.blockerType}\",")
                appendLine("    \"confidence\": \"${it.confidence}\",")
                appendLine("    \"description\": \"${it.description}\",")
                appendLine("    \"suggested_action\": \"${it.suggestedAction}\"")
            }
            appendLine("  }")
            appendLine("}")
        }
    }
    
    fun toText(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val date = dateFormat.format(Date(timestamp))
        
        return buildString {
            appendLine("===========================================")
            appendLine("SVTT CarrierConfig Diagnostic Report")
            appendLine("Generated: $date")
            appendLine("===========================================")
            appendLine()
            
            appendLine("DEVICE INFORMATION")
            appendLine("-------------------------------------------")
            deviceInfo?.let {
                appendLine("Model: ${it.model}")
                appendLine("Manufacturer: ${it.manufacturer}")
                appendLine("One UI: ${it.oneUiVersion}")
                appendLine("Android: ${it.androidVersion}")
                appendLine("Build: ${it.buildFingerprint}")
                appendLine("Root Access: ${if (it.isRooted) "Yes" else "No"}")
            }
            appendLine()
            
            appendLine("SIM INFORMATION")
            appendLine("-------------------------------------------")
            if (simInfo.isEmpty()) {
                appendLine("No SIM cards detected")
            } else {
                simInfo.forEach { sim ->
                    appendLine("SIM ${sim.slot + 1}:")
                    appendLine("  Carrier: ${sim.carrierName}")
                    appendLine("  MCC/MNC: ${sim.mcc}/${sim.mnc}")
                    appendLine("  Network: ${sim.networkType}")
                    appendLine("  Roaming: ${if (sim.isRoaming) "Yes" else "No"}")
                    appendLine("  State: ${sim.simState}")
                }
            }
            appendLine()
            
            appendLine("IMS STATUS")
            appendLine("-------------------------------------------")
            imsStatus?.let {
                appendLine("IMS Registered: ${if (it.isRegistered) "Yes" else "No"}")
                appendLine("VoLTE Available: ${if (it.isVoLteAvailable) "Yes" else "No"}")
                appendLine("VoWiFi Available: ${if (it.isVoWiFiAvailable) "Yes" else "No"}")
                if (it.imsFeatures.isNotEmpty()) {
                    appendLine("Features: ${it.imsFeatures.joinToString(", ")}")
                }
                it.registrationTech?.let { tech ->
                    appendLine("Technology: $tech")
                }
            }
            appendLine()
            
            appendLine("WFC UI STATUS")
            appendLine("-------------------------------------------")
            wfcUiStatus?.let {
                appendLine("Settings Activity: ${if (it.settingsActivityExists) "Found" else "Not Found"}")
                appendLine("Page Populates: ${if (it.pagePopulates) "Yes" else "No"}")
                appendLine("Toggle Present: ${if (it.togglePresent) "Yes" else "No"}")
            }
            appendLine()
            
            appendLine("BLOCKER ANALYSIS")
            appendLine("-------------------------------------------")
            blockerAnalysis?.let {
                appendLine("Detected Blocker: ${it.blockerType.name.replace("_", " ")}")
                appendLine("Confidence: ${it.confidence}")
                appendLine("Description: ${it.description}")
                appendLine("Suggested Action: ${it.suggestedAction}")
            }
            appendLine()
            
            appendLine("===========================================")
            appendLine("End of Report")
            appendLine("===========================================")
        }
    }
    
    private fun List<String>.toJsonArray(): String {
        return "[" + joinToString(", ") { "\"$it\"" } + "]"
    }
}
