package com.supermarsx.carrierconfig.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import com.supermarsx.carrierconfig.ui.navigation.CCONavHost
import com.supermarsx.carrierconfig.ui.navigation.Screen
import com.supermarsx.carrierconfig.ui.theme.CCOTheme

/**
 * Main Activity for CCO app
 * Uses Jetpack Compose for UI with glassmorphism design
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val startDestination = when (intent?.data?.host ?: intent?.data?.path?.removePrefix("/")) {
            "deploy", "carrier_config" -> Screen.CarrierConfig.route
            "export_report", "diagnostics" -> Screen.Diagnostics.route
            "settings" -> Screen.Settings.route
            "about" -> Screen.About.route
            else -> Screen.Dashboard.route
        }
        
        setContent {
            CCOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CCONavHost(startDestination = startDestination)
                }
            }
        }
    }
}
