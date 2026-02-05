package com.supermarsx.cco.xposed.hooks

import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
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
 * Unit tests for SettingsHooks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class SettingsHooksTest {
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    @Mock
    private lateinit var logger: CCOLogger
    
    @Mock
    private lateinit var configManager: ConfigManager
    
    private lateinit var settingsHooks: SettingsHooks
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(configManager.isModuleEnabled("settings")).thenReturn(true)
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        
        settingsHooks = SettingsHooks(lpparam, logger, configManager)
    }
    
    @Test
    fun `test SettingsHooks initialization`() {
        assertThat(settingsHooks).isNotNull()
    }
    
    @Test
    fun `test install when module disabled`() {
        whenever(configManager.isModuleEnabled("settings")).thenReturn(false)
        
        settingsHooks.install()
        
        verify(logger).info("Settings hooks disabled by config")
        verify(logger, never()).hookSuccess(any(), any())
    }
    
    @Test
    fun `test install when module enabled`() {
        whenever(configManager.isModuleEnabled("settings")).thenReturn(true)
        
        settingsHooks.install()
        
        verify(configManager).isModuleEnabled("settings")
        verify(logger).info(contains("Settings hooks installed"))
    }
    
    @Test
    fun `test forced settings configuration`() {
        settingsHooks.install()
        
        verify(configManager).isModuleEnabled("settings")
    }
}
