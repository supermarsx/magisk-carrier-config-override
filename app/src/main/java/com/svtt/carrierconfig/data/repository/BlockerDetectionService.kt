package com.svtt.carrierconfig.data.repository

import com.svtt.carrierconfig.data.model.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockerDetectionService @Inject constructor() {
    
    fun detectBlocker(
        imsStatus: ImsStatus?,
        wfcUiStatus: WfcUiStatus?,
        simInfo: List<SimInfo>
    ): BlockerAnalysis {
        Timber.d("Detecting blocker...")
        
        // No SIM
        if (simInfo.isEmpty()) {
            return BlockerAnalysis(
                blockerType = BlockerType.MULTIPLE,
                confidence = Confidence.HIGH,
                description = "No SIM card detected",
                suggestedAction = "Insert a SIM card to enable Wi-Fi Calling"
            )
        }
        
        // IMS not registered
        if (imsStatus?.isRegistered == false) {
            return BlockerAnalysis(
                blockerType = BlockerType.IMS_NOT_REGISTERED,
                confidence = Confidence.HIGH,
                description = "IMS service is not registered. This is the primary blocker.",
                suggestedAction = "Check carrier support for VoLTE/VoWiFi. Ensure data is enabled."
            )
        }
        
        // WFC UI doesn't exist
        if (wfcUiStatus?.settingsActivityExists == false) {
            return BlockerAnalysis(
                blockerType = BlockerType.CARRIER_CONFIG,
                confidence = Confidence.HIGH,
                description = "Wi-Fi Calling settings not available. Likely blocked by CarrierConfig.",
                suggestedAction = "Try Method 1: Apply CarrierConfig overrides to expose WFC UI"
            )
        }
        
        // IMS registered but VoWiFi not available
        if (imsStatus?.isRegistered == true && imsStatus.isVoWiFiAvailable == false) {
            return if (wfcUiStatus?.pagePopulates == false) {
                // Settings exists but page empty
                BlockerAnalysis(
                    blockerType = BlockerType.CSC_ENTITLEMENT,
                    confidence = Confidence.MEDIUM,
                    description = "IMS registered but WFC UI blocked. Likely CSC or entitlement gate.",
                    suggestedAction = "Try Method 2: Use entitlement simulation to bypass checks"
                )
            } else {
                // Settings exists and populates
                BlockerAnalysis(
                    blockerType = BlockerType.ENTITLEMENT,
                    confidence = Confidence.MEDIUM,
                    description = "WFC UI present but entitlement check may be failing.",
                    suggestedAction = "Try Method 2: Simulate entitlement responses"
                )
            }
        }
        
        // Everything looks good
        if (imsStatus?.isRegistered == true && 
            imsStatus.isVoWiFiAvailable && 
            wfcUiStatus?.settingsActivityExists == true) {
            return BlockerAnalysis(
                blockerType = BlockerType.NONE,
                confidence = Confidence.HIGH,
                description = "Wi-Fi Calling appears to be available!",
                suggestedAction = "Open Wi-Fi Calling settings to configure"
            )
        }
        
        // Unknown case
        return BlockerAnalysis(
            blockerType = BlockerType.UNKNOWN,
            confidence = Confidence.LOW,
            description = "Unable to determine blocker. Requires manual investigation.",
            suggestedAction = "Run full diagnostics and export report for analysis"
        )
    }
}
