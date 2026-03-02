package com.supermarsx.carrierconfig.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for connectivity tests and network diagnostics
 */
@Singleton
class ConnectivityTestRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    /**
     * Run comprehensive connectivity test suite
     */
    suspend fun runFullTestSuite(): ConnectivityTestSuite = withContext(Dispatchers.IO) {
        ConnectivityTestSuite(
            networkStatus = testNetworkStatus(),
            dnsResolution = testDnsResolution(),
            internetConnectivity = testInternetConnectivity(),
            wifiCalling = testWifiCallingConnectivity(),
            imsRegistration = testImsRegistration(),
            cellularData = testCellularData(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Test network status
     */
    private fun testNetworkStatus(): TestResult {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (capabilities == null) {
                TestResult.Failed("No active network")
            } else {
                val networkType = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Unknown"
                }
                
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                TestResult.Passed(
                    "Active: $networkType | Internet: $hasInternet | Validated: $validated"
                )
            }
        } catch (e: Exception) {
            TestResult.Error("Exception: ${e.message}")
        }
    }
    
    /**
     * Test DNS resolution
     */
    private suspend fun testDnsResolution(): TestResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName("www.google.com")
            val duration = System.currentTimeMillis() - startTime
            
            TestResult.Passed("Resolved to ${address.hostAddress} in ${duration}ms")
        } catch (e: Exception) {
            TestResult.Failed("DNS resolution failed: ${e.message}")
        }
    }
    
    /**
     * Test internet connectivity
     */
    private suspend fun testInternetConnectivity(): TestResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val url = URL("https://www.google.com/generate_204")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            
            val responseCode = connection.responseCode
            val duration = System.currentTimeMillis() - startTime
            connection.disconnect()
            
            if (responseCode == 204 || responseCode == 200) {
                TestResult.Passed("HTTP $responseCode in ${duration}ms")
            } else {
                TestResult.Failed("Unexpected response code: $responseCode")
            }
        } catch (e: Exception) {
            TestResult.Failed("Connection failed: ${e.message}")
        }
    }
    
    /**
     * Test Wi-Fi Calling connectivity
     */
    private suspend fun testWifiCallingConnectivity(): TestResult = withContext(Dispatchers.IO) {
        try {
            // Check if Wi-Fi is active
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
                return@withContext TestResult.Skipped("Not connected to Wi-Fi")
            }
            
            // Check IMS registration over Wi-Fi
            val result = Shell.cmd("dumpsys ims | grep -i wifi").exec()
            val hasWifiRegistration = result.out.any { 
                it.contains("WIFI", ignoreCase = true) && 
                it.contains("REGISTERED", ignoreCase = true) 
            }
            
            if (hasWifiRegistration) {
                TestResult.Passed("IMS registered over Wi-Fi")
            } else {
                TestResult.Failed("IMS not registered over Wi-Fi")
            }
        } catch (e: Exception) {
            TestResult.Error("Exception: ${e.message}")
        }
    }
    
    /**
     * Test IMS registration
     */
    private suspend fun testImsRegistration(): TestResult = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("dumpsys ims | grep -i registered").exec()
            
            if (result.isSuccess && result.out.isNotEmpty()) {
                val registered = result.out.any { 
                    it.contains("mRegistered=true", ignoreCase = true) ||
                    it.contains("STATE_REGISTERED", ignoreCase = true)
                }
                
                if (registered) {
                    TestResult.Passed("IMS registered")
                } else {
                    TestResult.Failed("IMS not registered")
                }
            } else {
                TestResult.Error("Unable to query IMS status")
            }
        } catch (e: Exception) {
            TestResult.Error("Exception: ${e.message}")
        }
    }
    
    /**
     * Test cellular data connectivity
     */
    private fun testCellularData(): TestResult {
        return try {
            val dataState = telephonyManager.dataState
            val dataEnabled = telephonyManager.isDataEnabled
            @Suppress("DEPRECATION")
            val networkType = telephonyManager.dataNetworkType
            
            val stateString = when (dataState) {
                TelephonyManager.DATA_CONNECTED -> "Connected"
                TelephonyManager.DATA_CONNECTING -> "Connecting"
                TelephonyManager.DATA_DISCONNECTED -> "Disconnected"
                TelephonyManager.DATA_SUSPENDED -> "Suspended"
                else -> "Unknown"
            }
            
            val typeString = getNetworkTypeString(networkType)
            
            if (dataState == TelephonyManager.DATA_CONNECTED) {
                TestResult.Passed("$stateString ($typeString) | Enabled: $dataEnabled")
            } else {
                TestResult.Failed("$stateString | Enabled: $dataEnabled")
            }
        } catch (e: Exception) {
            TestResult.Error("Exception: ${e.message}")
        }
    }
    
    /**
     * Get network type string
     */
    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_HSPA, 
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            else -> "Unknown"
        }
    }
    
    /**
     * Test specific endpoint connectivity
     */
    suspend fun testEndpoint(url: String, timeout: Int = 5000): TestResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.connectTimeout = timeout
            urlConnection.readTimeout = timeout
            
            val responseCode = urlConnection.responseCode
            val duration = System.currentTimeMillis() - startTime
            urlConnection.disconnect()
            
            if (responseCode in 200..299) {
                TestResult.Passed("HTTP $responseCode in ${duration}ms")
            } else {
                TestResult.Failed("HTTP $responseCode")
            }
        } catch (e: Exception) {
            TestResult.Failed("Connection failed: ${e.message}")
        }
    }
}

/**
 * Connectivity test suite results
 */
data class ConnectivityTestSuite(
    val networkStatus: TestResult,
    val dnsResolution: TestResult,
    val internetConnectivity: TestResult,
    val wifiCalling: TestResult,
    val imsRegistration: TestResult,
    val cellularData: TestResult,
    val timestamp: Long
) {
    val allPassed: Boolean
        get() = listOf(
            networkStatus, dnsResolution, internetConnectivity,
            wifiCalling, imsRegistration, cellularData
        ).all { it is TestResult.Passed || it is TestResult.Skipped }
    
    val failedCount: Int
        get() = listOf(
            networkStatus, dnsResolution, internetConnectivity,
            wifiCalling, imsRegistration, cellularData
        ).count { it is TestResult.Failed || it is TestResult.Error }
}

/**
 * Test result sealed class
 */
sealed class TestResult {
    data class Passed(val message: String) : TestResult()
    data class Failed(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
    data class Skipped(val reason: String) : TestResult()
}
