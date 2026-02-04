package com.svtt.carrierconfig.data.repository

import android.content.Context
import android.os.Build
import com.topjohnwu.superuser.Shell
import com.svtt.carrierconfig.data.model.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        try {
            val model = Build.MODEL
            val manufacturer = Build.MANUFACTURER
            val buildFingerprint = Build.FINGERPRINT
            val androidVersion = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT
            val kernelVersion = getKernelVersion()
            val oneUiVersion = getOneUiVersion()
            val isRooted = checkRoot()
            
            DeviceInfo(
                model = model,
                manufacturer = manufacturer,
                buildFingerprint = buildFingerprint,
                oneUiVersion = oneUiVersion,
                androidVersion = androidVersion,
                sdkVersion = sdkVersion,
                kernelVersion = kernelVersion,
                isRooted = isRooted
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get device info")
            // Return default values
            DeviceInfo(
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                buildFingerprint = Build.FINGERPRINT,
                oneUiVersion = "Unknown",
                androidVersion = Build.VERSION.RELEASE,
                sdkVersion = Build.VERSION.SDK_INT,
                kernelVersion = "Unknown",
                isRooted = false
            )
        }
    }
    
    private suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            Timber.e(e, "Failed to check root")
            false
        }
    }
    
    private suspend fun getKernelVersion(): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("uname -r").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out[0]
            } else {
                System.getProperty("os.version") ?: "Unknown"
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get kernel version")
            System.getProperty("os.version") ?: "Unknown"
        }
    }
    
    private suspend fun getOneUiVersion(): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("getprop ro.build.version.oneui").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                val version = result.out[0]
                if (version.isNotBlank()) {
                    "One UI $version"
                } else {
                    detectOneUiFromBuild()
                }
            } else {
                detectOneUiFromBuild()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get One UI version")
            detectOneUiFromBuild()
        }
    }
    
    private fun detectOneUiFromBuild(): String {
        val buildFingerprint = Build.FINGERPRINT.lowercase()
        return when {
            buildFingerprint.contains("oneui_7") -> "One UI 7.x"
            buildFingerprint.contains("oneui_6.1") -> "One UI 6.1.x"
            buildFingerprint.contains("oneui_6") -> "One UI 6.x"
            buildFingerprint.contains("oneui_5") -> "One UI 5.x"
            Build.VERSION.SDK_INT >= 35 -> "One UI 7.x (estimated)"
            Build.VERSION.SDK_INT >= 34 -> "One UI 6.1.x (estimated)"
            Build.VERSION.SDK_INT >= 33 -> "One UI 6.x (estimated)"
            Build.VERSION.SDK_INT >= 33 -> "One UI 5.x (estimated)"
            else -> "Unknown / Not Samsung"
        }
    }
}
