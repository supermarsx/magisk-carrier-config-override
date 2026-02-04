package com.svtt.carrierconfig.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.svtt.carrierconfig.data.model.WfcUiStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WfcUiStatusRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun getWfcUiStatus(): WfcUiStatus = withContext(Dispatchers.IO) {
        try {
            // Common WFC settings activity intents
            val wfcIntents = listOf(
                "android.settings.WIFI_CALLING_SETTINGS",
                "com.samsung.settings.WIFI_CALLING_SETTINGS",
                "com.android.settings.WIFI_CALLING_SETTINGS"
            )
            
            var activityExists = false
            var activityPackage: String? = null
            var activityClass: String? = null
            
            for (action in wfcIntents) {
                val intent = Intent(action)
                val resolveInfo = context.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                
                if (resolveInfo != null) {
                    activityExists = true
                    activityPackage = resolveInfo.activityInfo.packageName
                    activityClass = resolveInfo.activityInfo.name
                    break
                }
            }
            
            // For now, we can't easily detect if the page populates or toggle is present
            // without actually launching it and inspecting the UI hierarchy
            // These would require UI testing tools or accessibility service
            val pagePopulates = activityExists // Assume true if activity exists
            val togglePresent = activityExists // Assume true if activity exists
            
            WfcUiStatus(
                settingsActivityExists = activityExists,
                pagePopulates = pagePopulates,
                togglePresent = togglePresent,
                activityPackage = activityPackage,
                activityClass = activityClass
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get WFC UI status")
            WfcUiStatus(
                settingsActivityExists = false,
                pagePopulates = false,
                togglePresent = false,
                activityPackage = null,
                activityClass = null
            )
        }
    }
}
