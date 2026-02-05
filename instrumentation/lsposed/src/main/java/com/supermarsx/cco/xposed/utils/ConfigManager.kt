package com.supermarsx.cco.xposed.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File

/**
 * Configuration manager for runtime config
 */
class ConfigManager {
    
    data class Config(
        val features: Features = Features(),
        val logging: Logging = Logging(),
        val modules: Modules = Modules()
    )
    
    data class Features(
        val autoBypass: Boolean = true,
        val captureEvents: Boolean = false,
        val diagnostics: Boolean = false
    )
    
    data class Logging(
        val level: String = "info",
        val console: Boolean = true,
        val file: Boolean = false
    )
    
    data class Modules(
        val ims: Boolean = true,
        val carrierconfig: Boolean = true,
        val telephony: Boolean = true,
        val settings: Boolean = true
    )
    
    private val gson = Gson()
    private var config: Config = Config()
    
    private val configPath = "/data/adb/cco/xposed_config.json"
    
    fun loadConfig() {
        try {
            val file = File(configPath)
            if (file.exists()) {
                val json = file.readText()
                config = gson.fromJson(json, Config::class.java)
            }
        } catch (e: Exception) {
            // Use defaults
        }
    }
    
    fun saveConfig() {
        try {
            val file = File(configPath)
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(config))
        } catch (e: Exception) {
            // Ignore save errors
        }
    }
    
    fun getConfig(): Config = config
    
    fun updateConfig(newConfig: Config) {
        config = newConfig
        saveConfig()
    }
    
    fun isFeatureEnabled(feature: String): Boolean {
        return when (feature) {
            "autoBypass" -> config.features.autoBypass
            "captureEvents" -> config.features.captureEvents
            "diagnostics" -> config.features.diagnostics
            else -> false
        }
    }
    
    fun isModuleEnabled(module: String): Boolean {
        return when (module) {
            "ims" -> config.modules.ims
            "carrierconfig" -> config.modules.carrierconfig
            "telephony" -> config.modules.telephony
            "settings" -> config.modules.settings
            else -> true
        }
    }
}
