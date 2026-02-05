package com.supermarsx.cco.xposed.hooks

import com.supermarsx.cco.xposed.utils.CCOLogger
import com.supermarsx.cco.xposed.utils.ConfigManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Settings Hooks
 * 
 * Intercepts Android Settings for WFC/VoLTE
 */
class SettingsHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    
    private var hooksInstalled = 0
    
    private val forceSettings = mapOf(
        "wfc_ims_enabled" to 1,
        "wfc_ims_mode" to 2,
        "wfc_ims_roaming_enabled" to 1,
        "wfc_ims_roaming_mode" to 2,
        "volte_vt_enabled" to 1,
        "lte_service_forced" to 1
    )
    
    fun install() {
        if (!configManager.isModuleEnabled("settings")) {
            logger.info("Settings hooks disabled by config")
            return
        }
        
        hookSettingsGlobal()
        hookSettingsSecure()
        hookSettingsSystem()
        
        logger.info("Settings hooks installed: $hooksInstalled")
    }
    
    private fun hookSettingsGlobal() {
        try {
            val globalClass = XposedHelpers.findClass(
                "android.provider.Settings\$Global",
                lpparam.classLoader
            )
            
            // getInt with default
            XposedHelpers.findAndHookMethod(
                globalClass,
                "getInt",
                android.content.ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val key = param.args[1] as? String
                            if (key != null && forceSettings.containsKey(key)) {
                                val original = param.result as? Int ?: 0
                                val forced = forceSettings[key] as Int
                                
                                if (original != forced && configManager.isFeatureEnabled("autoBypass")) {
                                    param.result = forced
                                    logger.hookEvent("Settings.Global.getInt($key)", forced, forced = true)
                                }
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed in Settings.Global.getInt", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("Settings.Global", "getInt")
            
            // getString
            XposedHelpers.findAndHookMethod(
                globalClass,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val key = param.args[1] as? String
                            if (key != null && (key.contains("wfc") || key.contains("vowifi") || key.contains("volte"))) {
                                logger.hookEvent("Settings.Global.getString($key)", param.result)
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed in Settings.Global.getString", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("Settings.Global", "getString")
            
        } catch (e: Throwable) {
            logger.error("Failed to hook Settings.Global", e)
        }
    }
    
    private fun hookSettingsSecure() {
        try {
            val secureClass = XposedHelpers.findClass(
                "android.provider.Settings\$Secure",
                lpparam.classLoader
            )
            
            // getInt with default
            XposedHelpers.findAndHookMethod(
                secureClass,
                "getInt",
                android.content.ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val key = param.args[1] as? String
                            if (key != null && forceSettings.containsKey(key)) {
                                val original = param.result as? Int ?: 0
                                val forced = forceSettings[key] as Int
                                
                                if (original != forced && configManager.isFeatureEnabled("autoBypass")) {
                                    param.result = forced
                                    logger.hookEvent("Settings.Secure.getInt($key)", forced, forced = true)
                                }
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed in Settings.Secure.getInt", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("Settings.Secure", "getInt")
            
        } catch (e: Throwable) {
            logger.error("Failed to hook Settings.Secure", e)
        }
    }
    
    private fun hookSettingsSystem() {
        try {
            val systemClass = XposedHelpers.findClass(
                "android.provider.Settings\$System",
                lpparam.classLoader
            )
            
            // getInt with default
            XposedHelpers.findAndHookMethod(
                systemClass,
                "getInt",
                android.content.ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val key = param.args[1] as? String
                            if (key != null && forceSettings.containsKey(key)) {
                                val original = param.result as? Int ?: 0
                                val forced = forceSettings[key] as Int
                                
                                if (original != forced && configManager.isFeatureEnabled("autoBypass")) {
                                    param.result = forced
                                    logger.hookEvent("Settings.System.getInt($key)", forced, forced = true)
                                }
                            }
                        } catch (e: Throwable) {
                            logger.error("Failed in Settings.System.getInt", e)
                        }
                    }
                }
            )
            hooksInstalled++
            logger.hookSuccess("Settings.System", "getInt")
            
        } catch (e: Throwable) {
            logger.error("Failed to hook Settings.System", e)
        }
    }
}
