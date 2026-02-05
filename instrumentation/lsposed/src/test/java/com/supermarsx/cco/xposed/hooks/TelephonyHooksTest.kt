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
 * Unit tests for TelephonyHooks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class TelephonyHooksTest {
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    @Mock
    private lateinit var logger: CCOLogger
    
    @Mock
    private lateinit var configManager: ConfigManager
    
    private lateinit var telephonyHooks: TelephonyHooks
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(configManager.isModuleEnabled("telephony")).thenReturn(true)
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        
        telephonyHooks = TelephonyHooks(lpparam, logger, configManager)
    }
    
    @Test
    fun `test TelephonyHooks initialization`() {
        assertThat(telephonyHooks).isNotNull()
    }
    
    @Test
    fun `test install when module disabled`() {
        whenever(configManager.isModuleEnabled("telephony")).thenReturn(false)
        
        telephonyHooks.install()
        
        verify(logger).info("Telephony hooks disabled by config")
        verify(logger, never()).hookSuccess(any(), any())
    }
    
    @Test
    fun `test install when module enabled`() {
        whenever(configManager.isModuleEnabled("telephony")).thenReturn(true)
        
        telephonyHooks.install()
        
        verify(configManager).isModuleEnabled("telephony")
        verify(logger).info(contains("Telephony hooks installed"))
    }
    
    @Test
    fun `test configuration interaction`() {
        telephonyHooks.install()
        
        verify(configManager).isModuleEnabled("telephony")
    }
}
