package com.supermarsx.cco.xposed.hooks

import android.os.PersistableBundle
import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * CarrierConfig Hooks
 * 
 * Intercepts and modifies CarrierConfig at runtime
 */
class CarrierConfigHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    
    private var hooksInstalled = 0
    
    // Keys to force
    private val forceKeys = mapOf(
        "carrier_wfc_ims_available_bool" to true,
        "editable_wfc_mode_bool" to true,
        "carrier_default_wfc_ims_enabled_bool" to true,
        "carrier_default_wfc_ims_roaming_enabled_bool" to true,
        "carrier_promote_wfc_on_call_fail_bool" to true,
        "use_wfc_home_network_mode_in_roaming_network_bool" to false,
        "wfc_carrier_name_override_by_pnn_bool" to false
    )
    
    fun install() {
        if (!configManager.isModuleEnabled("carrierconfig")) {
            logger.info("CarrierConfig hooks disabled by config")
            return
        }
        
        hookCarrierConfigManager()
        hookPersistableBundle()
        
        logger.info("CarrierConfig hooks installed: $hooksInstalled")
    }
    
    private fun hookCarrierConfigManager() {
        try {
            val ccmClass = XposedHelpers.findClass(
                "android.telephony.CarrierConfigManager",
                lpparam.classLoader
            )
            
            // getConfigForSubId
            XposedHelpers.findAndHookMethod(
                ccmClass,
                "getConfigForSubId",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val bundle = param.result as? PersistableBundle
                            if (bundle != null && configManager.isFeatureEnabled("autoBypass")) {
                                modifyBundle(bundle)
                                logger.hookEvent("CarrierConfigManager.getConfigForSubId", "modified")
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed to modify config bundle", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("CarrierConfigManager", "getConfigForSubId")
            
            // getConfig
            XposedHelpers.findAndHookMethod(
                ccmClass,
                "getConfig",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val bundle = param.result as? PersistableBundle
                            if (bundle != null && configManager.isFeatureEnabled("autoBypass")) {
                                modifyBundle(bundle)
                                logger.hookEvent("CarrierConfigManager.getConfig", "modified")
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed to modify config bundle", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("CarrierConfigManager", "getConfig")
            
        } catch (e: Throwable) {
            logger.error("Failed to hook CarrierConfigManager", e)
        }
    }
    
    private fun hookPersistableBundle() {
        try {
            val bundleClass = XposedHelpers.findClass(
                "android.os.PersistableBundle",
                lpparam.classLoader
            )
            
            // getBoolean with default
            XposedHelpers.findAndHookMethod(
                bundleClass,
                "getBoolean",
                String::class.java,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val key = param.args[0] as? String
                            if (key != null && forceKeys.containsKey(key)) {
                                val original = param.result as? Boolean ?: false
                                val forced = forceKeys[key] as Boolean
                                
                                if (original != forced && configManager.isFeatureEnabled("autoBypass")) {
                                    param.result = forced
                                    logger.hookEvent("PersistableBundle.getBoolean($key)", forced, forced = true)
                                }
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed in PersistableBundle.getBoolean", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("PersistableBundle", "getBoolean")
            
        } catch (e: Throwable) {
            logger.error("Failed to hook PersistableBundle", e)
        }
    }
    
    private fun modifyBundle(bundle: PersistableBundle) {
        try {
            for ((key, value) in forceKeys) {
                when (value) {
                    is Boolean -> {
                        val current = bundle.getBoolean(key, !value)
                        if (current != value) {
                            bundle.putBoolean(key, value)
                        }
                    }
                    is Int -> bundle.putInt(key, value)
                    is String -> bundle.putString(key, value)
                }
            }
        } catch (e: Throwable) {
            logger.error("Failed to modify bundle", e)
        }
    }
}
