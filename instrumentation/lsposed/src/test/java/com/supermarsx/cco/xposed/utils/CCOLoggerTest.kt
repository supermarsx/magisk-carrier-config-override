package com.supermarsx.cco.xposed.utils

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import com.google.common.truth.Truth.assertThat

/**
 * Unit tests for CCOLogger
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class CCOLoggerTest {
    
    private lateinit var logger: CCOLogger
    private val testTag = "TestTag"
    
    @Before
    fun setup() {
        ShadowLog.stream = System.out
        logger = CCOLogger(testTag)
    }
    
    @Test
    fun `test logger initialization`() {
        assertThat(logger).isNotNull()
        assertThat(logger.minLevel).isEqualTo(CCOLogger.LogLevel.INFO)
    }
    
    @Test
    fun `test debug logging respects minLevel`() {
        logger.minLevel = CCOLogger.LogLevel.INFO
        logger.debug("debug message")
        
        val logs = ShadowLog.getLogs()
        val debugLogs = logs.filter { it.msg.contains("debug message") }
        assertThat(debugLogs).isEmpty()
    }
    
    @Test
    fun `test debug logging when enabled`() {
        logger.minLevel = CCOLogger.LogLevel.DEBUG
        logger.debug("debug message")
        
        val logs = ShadowLog.getLogs()
        val debugLogs = logs.filter { it.msg.contains("debug message") }
        assertThat(debugLogs).isNotEmpty()
    }
    
    @Test
    fun `test info logging`() {
        logger.info("info message")
        
        val logs = ShadowLog.getLogs()
        val infoLogs = logs.filter { it.msg.contains("info message") }
        assertThat(infoLogs).isNotEmpty()
    }
    
    @Test
    fun `test warn logging`() {
        logger.warn("warning message")
        
        val logs = ShadowLog.getLogs()
        val warnLogs = logs.filter { it.msg.contains("warning message") }
        assertThat(warnLogs).isNotEmpty()
    }
    
    @Test
    fun `test error logging without exception`() {
        logger.error("error message")
        
        val logs = ShadowLog.getLogs()
        val errorLogs = logs.filter { it.msg.contains("error message") }
        assertThat(errorLogs).isNotEmpty()
    }
    
    @Test
    fun `test error logging with exception`() {
        val exception = RuntimeException("test exception")
        logger.error("error with exception", exception)
        
        val logs = ShadowLog.getLogs()
        val errorLogs = logs.filter { it.msg.contains("error with exception") }
        assertThat(errorLogs).isNotEmpty()
    }
    
    @Test
    fun `test hookSuccess logging`() {
        logger.hookSuccess("TestClass", "testMethod")
        
        val logs = ShadowLog.getLogs()
        val successLogs = logs.filter { 
            it.msg.contains("✓ Hooked") && 
            it.msg.contains("TestClass.testMethod") 
        }
        assertThat(successLogs).isNotEmpty()
    }
    
    @Test
    fun `test hookFailed logging`() {
        val exception = RuntimeException("hook failed")
        logger.hookFailed("TestClass", "testMethod", exception)
        
        val logs = ShadowLog.getLogs()
        val failedLogs = logs.filter { 
            it.msg.contains("✗ Hook failed") && 
            it.msg.contains("TestClass.testMethod") 
        }
        assertThat(failedLogs).isNotEmpty()
    }
    
    @Test
    fun `test hookEvent logging without force`() {
        logger.minLevel = CCOLogger.LogLevel.DEBUG
        logger.hookEvent("testMethod", true, forced = false)
        
        val logs = ShadowLog.getLogs()
        val eventLogs = logs.filter { 
            it.msg.contains("→ testMethod") && 
            it.msg.contains("true") &&
            !it.msg.contains("[FORCED]")
        }
        assertThat(eventLogs).isNotEmpty()
    }
    
    @Test
    fun `test hookEvent logging with force`() {
        logger.minLevel = CCOLogger.LogLevel.DEBUG
        logger.hookEvent("testMethod", true, forced = true)
        
        val logs = ShadowLog.getLogs()
        val eventLogs = logs.filter { 
            it.msg.contains("→ testMethod") && 
            it.msg.contains("true") &&
            it.msg.contains("[FORCED]")
        }
        assertThat(eventLogs).isNotEmpty()
    }
    
    @Test
    fun `test minLevel filtering`() {
        logger.minLevel = CCOLogger.LogLevel.ERROR
        
        logger.debug("debug")
        logger.info("info")
        logger.warn("warn")
        logger.error("error")
        
        val logs = ShadowLog.getLogs()
        assertThat(logs.filter { it.msg.contains("debug") }).isEmpty()
        assertThat(logs.filter { it.msg.contains("info") }).isEmpty()
        assertThat(logs.filter { it.msg.contains("warn") }).isEmpty()
        assertThat(logs.filter { it.msg.contains("error") }).isNotEmpty()
    }
    
    @Test
    fun `test log level ordering`() {
        assertThat(CCOLogger.LogLevel.DEBUG.ordinal)
            .isLessThan(CCOLogger.LogLevel.INFO.ordinal)
        assertThat(CCOLogger.LogLevel.INFO.ordinal)
            .isLessThan(CCOLogger.LogLevel.WARN.ordinal)
        assertThat(CCOLogger.LogLevel.WARN.ordinal)
            .isLessThan(CCOLogger.LogLevel.ERROR.ordinal)
    }
}
