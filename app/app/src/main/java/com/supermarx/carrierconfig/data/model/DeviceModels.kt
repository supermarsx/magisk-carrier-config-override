package com.supermarx.carrierconfig.data.model

/**
 * Device information data model
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val buildFingerprint: String,
    val androidVersion: String,
    val oneUIVersion: String?,
    val securityPatch: String,
    val isRooted: Boolean
)

/**
 * SIM card information
 */
data class SIMInfo(
    val slotIndex: Int,
    val carrierName: String?,
    val mcc: String?,
    val mnc: String?,
    val iccid: String?,  // Redacted for privacy
    val isActive: Boolean
)

/**
 * IMS registration status
 */
data class IMSStatus(
    val isRegistered: Boolean,
    val isVoLTEAvailable: Boolean,
    val isVoWiFiAvailable: Boolean,
    val registrationState: String
)

/**
 * Wi-Fi Calling UI availability status
 */
data class WFCUIStatus(
    val settingsActivityExists: Boolean,
    val pagePopulates: Boolean,
    val togglePresent: Boolean
)

/**
 * Detected blocker for WFC functionality
 */
enum class WFCBlocker {
    NONE,
    IMS_NOT_REGISTERED,
    CARRIER_CONFIG_GATE,
    CSC_GATE,
    ENTITLEMENT_GATE,
    SETTINGS_MISSING,
    UNKNOWN
}

/**
 * Complete dashboard state
 */
data class DashboardState(
    val deviceInfo: DeviceInfo? = null,
    val simInfo: List<SIMInfo> = emptyList(),
    val imsStatus: IMSStatus? = null,
    val wfcUIStatus: WFCUIStatus? = null,
    val detectedBlocker: WFCBlocker = WFCBlocker.UNKNOWN,
    val isLoading: Boolean = true,
    val error: String? = null
)
