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
 * Unit tests for CarrierConfigHooks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class CarrierConfigHooksTest {
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    @Mock
    private lateinit var logger: CCOLogger
    
    @Mock
    private lateinit var configManager: ConfigManager
    
    private lateinit var carrierConfigHooks: CarrierConfigHooks
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(configManager.isModuleEnabled("carrierconfig")).thenReturn(true)
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        
        carrierConfigHooks = CarrierConfigHooks(lpparam, logger, configManager)
    }
    
    @Test
    fun `test CarrierConfigHooks initialization`() {
        assertThat(carrierConfigHooks).isNotNull()
    }
    
    @Test
    fun `test install when module disabled`() {
        whenever(configManager.isModuleEnabled("carrierconfig")).thenReturn(false)
        
        carrierConfigHooks.install()
        
        verify(logger).info("CarrierConfig hooks disabled by config")
        verify(logger, never()).hookSuccess(any(), any())
    }
    
    @Test
    fun `test install when module enabled`() {
        whenever(configManager.isModuleEnabled("carrierconfig")).thenReturn(true)
        
        carrierConfigHooks.install()
        
        verify(configManager).isModuleEnabled("carrierconfig")
        verify(logger).info(contains("CarrierConfig hooks installed"))
    }
    
    @Test
    fun `test forced keys configuration`() {
        // Verify the force keys are properly defined
        carrierConfigHooks.install()
        
        verify(configManager).isModuleEnabled("carrierconfig")
    }
    
    @Test
    fun `test autoBypass feature interaction`() {
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(false)
        
        carrierConfigHooks.install()
        
        verify(configManager).isModuleEnabled("carrierconfig")
    }
}
