package com.svtt.carrierconfig.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.svtt.carrierconfig.ui.navigation.NavGraph
import com.svtt.carrierconfig.ui.theme.SVTTTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        Timber.d("MainActivity created")
        
        setContent {
            SVTTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
    
    fun openWfcSettings() {
        try {
            val wfcIntents = listOf(
                "android.settings.WIFI_CALLING_SETTINGS",
                "com.samsung.settings.WIFI_CALLING_SETTINGS",
                "com.android.settings.WIFI_CALLING_SETTINGS"
            )
            
            for (action in wfcIntents) {
                try {
                    val intent = Intent(action).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                        Timber.d("Opened WFC settings with action: $action")
                        return
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to open WFC settings with action: $action")
                }
            }
            
            Timber.w("No WFC settings activity found")
        } catch (e: Exception) {
            Timber.e(e, "Error opening WFC settings")
        }
    }
}
