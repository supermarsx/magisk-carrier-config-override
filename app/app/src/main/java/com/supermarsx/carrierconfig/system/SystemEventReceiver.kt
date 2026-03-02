package com.supermarsx.carrierconfig.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.CarrierConfigManager
import android.telephony.TelephonyManager
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder

/**
 * Broadcast receiver for system events
 * Monitors network changes, carrier changes, and airplane mode
 */
class SystemEventReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED -> {
                handleCarrierConfigChange(context)
            }
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                handleAirplaneModeChange(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                handlePhoneStateChange(context)
            }
        }
    }
    
    private fun handleCarrierConfigChange(context: Context) {
        // Carrier config changed - refresh dashboard
        scheduleStatusRefresh(context)
        
        // Show notification if configured
        showNotification(
            context,
            "Carrier Config Changed",
            "CarrierConfig has been updated. Tap to view changes."
        )
    }
    
    private fun handleAirplaneModeChange(context: Context, intent: Intent) {
        val isAirplaneModeOn = intent.getBooleanExtra("state", false)
        
        if (!isAirplaneModeOn) {
            // Airplane mode turned off - wait and refresh
            scheduleDelayedStatusRefresh(context, delayMillis = 5000)
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        // Device booted - check if overrides need to be reapplied
        scheduleDelayedStatusRefresh(context, delayMillis = 10000)
    }
    
    private fun handlePhoneStateChange(context: Context) {
        // Phone state changed - might affect IMS registration
        scheduleStatusRefresh(context)
    }
    
    private fun scheduleStatusRefresh(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<StatusRefreshWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    private fun scheduleDelayedStatusRefresh(context: Context, delayMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<StatusRefreshWorker>()
            .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    private fun showNotification(context: Context, title: String, message: String) {
        // NotificationHelper.showNotification is synchronous — no coroutine needed
        NotificationHelper.showNotification(
            context,
            title = title,
            message = message,
            channelId = NotificationHelper.CHANNEL_SYSTEM_EVENTS
        )
    }
}
