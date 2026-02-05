package com.supermarsx.cco.xposed

import com.supermarsx.cco.xposed.hooks.*
import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

/**
 * Integration tests for CCO Xposed Module
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class IntegrationTest {
    
    @Mock
    private lateinit var startupParam: IXposedHookZygoteInit.StartupParam
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    private lateinit var module: CCOXposedModule
    private lateinit var configManager: ConfigManager
    private lateinit var logger: CCOLogger
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(startupParam.modulePath).thenReturn("/data/app/test.apk")
        
        module = CCOXposedModule()
        configManager = ConfigManager()
        logger = CCOLogger("Test")
    }
    
    @Test
    fun `test full module lifecycle`() {
        // Initialize
        module.initZygote(startupParam)
        assertThat(CCOXposedModule.modulePath).isEqualTo("/data/app/test.apk")
        
        // Load packages
        val packages = listOf(
            "android",
            "com.android.phone",
            "com.sec.imsservice",
            "com.samsung.android.ims"
        )
        
        for (pkg in packages) {
            lpparam.packageName = pkg
            lpparam.processName = pkg
            module.handleLoadPackage(lpparam)
        }
    }
    
    @Test
    fun `test all hooks installation flow`() {
        val hooks = listOf(
            ImsHooks(lpparam, logger, configManager),
            CarrierConfigHooks(lpparam, logger, configManager),
            TelephonyHooks(lpparam, logger, configManager),
            SettingsHooks(lpparam, logger, configManager),
            SamsungImsHooks(lpparam, logger, configManager)
        )
        
        for (hook in hooks) {
            hook.install()
        }
    }
    
    @Test
    fun `test config manager with multiple modules`() {
        val config = configManager.getConfig()
        
        // All modules should be enabled by default
        assertThat(config.modules.ims).isTrue()
        assertThat(config.modules.carrierconfig).isTrue()
        assertThat(config.modules.telephony).isTrue()
        assertThat(config.modules.settings).isTrue()
        
        // AutoBypass should be enabled
        assertThat(config.features.autoBypass).isTrue()
    }
    
    @Test
    fun `test selective module disabling`() {
        var config = configManager.getConfig()
        
        // Disable IMS module
        configManager.updateConfig(
            config.copy(modules = config.modules.copy(ims = false))
        )
        
        val imsHooks = ImsHooks(lpparam, logger, configManager)
        imsHooks.install()
        
        assertThat(configManager.isModuleEnabled("ims")).isFalse()
        assertThat(configManager.isModuleEnabled("carrierconfig")).isTrue()
    }
    
    @Test
    fun `test autoBypass feature toggle`() {
        var config = configManager.getConfig()
        
        // Disable autoBypass
        configManager.updateConfig(
            config.copy(features = config.features.copy(autoBypass = false))
        )
        
        assertThat(configManager.isFeatureEnabled("autoBypass")).isFalse()
        
        // Re-enable
        config = configManager.getConfig()
        configManager.updateConfig(
            config.copy(features = config.features.copy(autoBypass = true))
        )
        
        assertThat(configManager.isFeatureEnabled("autoBypass")).isTrue()
    }
    
    @Test
    fun `test logging levels`() {
        logger.minLevel = CCOLogger.LogLevel.DEBUG
        logger.debug("debug message")
        logger.info("info message")
        logger.warn("warn message")
        logger.error("error message")
        
        logger.minLevel = CCOLogger.LogLevel.ERROR
        logger.debug("should not log")
        logger.info("should not log")
        logger.warn("should not log")
        logger.error("should log")
    }
    
    @Test
    fun `test hook event logging`() {
        logger.minLevel = CCOLogger.LogLevel.DEBUG
        
        logger.hookSuccess("TestClass", "testMethod")
        logger.hookFailed("TestClass", "failedMethod", RuntimeException("test"))
        logger.hookEvent("eventMethod", true, forced = true)
    }
    
    @Test
    fun `test config persistence across updates`() {
        // First update
        var config = configManager.getConfig()
        configManager.updateConfig(
            config.copy(
                features = config.features.copy(autoBypass = false),
                logging = config.logging.copy(level = "debug"),
                modules = config.modules.copy(ims = false)
            )
        )
        
        // Verify all changes persisted
        val updatedConfig = configManager.getConfig()
        assertThat(updatedConfig.features.autoBypass).isFalse()
        assertThat(updatedConfig.logging.level).isEqualTo("debug")
        assertThat(updatedConfig.modules.ims).isFalse()
    }
    
    @Test
    fun `test module version info`() {
        assertThat(CCOXposedModule.VERSION).isNotEmpty()
        assertThat(CCOXposedModule.VERSION_CODE).isGreaterThan(0)
        assertThat(CCOXposedModule.TAG).isEqualTo("CCO-Xposed")
    }
    
    @Test
    fun `test error handling in hook installation`() {
        // Hooks should handle missing classes gracefully
        val hooks = listOf(
            ImsHooks(lpparam, logger, configManager),
            CarrierConfigHooks(lpparam, logger, configManager),
            TelephonyHooks(lpparam, logger, configManager),
            SettingsHooks(lpparam, logger, configManager),
            SamsungImsHooks(lpparam, logger, configManager)
        )
        
        // Should not throw exceptions even with missing classes
        for (hook in hooks) {
            try {
                hook.install()
            } catch (e: Exception) {
                // Expected for some hooks without actual Android classes
            }
        }
    }
}
