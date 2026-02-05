package com.supermarsx.cco.xposed.utils

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Unit tests for ConfigManager
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class ConfigManagerTest {
    
    private lateinit var configManager: ConfigManager
    private lateinit var tempConfigFile: File
    private val gson = Gson()
    
    @Before
    fun setup() {
        configManager = ConfigManager()
        
        // Create temp config file for testing
        tempConfigFile = File.createTempFile("xposed_config", ".json")
        tempConfigFile.deleteOnExit()
    }
    
    @Test
    fun `test default config values`() {
        val config = configManager.getConfig()
        
        assertThat(config.features.autoBypass).isTrue()
        assertThat(config.features.captureEvents).isFalse()
        assertThat(config.features.diagnostics).isFalse()
        
        assertThat(config.logging.level).isEqualTo("info")
        assertThat(config.logging.console).isTrue()
        assertThat(config.logging.file).isFalse()
        
        assertThat(config.modules.ims).isTrue()
        assertThat(config.modules.carrierconfig).isTrue()
        assertThat(config.modules.telephony).isTrue()
        assertThat(config.modules.settings).isTrue()
    }
    
    @Test
    fun `test config features data class`() {
        val features = ConfigManager.Features(
            autoBypass = false,
            captureEvents = true,
            diagnostics = true
        )
        
        assertThat(features.autoBypass).isFalse()
        assertThat(features.captureEvents).isTrue()
        assertThat(features.diagnostics).isTrue()
    }
    
    @Test
    fun `test config logging data class`() {
        val logging = ConfigManager.Logging(
            level = "debug",
            console = false,
            file = true
        )
        
        assertThat(logging.level).isEqualTo("debug")
        assertThat(logging.console).isFalse()
        assertThat(logging.file).isTrue()
    }
    
    @Test
    fun `test config modules data class`() {
        val modules = ConfigManager.Modules(
            ims = false,
            carrierconfig = false,
            telephony = false,
            settings = false
        )
        
        assertThat(modules.ims).isFalse()
        assertThat(modules.carrierconfig).isFalse()
        assertThat(modules.telephony).isFalse()
        assertThat(modules.settings).isFalse()
    }
    
    @Test
    fun `test updateConfig`() {
        val newConfig = ConfigManager.Config(
            features = ConfigManager.Features(autoBypass = false),
            logging = ConfigManager.Logging(level = "debug"),
            modules = ConfigManager.Modules(ims = false)
        )
        
        configManager.updateConfig(newConfig)
        val updatedConfig = configManager.getConfig()
        
        assertThat(updatedConfig.features.autoBypass).isFalse()
        assertThat(updatedConfig.logging.level).isEqualTo("debug")
        assertThat(updatedConfig.modules.ims).isFalse()
    }
    
    @Test
    fun `test isFeatureEnabled with autoBypass`() {
        assertThat(configManager.isFeatureEnabled("autoBypass")).isTrue()
        
        val config = configManager.getConfig()
        val newConfig = config.copy(
            features = config.features.copy(autoBypass = false)
        )
        configManager.updateConfig(newConfig)
        
        assertThat(configManager.isFeatureEnabled("autoBypass")).isFalse()
    }
    
    @Test
    fun `test isFeatureEnabled with captureEvents`() {
        assertThat(configManager.isFeatureEnabled("captureEvents")).isFalse()
        
        val config = configManager.getConfig()
        val newConfig = config.copy(
            features = config.features.copy(captureEvents = true)
        )
        configManager.updateConfig(newConfig)
        
        assertThat(configManager.isFeatureEnabled("captureEvents")).isTrue()
    }
    
    @Test
    fun `test isFeatureEnabled with diagnostics`() {
        assertThat(configManager.isFeatureEnabled("diagnostics")).isFalse()
        
        val config = configManager.getConfig()
        val newConfig = config.copy(
            features = config.features.copy(diagnostics = true)
        )
        configManager.updateConfig(newConfig)
        
        assertThat(configManager.isFeatureEnabled("diagnostics")).isTrue()
    }
    
    @Test
    fun `test isFeatureEnabled with unknown feature`() {
        assertThat(configManager.isFeatureEnabled("unknownFeature")).isFalse()
    }
    
    @Test
    fun `test isModuleEnabled with ims`() {
        assertThat(configManager.isModuleEnabled("ims")).isTrue()
        
        val config = configManager.getConfig()
        val newConfig = config.copy(
            modules = config.modules.copy(ims = false)
        )
        configManager.updateConfig(newConfig)
        
        assertThat(configManager.isModuleEnabled("ims")).isFalse()
    }
    
    @Test
    fun `test isModuleEnabled with carrierconfig`() {
        assertThat(configManager.isModuleEnabled("carrierconfig")).isTrue()
        
        val config = configManager.getConfig()
        val newConfig = config.copy(
            modules = config.modules.copy(carrierconfig = false)
        )
        configManager.updateConfig(newConfig)
        
        assertThat(configManager.isModuleEnabled("carrierconfig")).isFalse()
    }
    
    @Test
    fun `test isModuleEnabled with telephony`() {
        assertThat(configManager.isModuleEnabled("telephony")).isTrue()
    }
    
    @Test
    fun `test isModuleEnabled with settings`() {
        assertThat(configManager.isModuleEnabled("settings")).isTrue()
    }
    
    @Test
    fun `test isModuleEnabled with unknown module defaults to true`() {
        assertThat(configManager.isModuleEnabled("unknownModule")).isTrue()
    }
    
    @Test
    fun `test config JSON serialization`() {
        val config = ConfigManager.Config(
            features = ConfigManager.Features(autoBypass = true),
            logging = ConfigManager.Logging(level = "debug"),
            modules = ConfigManager.Modules(ims = true)
        )
        
        val json = gson.toJson(config)
        assertThat(json).contains("\"autoBypass\":true")
        assertThat(json).contains("\"level\":\"debug\"")
        assertThat(json).contains("\"ims\":true")
    }
    
    @Test
    fun `test config JSON deserialization`() {
        val json = """
            {
                "features": {
                    "autoBypass": false,
                    "captureEvents": true,
                    "diagnostics": false
                },
                "logging": {
                    "level": "warn",
                    "console": true,
                    "file": true
                },
                "modules": {
                    "ims": false,
                    "carrierconfig": true,
                    "telephony": false,
                    "settings": true
                }
            }
        """.trimIndent()
        
        val config = gson.fromJson(json, ConfigManager.Config::class.java)
        
        assertThat(config.features.autoBypass).isFalse()
        assertThat(config.features.captureEvents).isTrue()
        assertThat(config.logging.level).isEqualTo("warn")
        assertThat(config.logging.file).isTrue()
        assertThat(config.modules.ims).isFalse()
        assertThat(config.modules.carrierconfig).isTrue()
    }
    
    @Test
    fun `test multiple config updates`() {
        // First update
        var config = configManager.getConfig()
        configManager.updateConfig(
            config.copy(features = config.features.copy(autoBypass = false))
        )
        assertThat(configManager.isFeatureEnabled("autoBypass")).isFalse()
        
        // Second update
        config = configManager.getConfig()
        configManager.updateConfig(
            config.copy(logging = config.logging.copy(level = "debug"))
        )
        assertThat(configManager.getConfig().logging.level).isEqualTo("debug")
        
        // Third update
        config = configManager.getConfig()
        configManager.updateConfig(
            config.copy(modules = config.modules.copy(ims = false))
        )
        assertThat(configManager.isModuleEnabled("ims")).isFalse()
        
        // Verify all updates persisted
        assertThat(configManager.isFeatureEnabled("autoBypass")).isFalse()
        assertThat(configManager.getConfig().logging.level).isEqualTo("debug")
        assertThat(configManager.isModuleEnabled("ims")).isFalse()
    }
}
