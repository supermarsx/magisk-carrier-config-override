package com.supermarsx.carrierconfig.ui.screens.diagnostics

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.repository.*
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [DiagnosticsViewModel].
 *
 * Repositories are mocked so that we can verify ViewModel state transitions
 * without needing a real Android runtime.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var logcatRepository: LogcatRepository
    private lateinit var dumpsysRepository: DumpsysRepository
    private lateinit var connectivityTestRepository: ConnectivityTestRepository
    private lateinit var exportRepository: ExportRepository
    private lateinit var viewModel: DiagnosticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        logcatRepository = mock()
        dumpsysRepository = mock()
        connectivityTestRepository = mock()
        exportRepository = mock()

        // Stub default logcat snapshot (called in init)
        whenever(logcatRepository.getLogcatSnapshot(
            filterType = LogcatFilterType.ALL,
            lineCount = 500
        )).thenReturn(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DiagnosticsViewModel {
        val vm = DiagnosticsViewModel(
            logcatRepository, dumpsysRepository,
            connectivityTestRepository, exportRepository
        )
        // Advance through init { loadLogcatSnapshot() }
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    // =========================================================================
    // Initial state
    // =========================================================================

    @Test
    fun `initial state has correct defaults`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        val state = viewModel.state.value

        assertThat(state.selectedTab).isEqualTo(0)
        assertThat(state.logcatFilterType).isEqualTo(LogcatFilterType.ALL)
        assertThat(state.logLevel).isEqualTo(LogLevel.DEBUG)
        assertThat(state.isLiveLogging).isFalse()
        assertThat(state.isLoadingLogs).isFalse()
        assertThat(state.dumpsysResult).isNull()
        assertThat(state.testResults).isNull()
        assertThat(state.isRunningTests).isFalse()
        assertThat(state.message).isNull()
        assertThat(state.error).isNull()
    }

    @Test
    fun `initial logcatEntries is empty`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        assertThat(viewModel.logcatEntries.value).isEmpty()
    }

    // =========================================================================
    // Filter & level changes
    // =========================================================================

    @Test
    fun `setLogcatFilter updates state and reloads snapshot`() = runTest(testDispatcher) {
        whenever(logcatRepository.getLogcatSnapshot(
            filterType = LogcatFilterType.IMS,
            lineCount = 500
        )).thenReturn(emptyList())

        viewModel = createViewModel()
        viewModel.setLogcatFilter(LogcatFilterType.IMS)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.logcatFilterType).isEqualTo(LogcatFilterType.IMS)
    }

    @Test
    fun `setLogLevel updates state`() = runTest(testDispatcher) {
        whenever(logcatRepository.getLogcatSnapshot(
            filterType = LogcatFilterType.ALL,
            lineCount = 500
        )).thenReturn(emptyList())

        viewModel = createViewModel()
        viewModel.setLogLevel(LogLevel.WARNING)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.logLevel).isEqualTo(LogLevel.WARNING)
    }

    // =========================================================================
    // Dumpsys
    // =========================================================================

    @Test
    fun `loadDumpsys sets result in state`() = runTest(testDispatcher) {
        val dumpsysResult = DumpsysResult.Success("ims", "mRegistered=true", 1)
        whenever(dumpsysRepository.getDumpsysIms()).thenReturn(dumpsysResult)

        viewModel = createViewModel()
        viewModel.loadDumpsys("ims")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isLoadingDumpsys).isFalse()
        assertThat(state.selectedDumpsysService).isEqualTo("ims")
        assertThat(state.dumpsysResult).isEqualTo(dumpsysResult)
    }

    @Test
    fun `loadAllDumpsys populates allDumpsysResults`() = runTest(testDispatcher) {
        val results = mapOf(
            "ims" to DumpsysResult.Success("ims", "output", 10),
            "phone" to DumpsysResult.Error("phone", "denied")
        )
        whenever(dumpsysRepository.getAllDumpsys()).thenReturn(results)

        viewModel = createViewModel()
        viewModel.loadAllDumpsys()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.allDumpsysResults).isEqualTo(results)
    }

    // =========================================================================
    // Connectivity tests
    // =========================================================================

    @Test
    fun `runConnectivityTests sets results in state`() = runTest(testDispatcher) {
        val suite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("OK"),
            dnsResolution = TestResult.Passed("OK"),
            internetConnectivity = TestResult.Passed("OK"),
            wifiCalling = TestResult.Skipped("No Wi-Fi"),
            imsRegistration = TestResult.Passed("OK"),
            cellularData = TestResult.Passed("OK"),
            timestamp = 100L
        )
        whenever(connectivityTestRepository.runFullTestSuite()).thenReturn(suite)

        viewModel = createViewModel()
        viewModel.runConnectivityTests()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isRunningTests).isFalse()
        assertThat(state.testResults).isEqualTo(suite)
    }

    // =========================================================================
    // Tab selection
    // =========================================================================

    @Test
    fun `setSelectedTab updates state`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.setSelectedTab(2)

        assertThat(viewModel.state.value.selectedTab).isEqualTo(2)
    }

    // =========================================================================
    // Message handling
    // =========================================================================

    @Test
    fun `clearMessage resets message and error`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        // Trigger a message via singleTest
        viewModel.runSingleTest("network")
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.message).isNotNull()

        viewModel.clearMessage()
        assertThat(viewModel.state.value.message).isNull()
        assertThat(viewModel.state.value.error).isNull()
    }

    // =========================================================================
    // DiagnosticsState defaults
    // =========================================================================

    @Test
    fun `DiagnosticsState defaults`() {
        val state = DiagnosticsState()
        assertThat(state.selectedTab).isEqualTo(0)
        assertThat(state.logcatFilterType).isEqualTo(LogcatFilterType.ALL)
        assertThat(state.logLevel).isEqualTo(LogLevel.DEBUG)
        assertThat(state.isLiveLogging).isFalse()
        assertThat(state.isLoadingLogs).isFalse()
        assertThat(state.selectedDumpsysService).isEqualTo("ims")
        assertThat(state.dumpsysResult).isNull()
        assertThat(state.allDumpsysResults).isNull()
        assertThat(state.isLoadingDumpsys).isFalse()
        assertThat(state.testResults).isNull()
        assertThat(state.isRunningTests).isFalse()
        assertThat(state.isExporting).isFalse()
        assertThat(state.message).isNull()
        assertThat(state.error).isNull()
    }
}
