package com.supermarsx.carrierconfig.ui.screens.dashboard

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import com.supermarsx.carrierconfig.data.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [DashboardViewModel].
 *
 * Tests cover:
 * - Initial state loading
 * - runDiagnostics delegates to loadDashboardData (Fix #9: no double-load)
 * - Error handling
 * - State transitions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var deviceRepository: DeviceRepository

    private val testDeviceInfo = DeviceInfo(
        manufacturer = "samsung",
        model = "SM-S928B",
        buildFingerprint = "samsung/...",
        androidVersion = "Android 14 (API 34)",
        oneUIVersion = "One UI 6.1",
        securityPatch = "2024-01-01",
        isRooted = true
    )

    private val testSIMInfo = listOf(
        SIMInfo(0, "T-Mobile", "310", "260", "••••1234", true)
    )

    private val testIMSStatus = IMSStatus(
        isRegistered = true,
        isVoLTEAvailable = true,
        isVoWiFiAvailable = true,
        registrationState = "REGISTERED_WIFI"
    )

    private val testWFCStatus = WFCUIStatus(
        settingsActivityExists = true,
        pagePopulates = true,
        togglePresent = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        deviceRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun stubAllRepositoryCalls() {
        whenever(deviceRepository.getDeviceInfo()).thenReturn(testDeviceInfo)
        whenever(deviceRepository.getSIMInfo()).thenReturn(testSIMInfo)
        whenever(deviceRepository.getIMSStatus()).thenReturn(testIMSStatus)
        whenever(deviceRepository.getWFCUIStatus()).thenReturn(testWFCStatus)
        whenever(deviceRepository.detectBlocker(testIMSStatus, testWFCStatus)).thenReturn(WFCBlocker.NONE)
    }

    private fun createViewModel(): DashboardViewModel {
        val vm = DashboardViewModel(deviceRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    // =========================================================================
    // Initial state
    // =========================================================================

    @Test
    fun `initial state triggers loading`() = runTest(testDispatcher) {
        stubAllRepositoryCalls()
        val vm = createViewModel()
        val state = vm.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.deviceInfo).isEqualTo(testDeviceInfo)
        assertThat(state.simInfo).isEqualTo(testSIMInfo)
        assertThat(state.imsStatus).isEqualTo(testIMSStatus)
        assertThat(state.wfcUIStatus).isEqualTo(testWFCStatus)
        assertThat(state.detectedBlocker).isEqualTo(WFCBlocker.NONE)
    }

    @Test
    fun `initial state handles error`() = runTest(testDispatcher) {
        whenever(deviceRepository.getDeviceInfo()).thenThrow(RuntimeException("No root"))
        val vm = createViewModel()
        val state = vm.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).contains("No root")
    }

    // =========================================================================
    // runDiagnostics (Fix #9: delegates to loadDashboardData, no double-load)
    // =========================================================================

    @Test
    fun `runDiagnostics calls loadDashboardData - device info loaded once per call`() = runTest(testDispatcher) {
        stubAllRepositoryCalls()
        val vm = createViewModel()
        // init calls loadDashboardData once
        verify(deviceRepository, times(1)).getDeviceInfo()

        // runDiagnostics should call loadDashboardData again
        vm.runDiagnostics()
        testDispatcher.scheduler.advanceUntilIdle()

        // Total: 2 calls (init + runDiagnostics), NOT 3 (which would be double-load)
        verify(deviceRepository, times(2)).getDeviceInfo()
        verify(deviceRepository, times(2)).getSIMInfo()
        verify(deviceRepository, times(2)).getIMSStatus()
    }

    @Test
    fun `runDiagnostics refreshes state`() = runTest(testDispatcher) {
        stubAllRepositoryCalls()
        val vm = createViewModel()
        assertThat(vm.state.value.deviceInfo).isNotNull()

        // Modify mock to return different data
        val updatedInfo = testDeviceInfo.copy(model = "SM-S929B")
        whenever(deviceRepository.getDeviceInfo()).thenReturn(updatedInfo)

        vm.runDiagnostics()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value.deviceInfo?.model).isEqualTo("SM-S929B")
    }

    // =========================================================================
    // refresh()
    // =========================================================================

    @Test
    fun `refresh calls loadDashboardData`() = runTest(testDispatcher) {
        stubAllRepositoryCalls()
        val vm = createViewModel()
        vm.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // init + refresh = 2 calls
        verify(deviceRepository, times(2)).getDeviceInfo()
    }

    // =========================================================================
    // DashboardState defaults
    // =========================================================================

    @Test
    fun `DashboardState default values`() {
        val state = DashboardState()
        assertThat(state.deviceInfo).isNull()
        assertThat(state.simInfo).isEmpty()
        assertThat(state.imsStatus).isNull()
        assertThat(state.wfcUIStatus).isNull()
        assertThat(state.detectedBlocker).isEqualTo(WFCBlocker.UNKNOWN)
        assertThat(state.isLoading).isTrue()
        assertThat(state.error).isNull()
    }
}
