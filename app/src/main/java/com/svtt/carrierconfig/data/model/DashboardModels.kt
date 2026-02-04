package com.svtt.carrierconfig.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val buildFingerprint: String,
    val oneUiVersion: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val kernelVersion: String,
    val isRooted: Boolean
) : Parcelable

@Parcelize
data class SimInfo(
    val slot: Int,
    val carrierName: String,
    val mcc: String,
    val mnc: String,
    val networkType: String,
    val isRoaming: Boolean,
    val simState: String,
    val phoneNumber: String? = null
) : Parcelable

@Parcelize
data class ImsStatus(
    val isRegistered: Boolean,
    val isVoLteAvailable: Boolean,
    val isVoWiFiAvailable: Boolean,
    val imsFeatures: List<String> = emptyList(),
    val registrationTech: String? = null
) : Parcelable

@Parcelize
data class WfcUiStatus(
    val settingsActivityExists: Boolean,
    val pagePopulates: Boolean,
    val togglePresent: Boolean,
    val activityPackage: String? = null,
    val activityClass: String? = null
) : Parcelable

@Parcelize
data class BlockerAnalysis(
    val blockerType: BlockerType,
    val confidence: Confidence,
    val description: String,
    val suggestedAction: String
) : Parcelable

enum class BlockerType {
    NONE,
    IMS_NOT_REGISTERED,
    CARRIER_CONFIG,
    CSC_ENTITLEMENT,
    ENTITLEMENT,
    MULTIPLE,
    UNKNOWN
}

enum class Confidence {
    HIGH,
    MEDIUM,
    LOW
}

@Parcelize
data class DashboardState(
    val deviceInfo: DeviceInfo? = null,
    val simInfo: List<SimInfo> = emptyList(),
    val imsStatus: ImsStatus? = null,
    val wfcUiStatus: WfcUiStatus? = null,
    val blockerAnalysis: BlockerAnalysis? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) : Parcelable
