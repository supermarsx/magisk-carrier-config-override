package com.supermarsx.cco.xposed.hooks

import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Samsung IMS Framework Hooks
 * 
 * Samsung-specific IMS classes
 */
class SamsungImsHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    
    private var hooksInstalled = 0
    
    fun install() {
        if (!configManager.isModuleEnabled("ims")) {
            logger.info("Samsung IMS hooks disabled by config")
            return
        }
        
        hookSamsungImsManager()
        hookSamsungVoWiFi()
        
        logger.info("Samsung IMS hooks installed: $hooksInstalled")
    }
    
    private fun hookSamsungImsManager() {
        try {
            // Samsung ImsManager
            val imsManagerClass = XposedHelpers.findClass(
                "com.sec.ims.ImsManager",
                lpparam.classLoader
            )
            
            // isWfcEntitled
            hookMethod(imsManagerClass, "isWfcEntitled") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isWfcEntitled", true, forced = true)
                }
            }
            
            // isVolteProvisioned
            hookMethod(imsManagerClass, "isVolteProvisioned") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isVolteProvisioned", true, forced = true)
                }
            }
            
            // isWfcEnabled
            hookMethod(imsManagerClass, "isWfcEnabled") { param ->
                val original = param.result as? Boolean ?: false
                if (configManager.isFeatureEnabled("autoBypass") && !original) {
                    param.result = true
                    logger.hookEvent("ImsManager.isWfcEnabled", true, forced = true)
                }
            }
            
        } catch (e: Throwable) {
            logger.error("Failed to hook Samsung ImsManager", e)
        }
    }
    
    private fun hookSamsungVoWiFi() {
        try {
            // VoWiFi specific classes
            val classesToTry = listOf(
                "com.sec.ims.settings.ImsSettings",
                "com.sec.ims.ImsSettings"
            )
            
            for (className in classesToTry) {
                try {
                    val settingsClass = XposedHelpers.findClass(className, lpparam.classLoader)
                    
                    // getBoolean
                    XposedHelpers.findAndHookMethod(
                        settingsClass,
                        "getBoolean",
                        String::class.java,
                        Boolean::class.javaPrimitiveType,
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                try {
                                    val key = param.args[0] as? String
                                    if (key != null && isWfcRelatedKey(key)) {
                                        val original = param.result as? Boolean ?: false
                                        if (configManager.isFeatureEnabled("autoBypass") && !original) {
                                            param.result = true
                                            logger.hookEvent("ImsSettings.getBoolean($key)", true, forced = true)
                                        }
                                    }
                                } catch (e: Throwable) {
                                    logger.error("Failed in ImsSettings.getBoolean", e)
                                }
                            }
                        }
                    )
                    hooksInstalled++
                    logger.hookSuccess(settingsClass.simpleName, "getBoolean")
                    break
                } catch (e: Throwable) {
                    // Try next class
                }
            }
            
        } catch (e: Throwable) {
            logger.error("Failed to hook Samsung VoWiFi settings", e)
        }
    }
    
    private fun isWfcRelatedKey(key: String): Boolean {
        val wfcKeys = listOf("wfc", "vowifi", "wifi_call", "volte", "ims")
        return wfcKeys.any { key.contains(it, ignoreCase = true) }
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
