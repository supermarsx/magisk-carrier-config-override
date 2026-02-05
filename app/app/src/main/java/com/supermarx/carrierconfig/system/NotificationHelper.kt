package com.supermarx.carrierconfig.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.supermarx.carrierconfig.R

/**
 * Notification helper for system alerts and updates
 */
object NotificationHelper {
    
    const val CHANNEL_STATUS_ALERTS = "status_alerts"
    const val CHANNEL_SYSTEM_EVENTS = "system_events"
    const val CHANNEL_UPDATES = "app_updates"
    
    private const val NOTIFICATION_ID_STATUS = 100
    private const val NOTIFICATION_ID_SYSTEM = 101
    private const val NOTIFICATION_ID_UPDATE = 102
    
    /**
     * Create notification channels
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Status alerts channel
        val statusChannel = NotificationChannel(
            CHANNEL_STATUS_ALERTS,
            "Status Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about Wi-Fi Calling and IMS status"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(statusChannel)
        
        // System events channel
        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM_EVENTS,
            "System Events",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications about carrier config and system changes"
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(systemChannel)
        
        // Updates channel
        val updateChannel = NotificationChannel(
            CHANNEL_UPDATES,
            "App Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about new app versions"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(updateChannel)
    }
    
    /**
     * Show generic notification
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String,
        notificationId: Int = NOTIFICATION_ID_SYSTEM
    ) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to create this
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    /**
     * Show update available notification
     */
    fun showUpdateNotification(
        context: Context,
        version: String,
        downloadUrl: String
    ) {
        val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            downloadIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("CCO Manager Update Available")
            .setContentText("Version $version is now available")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("A new version ($version) of CCO Manager is available. Tap to download."))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                R.drawable.ic_download,
                "Download",
                pendingIntent
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_UPDATE, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    /**
     * Show WFC status notification
     */
    fun showWfcStatusNotification(
        context: Context,
        isEnabled: Boolean,
        isRegistered: Boolean
    ) {
        val title = if (isEnabled && isRegistered) {
            "Wi-Fi Calling Active"
        } else {
            "Wi-Fi Calling Issue"
        }
        
        val message = when {
            !isEnabled -> "Wi-Fi Calling is disabled. Enable it in settings."
            !isRegistered -> "Wi-Fi Calling not registered. Check your connection."
            else -> "Wi-Fi Calling is working properly."
        }
        
        showNotification(
            context,
            title = title,
            message = message,
            channelId = CHANNEL_STATUS_ALERTS,
            notificationId = NOTIFICATION_ID_STATUS
        )
    }
    
    /**
     * Cancel notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
