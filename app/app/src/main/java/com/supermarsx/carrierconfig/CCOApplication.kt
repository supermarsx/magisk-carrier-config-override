package com.supermarsx.carrierconfig

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for CarrierConfig Override Manager
 */
@HiltAndroidApp
class CCOApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize application-wide components here
        // LibSU will auto-initialize when needed
    }
}
