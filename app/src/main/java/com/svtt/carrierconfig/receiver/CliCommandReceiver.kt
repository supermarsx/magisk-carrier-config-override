package com.svtt.carrierconfig.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.topjohnwu.superuser.Shell
import timber.log.Timber

/**
 * Receiver for CLI utility intents
 */
class CliCommandReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_STATUS = "com.svtt.carrierconfig.ACTION_STATUS"
        private const val ACTION_SCAN = "com.svtt.carrierconfig.ACTION_SCAN"
        private const val ACTION_EXPORT = "com.svtt.carrierconfig.ACTION_EXPORT"
        private const val EXTRA_MESSAGE = "message"
        private const val EXTRA_SUCCESS = "success"
        private const val EXTRA_TIMESTAMP = "timestamp"
        private const val EXTRA_ROOT_AVAILABLE = "root_available"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        Timber.d("Received CLI command: ${intent.action}")
        
        when (intent.action) {
            ACTION_STATUS -> {
                Timber.d("CLI: Status requested")
                sendSuccessResult(
                    message = "Status ready",
                    extra = Bundle().apply {
                        putLong(EXTRA_TIMESTAMP, System.currentTimeMillis())
                        putBoolean(EXTRA_ROOT_AVAILABLE, Shell.getShell().isRoot)
                    }
                )
            }
            ACTION_SCAN -> {
                Timber.d("CLI: Scan requested")
                sendSuccessResult("Diagnostic scan request accepted")
            }
            ACTION_EXPORT -> {
                Timber.d("CLI: Export requested")
                sendSuccessResult("Export request accepted")
            }
            else -> {
                sendErrorResult("Unknown action: ${intent.action}")
            }
        }
    }

    private fun sendSuccessResult(message: String, extra: Bundle? = null) {
        val data = Bundle().apply {
            putBoolean(EXTRA_SUCCESS, true)
            putString(EXTRA_MESSAGE, message)
        }
        extra?.let { data.putAll(it) }
        setResultCode(Activity.RESULT_OK)
        getResultExtras(true).putAll(data)
    }

    private fun sendErrorResult(message: String) {
        setResultCode(Activity.RESULT_CANCELED)
        getResultExtras(true).apply {
            putBoolean(EXTRA_SUCCESS, false)
            putString(EXTRA_MESSAGE, message)
            putLong(EXTRA_TIMESTAMP, System.currentTimeMillis())
        }
    }
}
