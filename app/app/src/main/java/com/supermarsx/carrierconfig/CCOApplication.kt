package com.supermarsx.carrierconfig

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.supermarsx.carrierconfig.system.BackgroundWorkers
import com.supermarsx.carrierconfig.system.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for CarrierConfig Override Manager
 */
@HiltAndroidApp
class CCOApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannels(this)
        BackgroundWorkers.scheduleAll(this)
    }
}
