package dev.mars.carrierconfig.system

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.mars.carrierconfig.data.repository.DeviceRepository
import java.util.concurrent.TimeUnit

/**
 * Background worker for periodic status refresh
 */
@HiltWorker
class StatusRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceRepository: DeviceRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val WORK_NAME = "status_refresh_work"
        private const val REFRESH_INTERVAL_HOURS = 6L
        
        /**
         * Schedule periodic status refresh
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<StatusRefreshWorker>(
                REFRESH_INTERVAL_HOURS, TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        /**
         * Cancel periodic refresh
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Refresh device info
            deviceRepository.getDeviceInfo()
            
            // Refresh SIM info
            deviceRepository.getSIMInfo()
            
            // Refresh IMS status
            deviceRepository.getIMSStatus()
            
            // Check for issues and notify if needed
            val wfcStatus = deviceRepository.getWFCUIStatus()
            if (!wfcStatus.settingsActivityExists) {
                NotificationHelper.showNotification(
                    applicationContext,
                    title = "WFC Issue Detected",
                    message = "Wi-Fi Calling settings not available. Check CCO Manager.",
                    channelId = NotificationHelper.CHANNEL_STATUS_ALERTS
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Background worker for update checking
 */
@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val WORK_NAME = "update_check_work"
        private const val CHECK_INTERVAL_DAYS = 3L
        
        /**
         * Schedule periodic update checks
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                CHECK_INTERVAL_DAYS, TimeUnit.DAYS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        /**
         * Cancel update checks
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val result = com.supermarx.carrierconfig.util.UpdateChecker.checkForUpdates(applicationContext)
            
            when (result) {
                is com.supermarx.carrierconfig.util.UpdateCheckResult.UpdateAvailable -> {
                    NotificationHelper.showUpdateNotification(
                        applicationContext,
                        version = result.latestVersion,
                        downloadUrl = result.downloadUrl
                    )
                }
                else -> {
                    // No update or error - do nothing
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
