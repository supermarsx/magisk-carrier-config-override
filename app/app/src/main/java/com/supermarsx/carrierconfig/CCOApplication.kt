package com.supermarsx.carrierconfig

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.supermarsx.carrierconfig.system.BackgroundWorkers
import com.supermarsx.carrierconfig.system.NotificationHelper
import com.supermarsx.carrierconfig.system.StatusRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
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

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        NotificationHelper.createNotificationChannels(this)
        BackgroundWorkers.scheduleAll(this)
        registerNetworkCallback()
    }

    /**
     * Register a ConnectivityManager.NetworkCallback to monitor connectivity changes.
     * Replaces the deprecated CONNECTIVITY_ACTION manifest broadcast.
     */
    private fun registerNetworkCallback() {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            @Volatile
            private var lastRefreshTime = 0L
            private val throttleMs = 30_000L // 30-second throttle

            private fun scheduleThrottled() {
                val now = System.currentTimeMillis()
                if (now - lastRefreshTime < throttleMs) return
                lastRefreshTime = now
                Timber.d("Network change — scheduling throttled status refresh")
                val work = OneTimeWorkRequestBuilder<StatusRefreshWorker>().build()
                WorkManager.getInstance(this@CCOApplication).enqueue(work)
            }

            override fun onAvailable(network: Network) {
                scheduleThrottled()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val wifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val cell = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                if (wifi || cell) {
                    scheduleThrottled()
                }
            }
        })
    }
}
