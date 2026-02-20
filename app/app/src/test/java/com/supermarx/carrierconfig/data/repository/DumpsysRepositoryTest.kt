package dev.mars.carrierconfig.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class DumpsysRepositoryTest {

    private lateinit var repository: DumpsysRepository

    @Before
    fun setup() {
        repository = DumpsysRepository()
    }

    @Test
    fun `extractImsInfo with valid IMS registration`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            IMS Voice: true
            IMS Video: true
            IMS SMS: false
            VoLTE enabled: true
            VoWiFi enabled: true
            UT enabled: false
            Registration type: WLAN
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertNotNull(info)
        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Type: Wi-Fi"))
        assertTrue(info.contains("Voice: Available"))
        assertTrue(info.contains("Video: Available"))
    }

    @Test
    fun `extractImsInfo with unregistered IMS`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: NOT_REGISTERED
            IMS Voice: false
            IMS Video: false
            VoLTE enabled: false
            VoWiFi enabled: false
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertNotNull(info)
        assertTrue(info.contains("Status: Not Registered"))
        assertTrue(info.contains("Voice: Unavailable"))
        assertTrue(info.contains("Video: Unavailable"))
    }

    @Test
    fun `extractImsInfo with cellular registration`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            Registration type: CELLULAR
            VoLTE enabled: true
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertNotNull(info)
        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Type: Cellular"))
    }

    @Test
    fun `extractImsInfo with mixed capabilities`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            IMS Voice: true
            IMS Video: false
            IMS SMS: true
            VoLTE enabled: true
            VoWiFi enabled: false
            UT enabled: true
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertNotNull(info)
        assertTrue(info.contains("Voice: Available"))
        assertTrue(info.contains("Video: Unavailable"))
    }

    @Test
    fun `extractImsInfo with empty output`() {
        val info = repository.extractImsInfo("")

        assertTrue(info.contains("Status: Unknown"))
    }

    @Test
    fun `extractImsInfo with malformed output`() {
        val dumpsysOutput = "Some random text without IMS info"
        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Unknown"))
    }

    @Test
    fun `extractImsInfo with partial IMS data`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertNotNull(info)
        assertTrue(info.contains("Status: Registered"))
        // Should have fallback values for missing capabilities
        assertTrue(info.contains("Voice:"))
    }

    @Test
    fun `extractImsInfo detects registering state`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERING
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registering"))
    }

    @Test
    fun `extractImsInfo with all capabilities enabled`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            IMS Voice: true
            IMS Video: true
            IMS SMS: true
            VoLTE enabled: true
            VoWiFi enabled: true
            UT enabled: true
            Registration type: WLAN
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
        assertTrue(info.contains("Video: Available"))
        // Should indicate full capabilities
        assertFalse(info.contains("Unavailable"))
    }

    @Test
    fun `extractImsInfo with extra whitespace`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration:    REGISTERED   
            IMS Voice:   true  
            Registration type:   CELLULAR    
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Type: Cellular"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo case insensitive matching`() {
        val dumpsysOutput = """
            IMS Service State:
            ims registration: registered
            ims voice: TRUE
            registration type: wlan
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo with multiline format`() {
        val dumpsysOutput = """
            ==============
            IMS Service State
            ==============
            
            IMS Registration: REGISTERED
            
            Capabilities:
              IMS Voice: true
              IMS Video: true
              IMS SMS: false
            
            Settings:
              VoLTE enabled: true
              VoWiFi enabled: true
              UT enabled: false
            
            Network:
              Registration type: WLAN
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
        assertTrue(info.contains("Type: Wi-Fi"))
    }

    @Test
    fun `extractImsInfo with boolean variations`() {
        val testCases = listOf(
            "IMS Voice: true" to "Available",
            "IMS Voice: false" to "Unavailable",
            "IMS Voice: 1" to "Available",
            "IMS Voice: 0" to "Unavailable",
            "IMS Voice: yes" to "Available",
            "IMS Voice: no" to "Unavailable"
        )

        testCases.forEach { (input, expected) ->
            val dumpsysOutput = """
                IMS Service State:
                IMS Registration: REGISTERED
                $input
            """.trimIndent()

            val info = repository.extractImsInfo(dumpsysOutput)
            assertTrue("Expected '$expected' in output for input '$input'", 
                      info.contains(expected))
        }
    }

    @Test
    fun `extractImsInfo with registration type variations`() {
        val types = mapOf(
            "WLAN" to "Wi-Fi",
            "CELLULAR" to "Cellular",
            "IWLAN" to "Wi-Fi",
            "LTE" to "Cellular",
            "UNKNOWN" to "Unknown"
        )

        types.forEach { (input, expected) ->
            val dumpsysOutput = """
                IMS Service State:
                IMS Registration: REGISTERED
                Registration type: $input
            """.trimIndent()

            val info = repository.extractImsInfo(dumpsysOutput)
            assertTrue("Expected '$expected' for type '$input'", 
                      info.contains("Type: $expected"))
        }
    }

    @Test
    fun `extractImsInfo handles special characters`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED
            IMS Voice: true
            Extra info: [feature_enabled=1, test_mode=0]
            Debug: {capability: voice, video}
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo with very long output`() {
        val longOutput = buildString {
            append("IMS Service State:\n")
            append("IMS Registration: REGISTERED\n")
            repeat(1000) {
                append("Extra line $it\n")
            }
            append("IMS Voice: true\n")
        }

        val info = repository.extractImsInfo(longOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo with Unicode characters`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: REGISTERED ✓
            IMS Voice: true ✓
            Network: Wi-Fi 📶
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo performance with large dataset`() {
        val largeOutput = buildString {
            repeat(10000) {
                append("IMS Service State:\n")
                append("IMS Registration: REGISTERED\n")
                append("IMS Voice: true\n")
                append("IMS Video: true\n")
                append("VoLTE enabled: true\n")
                append("VoWiFi enabled: true\n")
            }
        }

        val startTime = System.currentTimeMillis()
        val info = repository.extractImsInfo(largeOutput)
        val endTime = System.currentTimeMillis()

        assertTrue(info.contains("Status: Registered"))
        // Should complete in reasonable time (< 1 second)
        assertTrue("Processing took ${endTime - startTime}ms", 
                  endTime - startTime < 1000)
    }

    @Test
    fun `extractImsInfo with null safety`() {
        val dumpsysOutput = """
            IMS Service State:
            IMS Registration: null
            IMS Voice: null
        """.trimIndent()

        val info = repository.extractImsInfo(dumpsysOutput)

        // Should not crash and provide fallback values
        assertNotNull(info)
        assertTrue(info.contains("Status:"))
    }

    @Test
    fun `extractImsInfo with mixed line endings`() {
        val dumpsysOutput = "IMS Service State:\rIMS Registration: REGISTERED\r\nIMS Voice: true\n"
        
        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }

    @Test
    fun `extractImsInfo with tabs and spaces`() {
        val dumpsysOutput = "IMS Service State:\n\tIMS Registration:\t\tREGISTERED\n    IMS Voice:    true"
        
        val info = repository.extractImsInfo(dumpsysOutput)

        assertTrue(info.contains("Status: Registered"))
        assertTrue(info.contains("Voice: Available"))
    }
}
