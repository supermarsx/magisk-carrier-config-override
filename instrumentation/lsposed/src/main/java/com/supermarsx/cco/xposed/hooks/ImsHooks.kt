package com.supermarsx.cco.xposed.hooks

import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * IMS Service Hooks
 * 
 * Hooks Samsung IMS stack for entitlement bypass
 */
class ImsHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    
    private var hooksInstalled = 0
    
    fun install() {
        if (!configManager.isModuleEnabled("ims")) {
            logger.info("IMS hooks disabled by config")
            return
        }
        
        // ImsManager hooks
        hookImsManager()
        
        // ImsFeature hooks
        hookImsFeature()
        
        // ImsRegistry hooks
        hookImsRegistry()
        
        // VoWiFiManager hooks
        hookVoWiFiManager()
        
        // ImsSettings hooks
        hookImsSettings()
        
        // EntitlementManager hooks
        hookEntitlementManager()
        
        logger.info("IMS hooks installed: $hooksInstalled")
    }
    
    private fun hookImsManager() {
        try {
            val imsManagerClass = XposedHelpers.findClass(
                "android.telephony.ims.ImsManager",
                lpparam.classLoader
            )
            
            // isWfcEnabledByUser
            hookMethod(imsManagerClass, "isWfcEnabledByUser") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isWfcEnabledByUser", true, forced = true)
                }
            }
            
            // isVtEnabledByUser
            hookMethod(imsManagerClass, "isVtEnabledByUser") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isVtEnabledByUser", true, forced = true)
                }
            }
            
            // isEnhanced4gLteModeSettingEnabledByUser
            hookMethod(imsManagerClass, "isEnhanced4gLteModeSettingEnabledByUser") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isEnhanced4gLteModeSettingEnabledByUser", true, forced = true)
                }
            }
            
        } catch (e: Throwable) {
            logger.error("Failed to hook ImsManager", e)
        }
    }
    
    private fun hookImsFeature() {
        try {
            // Try Samsung IMS feature classes
            val classNames = listOf(
                "com.sec.ims.ImsFeature",
                "android.telephony.ims.feature.ImsFeature"
            )
            
            for (className in classNames) {
                try {
                    val imsFeatureClass = XposedHelpers.findClass(className, lpparam.classLoader)
                    
                    // isVowifiEnabled
                    hookMethod(imsFeatureClass, "isVowifiEnabled") { param ->
                        val original = param.result as? Boolean ?: false
                        if (configManager.isFeatureEnabled("autoBypass") && !original) {
                            param.result = true
                            logger.hookEvent("ImsFeature.isVowifiEnabled", true, forced = true)
                        }
                    }
                    
                    // isVolteEnabled
                    hookMethod(imsFeatureClass, "isVolteEnabled") { param ->
                        val original = param.result as? Boolean ?: false
                        if (configManager.isFeatureEnabled("autoBypass") && !original) {
                            param.result = true
                            logger.hookEvent("ImsFeature.isVolteEnabled", true, forced = true)
                        }
                    }
                    
                    break // Success, stop trying
                } catch (e: Throwable) {
                    // Try next class
                }
            }
        } catch (e: Throwable) {
            logger.error("Failed to hook ImsFeature", e)
        }
    }
    
    private fun hookImsRegistry() {
        try {
            val imsRegistryClass = XposedHelpers.findClass(
                "com.sec.ims.ImsRegistry",
                lpparam.classLoader
            )
            
            // isRegistered
            hookMethod(imsRegistryClass, "isRegistered") { param ->
                logger.hookEvent("ImsRegistry.isRegistered", param.result)
            }
            
        } catch (e: Throwable) {
            logger.debug("ImsRegistry not available")
        }
    }
    
    private fun hookVoWiFiManager() {
        try {
            val vowifiManagerClass = XposedHelpers.findClass(
                "com.sec.ims.vowifi.VoWiFiManager",
                lpparam.classLoader
            )
            
            // getVoWiFiMode
            hookMethod(vowifiManagerClass, "getVoWiFiMode") { param ->
                logger.hookEvent("VoWiFiManager.getVoWiFiMode", param.result)
            }
            
            // isVoWiFiEnabled
            hookMethod(vowifiManagerClass, "isVoWiFiEnabled") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("VoWiFiManager.isVoWiFiEnabled", true, forced = true)
                }
            }
            
        } catch (e: Throwable) {
            logger.debug("VoWiFiManager not available")
        }
    }
    
    private fun hookImsSettings() {
        try {
            val imsSettingsClass = XposedHelpers.findClass(
                "com.sec.ims.settings.ImsSettings",
                lpparam.classLoader
            )
            
            // getBoolean
            hookMethod(imsSettingsClass, "getBoolean", String::class.java, Boolean::class.javaPrimitiveType) { param ->
                val key = param.args[0] as? String
                val original = param.result as? Boolean ?: false
                
                // Force WFC-related keys
                if (key != null && (key.contains("wfc", ignoreCase = true) || 
                                   key.contains("vowifi", ignoreCase = true))) {
                    if (configManager.isFeatureEnabled("autoBypass") && !original) {
                        param.result = true
                        logger.hookEvent("ImsSettings.getBoolean($key)", true, forced = true)
                    }
                }
            }
            
        } catch (e: Throwable) {
            logger.debug("ImsSettings not available")
        }
    }
    
    private fun hookEntitlementManager() {
        try {
            val entitlementManagerClass = XposedHelpers.findClass(
                "com.sec.ims.entitlement.EntitlementManager",
                lpparam.classLoader
            )
            
            // hasEntitlement
            hookMethod(entitlementManagerClass, "hasEntitlement") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("EntitlementManager.hasEntitlement", true, forced = true)
                }
            }
            
        } catch (e: Throwable) {
            logger.debug("EntitlementManager not available")
        }
    }
    
    private fun hookMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypes: Class<*>,
        afterHook: (XC_MethodHook.MethodHookParam) -> Unit
    ) {
        try {
            XposedHelpers.findAndHookMethod(
                clazz,
                methodName,
                *parameterTypes,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            afterHook(param)
                        } catch (e: Throwable) {
                            logger.error("Hook execution failed: ${clazz.simpleName}.$methodName", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess(clazz.simpleName, methodName)
        } catch (e: Throwable) {
            logger.hookFailed(clazz.simpleName, methodName, e)
        }
    }
}
