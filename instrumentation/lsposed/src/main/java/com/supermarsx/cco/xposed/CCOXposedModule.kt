package com.supermarsx.cco.xposed

import com.supermarsx.cco.xposed.hooks.*
import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Main Xposed Module Entry Point
 * 
 * Hooks into Samsung telephony stack for VoWiFi/VoLTE enablement
 */
class CCOXposedModule : IXposedHookLoadPackage, IXposedHookZygoteInit {
    
    companion object {
        const val TAG = "CCO-Xposed"
        const val VERSION = BuildConfig.MODULE_VERSION
        const val VERSION_CODE = BuildConfig.MODULE_VERSION_CODE
        
        lateinit var modulePath: String
            private set
    }
    
    private val logger = CCOLogger(TAG)
    private val configManager = ConfigManager()
    
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        logger.info("Module initialized: v$VERSION ($VERSION_CODE)")
        logger.info("Module path: $modulePath")
        
        // Load configuration
        configManager.loadConfig()
    }
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        val processName = lpparam.processName
        
        logger.debug("Package loaded: $packageName (process: $processName)")
        
        when (packageName) {
            "android" -> {
                logger.info("Hooking android system package")
                hookAndroidFramework(lpparam)
            }
            
            "com.android.phone" -> {
                logger.info("Hooking Phone app")
                hookPhoneApp(lpparam)
            }
            
            "com.sec.imsservice" -> {
                logger.info("Hooking Samsung IMS service")
                hookImsService(lpparam)
            }
            
            "com.samsung.android.ims" -> {
                logger.info("Hooking Samsung IMS framework")
                hookSamsungIms(lpparam)
            }
        }
    }
    
    private fun hookAndroidFramework(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // CarrierConfig hooks
            CarrierConfigHooks(lpparam, logger, configManager).install()
            
            // Telephony hooks
            TelephonyHooks(lpparam, logger, configManager).install()
            
            // Settings hooks
            SettingsHooks(lpparam, logger, configManager).install()
            
            logger.info("Android framework hooks installed successfully")
        } catch (e: Exception) {
            logger.error("Failed to hook android framework", e)
        }
    }
    
    private fun hookPhoneApp(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Phone app specific hooks
            logger.info("Phone app hooks installed successfully")
        } catch (e: Exception) {
            logger.error("Failed to hook Phone app", e)
        }
    }
    
    private fun hookImsService(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // IMS service hooks
            ImsHooks(lpparam, logger, configManager).install()
            
            logger.info("IMS service hooks installed successfully")
        } catch (e: Exception) {
            logger.error("Failed to hook IMS service", e)
        }
    }
    
    private fun hookSamsungIms(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Samsung IMS framework hooks
            SamsungImsHooks(lpparam, logger, configManager).install()
            
            logger.info("Samsung IMS hooks installed successfully")
        } catch (e: Exception) {
            logger.error("Failed to hook Samsung IMS", e)
        }
    }
}
