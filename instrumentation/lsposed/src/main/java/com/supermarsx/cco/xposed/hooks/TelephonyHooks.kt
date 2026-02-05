package com.supermarsx.cco.xposed.hooks

import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Telephony Hooks
 * 
 * Monitors telephony stack for diagnostics
 */
class TelephonyHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    
    private var hooksInstalled = 0
    
    fun install() {
        if (!configManager.isModuleEnabled("telephony")) {
            logger.info("Telephony hooks disabled by config")
            return
        }
        
        hookTelephonyManager()
        hookSubscriptionManager()
        
        logger.info("Telephony hooks installed: $hooksInstalled")
    }
    
    private fun hookTelephonyManager() {
        try {
            val tmClass = XposedHelpers.findClass(
                "android.telephony.TelephonyManager",
                lpparam.classLoader
            )
            
            // getSimState
            hookMethod(tmClass, "getSimState") { param ->
                logger.hookEvent("TelephonyManager.getSimState", param.result)
            }
            
            // getNetworkType
            hookMethod(tmClass, "getNetworkType") { param ->
                logger.hookEvent("TelephonyManager.getNetworkType", param.result)
            }
            
            // getSimOperator
            hookMethod(tmClass, "getSimOperator") { param ->
                logger.hookEvent("TelephonyManager.getSimOperator", param.result)
            }
            
            // getSimOperatorName
            hookMethod(tmClass, "getSimOperatorName") { param ->
                logger.hookEvent("TelephonyManager.getSimOperatorName", param.result)
            }
            
            // isVolteAvailable (Samsung specific)
            try {
                hookMethod(tmClass, "isVolteAvailable") { param ->
                    val original = param.result as? Boolean ?: false
                    if (configManager.isFeatureEnabled("autoBypass") && !original) {
                        param.result = true
                        logger.hookEvent("TelephonyManager.isVolteAvailable", true, forced = true)
                    }
                }
            } catch (e: Throwable) {
                logger.debug("isVolteAvailable not available")
            }
            
        } catch (e: Throwable) {
            logger.error("Failed to hook TelephonyManager", e)
        }
    }
    
    private fun hookSubscriptionManager() {
        try {
            val smClass = XposedHelpers.findClass(
                "android.telephony.SubscriptionManager",
                lpparam.classLoader
            )
            
            // getActiveSubscriptionInfoList
            hookMethod(smClass, "getActiveSubscriptionInfoList") { param ->
                val result = param.result
                if (result != null) {
                    logger.hookEvent("SubscriptionManager.getActiveSubscriptionInfoList", "list")
                }
            }
            
            // getDefaultSubscriptionId
            hookMethod(smClass, "getDefaultSubscriptionId") { param ->
                logger.hookEvent("SubscriptionManager.getDefaultSubscriptionId", param.result)
            }
            
        } catch (e: Throwable) {
            logger.error("Failed to hook SubscriptionManager", e)
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
