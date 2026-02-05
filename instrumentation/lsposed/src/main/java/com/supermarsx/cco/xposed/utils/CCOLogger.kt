package com.supermarsx.cco.xposed.utils

import android.util.Log
import de.robv.android.xposed.XposedBridge

/**
 * Centralized logging utility for CCO Xposed module
 */
class CCOLogger(private val tag: String) {
    
    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    var minLevel: LogLevel = LogLevel.INFO
    
    fun debug(message: String) {
        if (minLevel.ordinal <= LogLevel.DEBUG.ordinal) {
            log(LogLevel.DEBUG, message)
        }
    }
    
    fun info(message: String) {
        if (minLevel.ordinal <= LogLevel.INFO.ordinal) {
            log(LogLevel.INFO, message)
        }
    }
    
    fun warn(message: String) {
        if (minLevel.ordinal <= LogLevel.WARN.ordinal) {
            log(LogLevel.WARN, message)
        }
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, message, throwable)
    }
    
    fun hookSuccess(className: String, methodName: String) {
        info("✓ Hooked: $className.$methodName")
    }
    
    fun hookFailed(className: String, methodName: String, throwable: Throwable) {
        error("✗ Hook failed: $className.$methodName", throwable)
    }
    
    fun hookEvent(methodName: String, result: Any?, forced: Boolean = false) {
        val forcedTag = if (forced) " [FORCED]" else ""
        debug("→ $methodName: $result$forcedTag")
    }
    
    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        val fullMessage = "[$tag] $message"
        
        // Log to Xposed
        when (level) {
            LogLevel.DEBUG -> XposedBridge.log("D: $fullMessage")
            LogLevel.INFO -> XposedBridge.log("I: $fullMessage")
            LogLevel.WARN -> XposedBridge.log("W: $fullMessage")
            LogLevel.ERROR -> {
                XposedBridge.log("E: $fullMessage")
                throwable?.let { XposedBridge.log(it) }
            }
        }
        
        // Also log to Android logcat
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }
}
