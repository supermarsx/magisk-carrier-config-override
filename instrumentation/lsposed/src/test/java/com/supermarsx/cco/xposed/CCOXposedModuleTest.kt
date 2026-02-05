package com.supermarsx.cco.xposed

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
 * Unit tests for CCOXposedModule
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class CCOXposedModuleTest {
    
    @Mock
    private lateinit var startupParam: IXposedHookZygoteInit.StartupParam
    
    @Mock
    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam
    
    private lateinit var module: CCOXposedModule
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(startupParam.modulePath).thenReturn("/data/app/test.apk")
        
        module = CCOXposedModule()
    }
    
    @Test
    fun `test module initialization`() {
        assertThat(module).isNotNull()
    }
    
    @Test
    fun `test initZygote sets module path`() {
        module.initZygote(startupParam)
        
        assertThat(CCOXposedModule.modulePath).isEqualTo("/data/app/test.apk")
    }
    
    @Test
    fun `test module version constant`() {
        assertThat(CCOXposedModule.VERSION).isEqualTo("1.0.0")
    }
    
    @Test
    fun `test module version code constant`() {
        assertThat(CCOXposedModule.VERSION_CODE).isEqualTo(1)
    }
    
    @Test
    fun `test module tag constant`() {
        assertThat(CCOXposedModule.TAG).isEqualTo("CCO-Xposed")
    }
    
    @Test
    fun `test handleLoadPackage with android package`() {
        lpparam.packageName = "android"
        lpparam.processName = "android"
        
        // Should not throw exception
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test handleLoadPackage with Phone app`() {
        lpparam.packageName = "com.android.phone"
        lpparam.processName = "com.android.phone"
        
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test handleLoadPackage with IMS service`() {
        lpparam.packageName = "com.sec.imsservice"
        lpparam.processName = "com.sec.imsservice"
        
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test handleLoadPackage with Samsung IMS`() {
        lpparam.packageName = "com.samsung.android.ims"
        lpparam.processName = "com.samsung.android.ims"
        
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test handleLoadPackage with unknown package`() {
        lpparam.packageName = "com.example.unknown"
        lpparam.processName = "com.example.unknown"
        
        // Should handle gracefully
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test handleLoadPackage with null classLoader`() {
        lpparam.packageName = "android"
        lpparam.processName = "android"
        lpparam.classLoader = null
        
        // Should not throw exception
        module.handleLoadPackage(lpparam)
    }
    
    @Test
    fun `test multiple package loads`() {
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
}
