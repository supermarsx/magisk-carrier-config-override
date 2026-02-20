package dev.mars.carrierconfig.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityTestRepositoryTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var telephonyManager: TelephonyManager

    @Before
    fun setup() {
        context = mock(Context::class.java)
        connectivityManager = mock(ConnectivityManager::class.java)
        telephonyManager = mock(TelephonyManager::class.java)
        
        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
    }

    @Test
    fun `TestResult Passed has correct properties`() {
        val result = ConnectivityTestRepository.TestResult.Passed("Test passed")

        assertEquals("Test passed", result.message)
        assertTrue(result is ConnectivityTestRepository.TestResult.Passed)
    }

    @Test
    fun `TestResult Failed has correct properties`() {
        val result = ConnectivityTestRepository.TestResult.Failed("Test failed")

        assertEquals("Test failed", result.message)
        assertTrue(result is ConnectivityTestRepository.TestResult.Failed)
    }

    @Test
    fun `TestResult Error has correct properties`() {
        val exception = Exception("Test error")
        val result = ConnectivityTestRepository.TestResult.Error(exception)

        assertEquals(exception, result.exception)
        assertTrue(result is ConnectivityTestRepository.TestResult.Error)
    }

    @Test
    fun `TestResult Skipped has correct properties`() {
        val result = ConnectivityTestRepository.TestResult.Skipped("Test skipped")

        assertEquals("Test skipped", result.reason)
        assertTrue(result is ConnectivityTestRepository.TestResult.Skipped)
    }

    @Test
    fun `TestCase enum contains all expected tests`() {
        val testCases = ConnectivityTestRepository.TestCase.values()

        assertEquals(6, testCases.size)
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.NETWORK_STATUS))
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.DNS_RESOLUTION))
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.INTERNET_CONNECTIVITY))
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.WIFI_CALLING_CAPABILITY))
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.IMS_REGISTRATION))
        assertTrue(testCases.contains(ConnectivityTestRepository.TestCase.CELLULAR_DATA_STATUS))
    }

    @Test
    fun `TestCase has correct display names`() {
        assertEquals("Network Status", ConnectivityTestRepository.TestCase.NETWORK_STATUS.displayName)
        assertEquals("DNS Resolution", ConnectivityTestRepository.TestCase.DNS_RESOLUTION.displayName)
        assertEquals("Internet Connectivity", ConnectivityTestRepository.TestCase.INTERNET_CONNECTIVITY.displayName)
        assertEquals("Wi-Fi Calling Capability", ConnectivityTestRepository.TestCase.WIFI_CALLING_CAPABILITY.displayName)
        assertEquals("IMS Registration", ConnectivityTestRepository.TestCase.IMS_REGISTRATION.displayName)
        assertEquals("Cellular Data Status", ConnectivityTestRepository.TestCase.CELLULAR_DATA_STATUS.displayName)
    }

    @Test
    fun `TestCase has correct descriptions`() {
        val networkTest = ConnectivityTestRepository.TestCase.NETWORK_STATUS
        assertTrue(networkTest.description.contains("Checks if device has active network"))

        val dnsTest = ConnectivityTestRepository.TestCase.DNS_RESOLUTION
        assertTrue(dnsTest.description.contains("DNS resolution"))

        val internetTest = ConnectivityTestRepository.TestCase.INTERNET_CONNECTIVITY
        assertTrue(internetTest.description.contains("Internet connectivity"))

        val wfcTest = ConnectivityTestRepository.TestCase.WIFI_CALLING_CAPABILITY
        assertTrue(wfcTest.description.contains("Wi-Fi Calling"))

        val imsTest = ConnectivityTestRepository.TestCase.IMS_REGISTRATION
        assertTrue(imsTest.description.contains("IMS"))

        val cellularTest = ConnectivityTestRepository.TestCase.CELLULAR_DATA_STATUS
        assertTrue(cellularTest.description.contains("cellular data"))
    }

    @Test
    fun `TestResult sealed class hierarchy`() {
        val results: List<ConnectivityTestRepository.TestResult> = listOf(
            ConnectivityTestRepository.TestResult.Passed("Success"),
            ConnectivityTestRepository.TestResult.Failed("Failure"),
            ConnectivityTestRepository.TestResult.Error(Exception("Error")),
            ConnectivityTestRepository.TestResult.Skipped("Skip")
        )

        results.forEach { result ->
            when (result) {
                is ConnectivityTestRepository.TestResult.Passed -> assertTrue(result.message.isNotEmpty())
                is ConnectivityTestRepository.TestResult.Failed -> assertTrue(result.message.isNotEmpty())
                is ConnectivityTestRepository.TestResult.Error -> assertNotNull(result.exception)
                is ConnectivityTestRepository.TestResult.Skipped -> assertTrue(result.reason.isNotEmpty())
            }
        }
    }

    @Test
    fun `TestResult toString provides useful information`() {
        val passed = ConnectivityTestRepository.TestResult.Passed("All good")
        assertTrue(passed.toString().contains("Passed") || passed.toString().contains("All good"))

        val failed = ConnectivityTestRepository.TestResult.Failed("Something wrong")
        assertTrue(failed.toString().contains("Failed") || failed.toString().contains("Something wrong"))

        val error = ConnectivityTestRepository.TestResult.Error(Exception("Crash"))
        assertTrue(error.toString().contains("Error") || error.toString().contains("Crash"))

        val skipped = ConnectivityTestRepository.TestResult.Skipped("Not applicable")
        assertTrue(skipped.toString().contains("Skipped") || skipped.toString().contains("Not applicable"))
    }

    @Test
    fun `TestCase displayName is human readable`() {
        ConnectivityTestRepository.TestCase.values().forEach { testCase ->
            val displayName = testCase.displayName
            
            // Should not be empty
            assertTrue(displayName.isNotEmpty())
            
            // Should start with capital letter
            assertTrue(displayName[0].isUpperCase())
            
            // Should not contain underscores (formatted)
            assertFalse(displayName.contains('_'))
        }
    }

    @Test
    fun `TestCase description is informative`() {
        ConnectivityTestRepository.TestCase.values().forEach { testCase ->
            val description = testCase.description
            
            // Should not be empty
            assertTrue(description.isNotEmpty())
            
            // Should be reasonably long (at least 20 characters)
            assertTrue(description.length >= 20)
            
            // Should contain the display name or related keywords
            val keywords = testCase.displayName.split(" ")
            assertTrue(keywords.any { keyword -> 
                description.contains(keyword, ignoreCase = true) 
            })
        }
    }

    @Test
    fun `all TestCase values have unique display names`() {
        val displayNames = ConnectivityTestRepository.TestCase.values()
            .map { it.displayName }
        
        val uniqueNames = displayNames.toSet()
        assertEquals(displayNames.size, uniqueNames.size)
    }

    @Test
    fun `all TestCase values have unique descriptions`() {
        val descriptions = ConnectivityTestRepository.TestCase.values()
            .map { it.description }
        
        val uniqueDescriptions = descriptions.toSet()
        assertEquals(descriptions.size, uniqueDescriptions.size)
    }

    @Test
    fun `TestResult equality and hashCode`() {
        val passed1 = ConnectivityTestRepository.TestResult.Passed("Test")
        val passed2 = ConnectivityTestRepository.TestResult.Passed("Test")
        val passed3 = ConnectivityTestRepository.TestResult.Passed("Different")

        assertEquals(passed1, passed2)
        assertEquals(passed1.hashCode(), passed2.hashCode())
        assertNotEquals(passed1, passed3)
    }

    @Test
    fun `TestResult Error with different exceptions`() {
        val exception1 = Exception("Error 1")
        val exception2 = Exception("Error 2")
        val error1 = ConnectivityTestRepository.TestResult.Error(exception1)
        val error2 = ConnectivityTestRepository.TestResult.Error(exception2)

        assertNotEquals(error1, error2)
    }

    @Test
    fun `TestResult Skipped with different reasons`() {
        val skipped1 = ConnectivityTestRepository.TestResult.Skipped("Reason 1")
        val skipped2 = ConnectivityTestRepository.TestResult.Skipped("Reason 2")

        assertNotEquals(skipped1, skipped2)
    }

    @Test
    fun `TestCase enum order is logical`() {
        val values = ConnectivityTestRepository.TestCase.values()
        
        // Network status should be first (most basic)
        assertEquals(ConnectivityTestRepository.TestCase.NETWORK_STATUS, values[0])
        
        // DNS and Internet should come early (basic connectivity)
        assertTrue(values.indexOf(ConnectivityTestRepository.TestCase.DNS_RESOLUTION) < 3)
        assertTrue(values.indexOf(ConnectivityTestRepository.TestCase.INTERNET_CONNECTIVITY) < 3)
        
        // Advanced tests (IMS, WFC) should come later
        val imsIndex = values.indexOf(ConnectivityTestRepository.TestCase.IMS_REGISTRATION)
        val wfcIndex = values.indexOf(ConnectivityTestRepository.TestCase.WIFI_CALLING_CAPABILITY)
        assertTrue(imsIndex > 2 || wfcIndex > 2)
    }

    @Test
    fun `TestResult message formatting`() {
        val testMessages = listOf(
            "Simple message",
            "Message with numbers: 123",
            "Message with special chars: @#$%",
            "Very long message ".repeat(50)
        )

        testMessages.forEach { message ->
            val passed = ConnectivityTestRepository.TestResult.Passed(message)
            assertEquals(message, passed.message)

            val failed = ConnectivityTestRepository.TestResult.Failed(message)
            assertEquals(message, failed.message)
        }
    }

    @Test
    fun `TestResult handles empty strings`() {
        val passedEmpty = ConnectivityTestRepository.TestResult.Passed("")
        assertEquals("", passedEmpty.message)

        val failedEmpty = ConnectivityTestRepository.TestResult.Failed("")
        assertEquals("", failedEmpty.message)

        val skippedEmpty = ConnectivityTestRepository.TestResult.Skipped("")
        assertEquals("", skippedEmpty.reason)
    }

    @Test
    fun `TestResult Error with null message exception`() {
        val exception = Exception(null as String?)
        val error = ConnectivityTestRepository.TestResult.Error(exception)

        assertNotNull(error.exception)
        assertNull(error.exception.message)
    }

    @Test
    fun `TestCase description contains action verbs`() {
        val actionVerbs = listOf("check", "test", "verify", "validate", "monitor")
        
        ConnectivityTestRepository.TestCase.values().forEach { testCase ->
            val description = testCase.description.lowercase()
            assertTrue(
                "Description '${testCase.description}' should contain action verb",
                actionVerbs.any { verb -> description.contains(verb) }
            )
        }
    }
}
