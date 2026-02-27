package com.supermarsx.carrierconfig.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LogcatRepositoryTest {

    private lateinit var repository: LogcatRepository

    @Before
    fun setup() {
        repository = LogcatRepository()
    }

    @Test
    fun `parseLogEntry with valid threadtime format`() {
        val line = "01-15 10:30:45.123  1234  5678 I CarrierConfigManager: Test message"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertEquals("01-15 10:30:45.123", entry?.timestamp)
        assertEquals("1234", entry?.pid)
        assertEquals("5678", entry?.tid)
        assertEquals("I", entry?.level)
        assertEquals("CarrierConfigManager", entry?.tag)
        assertEquals("Test message", entry?.message)
    }

    @Test
    fun `parseLogEntry with invalid format returns null`() {
        val line = "Invalid log line"
        val entry = repository.parseLogEntry(line)

        assertNull(entry)
    }

    @Test
    fun `parseLogEntry with different log levels`() {
        val levels = listOf("V", "D", "I", "W", "E", "F")
        
        levels.forEach { level ->
            val line = "01-15 10:30:45.123  1234  5678 $level TestTag: Test message"
            val entry = repository.parseLogEntry(line)

            assertNotNull(entry)
            assertEquals(level, entry?.level)
        }
    }

    @Test
    fun `parseLogEntry with multiline message`() {
        val line = "01-15 10:30:45.123  1234  5678 E TestTag: Error occurred\n\tat line 1\n\tat line 2"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertTrue(entry?.message?.contains("Error occurred") == true)
        assertTrue(entry?.message?.contains("\tat line 1") == true)
    }

    @Test
    fun `parseLogEntry with special characters in message`() {
        val line = "01-15 10:30:45.123  1234  5678 I TestTag: Message with [brackets] and (parens) and {braces}"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertTrue(entry?.message?.contains("[brackets]") == true)
        assertTrue(entry?.message?.contains("(parens)") == true)
        assertTrue(entry?.message?.contains("{braces}") == true)
    }

    @Test
    fun `parseLogEntry with empty message`() {
        val line = "01-15 10:30:45.123  1234  5678 I TestTag: "
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertEquals("", entry?.message)
    }

    @Test
    fun `parseLogEntry with very long tag name`() {
        val longTag = "VeryLongTagNameThatExceedsNormalLengthButShouldStillParse"
        val line = "01-15 10:30:45.123  1234  5678 I $longTag: Test message"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertEquals(longTag, entry?.tag)
    }

    @Test
    fun `parseLogEntry with tag containing numbers`() {
        val line = "01-15 10:30:45.123  1234  5678 D TAG123: Numeric tag"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertEquals("TAG123", entry?.tag)
    }

    @Test
    fun `parseLogEntry with milliseconds precision`() {
        val line = "01-15 10:30:45.999  1234  5678 I TestTag: High precision"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertTrue(entry?.timestamp?.endsWith(".999") == true)
    }

    @Test
    fun `parseLogEntry with different date formats within year`() {
        val dates = listOf("01-01", "06-15", "12-31")
        
        dates.forEach { date ->
            val line = "$date 10:30:45.123  1234  5678 I TestTag: Date test"
            val entry = repository.parseLogEntry(line)

            assertNotNull(entry)
            assertTrue(entry?.timestamp?.startsWith(date) == true)
        }
    }

    @Test
    fun `matchesCategory with CarrierConfig category`() = runTest {
        assertTrue(repository.matchesCategory("CarrierConfigManager", LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertTrue(repository.matchesCategory("CarrierConfigLoader", LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertFalse(repository.matchesCategory("ImsManager", LogcatRepository.LogCategory.CARRIER_CONFIG))
    }

    @Test
    fun `matchesCategory with IMS category`() = runTest {
        assertTrue(repository.matchesCategory("ImsManager", LogcatRepository.LogCategory.IMS))
        assertTrue(repository.matchesCategory("ImsPhone", LogcatRepository.LogCategory.IMS))
        assertTrue(repository.matchesCategory("ImsService", LogcatRepository.LogCategory.IMS))
        assertFalse(repository.matchesCategory("CarrierConfig", LogcatRepository.LogCategory.IMS))
    }

    @Test
    fun `matchesCategory with Telephony category`() = runTest {
        assertTrue(repository.matchesCategory("TelephonyManager", LogcatRepository.LogCategory.TELEPHONY))
        assertTrue(repository.matchesCategory("GsmCdmaPhone", LogcatRepository.LogCategory.TELEPHONY))
        assertFalse(repository.matchesCategory("WifiManager", LogcatRepository.LogCategory.TELEPHONY))
    }

    @Test
    fun `matchesCategory with WFC category`() = runTest {
        assertTrue(repository.matchesCategory("WifiCalling", LogcatRepository.LogCategory.WFC))
        assertTrue(repository.matchesCategory("ImsPhoneCallTracker", LogcatRepository.LogCategory.WFC))
        assertFalse(repository.matchesCategory("Bluetooth", LogcatRepository.LogCategory.WFC))
    }

    @Test
    fun `matchesCategory with ALL category matches everything`() = runTest {
        assertTrue(repository.matchesCategory("AnyTag", LogcatRepository.LogCategory.ALL))
        assertTrue(repository.matchesCategory("RandomTag", LogcatRepository.LogCategory.ALL))
        assertTrue(repository.matchesCategory("", LogcatRepository.LogCategory.ALL))
    }

    @Test
    fun `matchesLogLevel with correct levels`() {
        assertTrue(repository.matchesLogLevel("V", LogcatRepository.LogLevel.VERBOSE))
        assertTrue(repository.matchesLogLevel("D", LogcatRepository.LogLevel.DEBUG))
        assertTrue(repository.matchesLogLevel("I", LogcatRepository.LogLevel.INFO))
        assertTrue(repository.matchesLogLevel("W", LogcatRepository.LogLevel.WARNING))
        assertTrue(repository.matchesLogLevel("E", LogcatRepository.LogLevel.ERROR))
        assertTrue(repository.matchesLogLevel("F", LogcatRepository.LogLevel.FATAL))
    }

    @Test
    fun `matchesLogLevel with INFO level filters correctly`() {
        val infoLevel = LogcatRepository.LogLevel.INFO

        assertFalse(repository.matchesLogLevel("V", infoLevel))
        assertFalse(repository.matchesLogLevel("D", infoLevel))
        assertTrue(repository.matchesLogLevel("I", infoLevel))
        assertTrue(repository.matchesLogLevel("W", infoLevel))
        assertTrue(repository.matchesLogLevel("E", infoLevel))
        assertTrue(repository.matchesLogLevel("F", infoLevel))
    }

    @Test
    fun `matchesLogLevel with ERROR level filters correctly`() {
        val errorLevel = LogcatRepository.LogLevel.ERROR

        assertFalse(repository.matchesLogLevel("V", errorLevel))
        assertFalse(repository.matchesLogLevel("D", errorLevel))
        assertFalse(repository.matchesLogLevel("I", errorLevel))
        assertFalse(repository.matchesLogLevel("W", errorLevel))
        assertTrue(repository.matchesLogLevel("E", errorLevel))
        assertTrue(repository.matchesLogLevel("F", errorLevel))
    }

    @Test
    fun `matchesLogLevel with FATAL level filters correctly`() {
        val fatalLevel = LogcatRepository.LogLevel.FATAL

        assertFalse(repository.matchesLogLevel("V", fatalLevel))
        assertFalse(repository.matchesLogLevel("D", fatalLevel))
        assertFalse(repository.matchesLogLevel("I", fatalLevel))
        assertFalse(repository.matchesLogLevel("W", fatalLevel))
        assertFalse(repository.matchesLogLevel("E", fatalLevel))
        assertTrue(repository.matchesLogLevel("F", fatalLevel))
    }

    @Test
    fun `LogEntry data class properties`() {
        val entry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "TestTag",
            message = "Test message"
        )

        assertEquals("01-15 10:30:45.123", entry.timestamp)
        assertEquals("1234", entry.pid)
        assertEquals("5678", entry.tid)
        assertEquals("I", entry.level)
        assertEquals("TestTag", entry.tag)
        assertEquals("Test message", entry.message)
    }

    @Test
    fun `LogCategory enum values`() {
        val categories = LogcatRepository.LogCategory.values()

        assertEquals(5, categories.size)
        assertTrue(categories.contains(LogcatRepository.LogCategory.ALL))
        assertTrue(categories.contains(LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertTrue(categories.contains(LogcatRepository.LogCategory.IMS))
        assertTrue(categories.contains(LogcatRepository.LogCategory.TELEPHONY))
        assertTrue(categories.contains(LogcatRepository.LogCategory.WFC))
    }

    @Test
    fun `LogLevel enum values and priority`() {
        val levels = LogcatRepository.LogLevel.values()

        assertEquals(6, levels.size)
        assertTrue(LogcatRepository.LogLevel.VERBOSE.priority < LogcatRepository.LogLevel.DEBUG.priority)
        assertTrue(LogcatRepository.LogLevel.DEBUG.priority < LogcatRepository.LogLevel.INFO.priority)
        assertTrue(LogcatRepository.LogLevel.INFO.priority < LogcatRepository.LogLevel.WARNING.priority)
        assertTrue(LogcatRepository.LogLevel.WARNING.priority < LogcatRepository.LogLevel.ERROR.priority)
        assertTrue(LogcatRepository.LogLevel.ERROR.priority < LogcatRepository.LogLevel.FATAL.priority)
    }

    @Test
    fun `edge case - parse log with colon in message`() {
        val line = "01-15 10:30:45.123  1234  5678 I TestTag: Message with: multiple: colons"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertEquals("Message with: multiple: colons", entry?.message)
    }

    @Test
    fun `edge case - parse log with leading spaces in message`() {
        val line = "01-15 10:30:45.123  1234  5678 I TestTag:    Leading spaces"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        // Should preserve leading spaces in message
        assertTrue(entry?.message?.startsWith(" ") == true)
    }

    @Test
    fun `edge case - parse log with trailing spaces`() {
        val line = "01-15 10:30:45.123  1234  5678 I TestTag: Trailing spaces   "
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertTrue(entry?.message?.endsWith(" ") == true)
    }

    @Test
    fun `performance - parseLogEntry handles large message`() {
        val largeMessage = "Message".repeat(1000)
        val line = "01-15 10:30:45.123  1234  5678 I TestTag: $largeMessage"
        val entry = repository.parseLogEntry(line)

        assertNotNull(entry)
        assertTrue(entry?.message?.length ?: 0 > 5000)
    }

    @Test
    fun `category tags case insensitive matching`() {
        assertTrue(repository.matchesCategory("carrierconfig", LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertTrue(repository.matchesCategory("CARRIERCONFIG", LogcatRepository.LogCategory.CARRIER_CONFIG))
        assertTrue(repository.matchesCategory("CarrierConfig", LogcatRepository.LogCategory.CARRIER_CONFIG))
    }
}
