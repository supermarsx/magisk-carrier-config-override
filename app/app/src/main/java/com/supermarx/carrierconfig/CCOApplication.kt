package com.supermarx.carrierconfig

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.supermarx.carrierconfig.system.NotificationHelper
import com.supermarx.carrierconfig.system.StatusRefreshWorker
import com.supermarx.carrierconfig.system.UpdateCheckWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for CarrierConfig Override Manager
 */
@HiltAndroidApp
class CCOApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Schedule background workers
        StatusRefreshWorker.schedule(this)
        UpdateCheckWorker.schedule(this)
        
        // LibSU will auto-initialize when needed
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
