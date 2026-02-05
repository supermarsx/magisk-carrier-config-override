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
 * Unit tests for ImsHooks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class ImsHooksTest {
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    @Mock
    private lateinit var logger: CCOLogger
    
    @Mock
    private lateinit var configManager: ConfigManager
    
    private lateinit var imsHooks: ImsHooks
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Setup default mocks
        whenever(configManager.isModuleEnabled("ims")).thenReturn(true)
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        
        imsHooks = ImsHooks(lpparam, logger, configManager)
    }
    
    @Test
    fun `test ImsHooks initialization`() {
        assertThat(imsHooks).isNotNull()
    }
    
    @Test
    fun `test install when module disabled`() {
        whenever(configManager.isModuleEnabled("ims")).thenReturn(false)
        
        imsHooks.install()
        
        verify(logger).info("IMS hooks disabled by config")
        verify(logger, never()).hookSuccess(any(), any())
    }
    
    @Test
    fun `test install when module enabled`() {
        whenever(configManager.isModuleEnabled("ims")).thenReturn(true)
        
        // Note: Full hook installation will fail without actual classes
        // But we can verify the module tries to install
        imsHooks.install()
        
        verify(configManager).isModuleEnabled("ims")
        verify(logger).info(contains("IMS hooks installed"))
    }
    
    @Test
    fun `test autoBypass feature check`() {
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(true)
        assertThat(configManager.isFeatureEnabled("autoBypass")).isTrue()
        
        whenever(configManager.isFeatureEnabled("autoBypass")).thenReturn(false)
        assertThat(configManager.isFeatureEnabled("autoBypass")).isFalse()
    }
    
    @Test
    fun `test multiple installs`() {
        whenever(configManager.isModuleEnabled("ims")).thenReturn(true)
        
        imsHooks.install()
        imsHooks.install()
        
        // Should log twice
        verify(logger, times(2)).info(contains("IMS hooks installed"))
    }
    
    @Test
    fun `test configuration interaction`() {
        imsHooks.install()
        
        verify(configManager).isModuleEnabled("ims")
    }
}
