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
 * Unit tests for SamsungImsHooks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class SamsungImsHooksTest {
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    @Mock
    private lateinit var logger: CCOLogger
    
    @Mock
    private lateinit var configManager: ConfigManager
    
    private lateinit var samsungImsHooks: SamsungImsHooks
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(configManager.isModuleEnabled("ims")).thenReturn(true)
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        
        samsungImsHooks = SamsungImsHooks(lpparam, logger, configManager)
    }
    
    @Test
    fun `test SamsungImsHooks initialization`() {
        assertThat(samsungImsHooks).isNotNull()
    }
    
    @Test
    fun `test install when module disabled`() {
        whenever(configManager.isModuleEnabled("ims")).thenReturn(false)
        
        samsungImsHooks.install()
        
        verify(logger).info("Samsung IMS hooks disabled by config")
        verify(logger, never()).hookSuccess(any(), any())
    }
    
    @Test
    fun `test install when module enabled`() {
        whenever(configManager.isModuleEnabled("ims")).thenReturn(true)
        
        samsungImsHooks.install()
        
        verify(configManager).isModuleEnabled("ims")
        verify(logger).info(contains("Samsung IMS hooks installed"))
    }
    
    @Test
    fun `test configuration interaction`() {
        samsungImsHooks.install()
        
        verify(configManager).isModuleEnabled("ims")
    }
}
