package com.supermarsx.carrierconfig.ui.screens.diagnostics

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
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
        runBlocking {
            whenever(logcatRepository.getLogcatSnapshot(
                filterType = LogcatFilterType.ALL,
                lineCount = 500
            )).thenReturn(emptyList())

            // Stub monitorLogcat to return empty flow (used by startLiveLogcat)
            whenever(logcatRepository.monitorLogcat(
                filterType = any(),
                minLevel = any(),
                buffer = any()
            )).thenReturn(emptyFlow())
        }
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

    // =========================================================================
    // Live logcat start/stop
    // =========================================================================

    @Test
    fun `startLiveLogcat sets isLiveLogging true`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.startLiveLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLiveLogging).isTrue()
    }

    @Test
    fun `stopLiveLogcat sets isLiveLogging false`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.startLiveLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.stopLiveLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLiveLogging).isFalse()
    }

    @Test
    fun `startLiveLogcat is idempotent when already running`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.startLiveLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        // Calling again should not throw or duplicate
        viewModel.startLiveLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLiveLogging).isTrue()
    }

    // =========================================================================
    // clearLogcat
    // =========================================================================

    @Test
    fun `clearLogcat empties entries and sets message`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.clearLogcat()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.logcatEntries.value).isEmpty()
        assertThat(viewModel.state.value.message).isEqualTo("Logcat cleared")
    }

    // =========================================================================
    // loadLogcatSnapshot
    // =========================================================================

    @Test
    fun `loadLogcatSnapshot populates logcatEntries`() = runTest(testDispatcher) {
        val entries = listOf(
            LogcatEntry(100L, LogLevel.INFO, "ImsManager", "registered", 1, 1),
            LogcatEntry(200L, LogLevel.DEBUG, "CarrierConfigLoader", "loaded", 2, 2)
        )
        whenever(logcatRepository.getLogcatSnapshot(
            filterType = LogcatFilterType.ALL,
            lineCount = 500
        )).thenReturn(entries)

        viewModel = createViewModel()
        assertThat(viewModel.logcatEntries.value).hasSize(2)
        assertThat(viewModel.logcatEntries.value[0].tag).isEqualTo("ImsManager")
    }

    // =========================================================================
    // Dumpsys service routing
    // =========================================================================

    @Test
    fun `loadDumpsys routes phone service correctly`() = runTest(testDispatcher) {
        val result = DumpsysResult.Success("phone", "phone output", 5)
        whenever(dumpsysRepository.getDumpsysPhone()).thenReturn(result)

        viewModel = createViewModel()
        viewModel.loadDumpsys("phone")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.selectedDumpsysService).isEqualTo("phone")
        assertThat(viewModel.state.value.dumpsysResult).isEqualTo(result)
    }

    @Test
    fun `loadDumpsys routes carrier_config correctly`() = runTest(testDispatcher) {
        val result = DumpsysResult.Success("carrier_config", "config output", 3)
        whenever(dumpsysRepository.getDumpsysCarrierConfig()).thenReturn(result)

        viewModel = createViewModel()
        viewModel.loadDumpsys("carrier_config")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.dumpsysResult).isEqualTo(result)
    }

    @Test
    fun `loadDumpsys routes telecom correctly`() = runTest(testDispatcher) {
        val result = DumpsysResult.Success("telecom", "telecom output", 2)
        whenever(dumpsysRepository.getDumpsysTelecom()).thenReturn(result)

        viewModel = createViewModel()
        viewModel.loadDumpsys("telecom")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.dumpsysResult).isEqualTo(result)
    }

    @Test
    fun `loadDumpsys routes connectivity correctly`() = runTest(testDispatcher) {
        val result = DumpsysResult.Success("connectivity", "connectivity output", 4)
        whenever(dumpsysRepository.getDumpsysConnectivity()).thenReturn(result)

        viewModel = createViewModel()
        viewModel.loadDumpsys("connectivity")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.dumpsysResult).isEqualTo(result)
    }

    @Test
    fun `loadDumpsys unknown service falls back to ims`() = runTest(testDispatcher) {
        val result = DumpsysResult.Success("ims", "ims fallback", 1)
        whenever(dumpsysRepository.getDumpsysIms()).thenReturn(result)

        viewModel = createViewModel()
        viewModel.loadDumpsys("unknown_service")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.dumpsysResult).isEqualTo(result)
    }

    // =========================================================================
    // Export diagnostics
    // =========================================================================

    @Test
    fun `exportDiagnostics success sets message`() = runTest(testDispatcher) {
        whenever(exportRepository.exportDiagnostics(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any()
        )).thenReturn(ExportResult.Success("/export/path.json"))

        viewModel = createViewModel()
        viewModel.exportDiagnostics()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isExporting).isFalse()
        assertThat(state.message).contains("/export/path.json")
    }

    @Test
    fun `exportDiagnostics error sets error`() = runTest(testDispatcher) {
        whenever(exportRepository.exportDiagnostics(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any()
        )).thenReturn(ExportResult.Error("Disk full"))

        viewModel = createViewModel()
        viewModel.exportDiagnostics()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).isEqualTo("Disk full")
    }

    // =========================================================================
    // Export logs
    // =========================================================================

    @Test
    fun `exportLogs success sets message`() = runTest(testDispatcher) {
        whenever(exportRepository.exportTextFile(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any()
        )).thenReturn(ExportResult.Success("/export/logcat.txt"))

        viewModel = createViewModel()
        viewModel.exportLogs()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.message).contains("/export/logcat.txt")
        assertThat(viewModel.state.value.isExporting).isFalse()
    }

    @Test
    fun `exportLogs error sets error`() = runTest(testDispatcher) {
        whenever(exportRepository.exportTextFile(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any()
        )).thenReturn(ExportResult.Error("IO error"))

        viewModel = createViewModel()
        viewModel.exportLogs()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).contains("IO error")
    }

    // =========================================================================
    // runSingleTest
    // =========================================================================

    @Test
    fun `runSingleTest sets message with test name`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        viewModel.runSingleTest("dns")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.message).contains("dns")
    }

    // =========================================================================
    // DiagnosticsState copy semantics
    // =========================================================================

    @Test
    fun `DiagnosticsState copy preserves unchanged fields`() {
        val state = DiagnosticsState()
        val modified = state.copy(selectedTab = 2, isLiveLogging = true)
        assertThat(modified.selectedTab).isEqualTo(2)
        assertThat(modified.isLiveLogging).isTrue()
        assertThat(modified.logcatFilterType).isEqualTo(LogcatFilterType.ALL)
        assertThat(modified.logLevel).isEqualTo(LogLevel.DEBUG)
    }

    @Test
    fun `DiagnosticsState equality`() {
        assertThat(DiagnosticsState()).isEqualTo(DiagnosticsState())
        assertThat(DiagnosticsState(selectedTab = 1)).isNotEqualTo(DiagnosticsState())
    }
}
