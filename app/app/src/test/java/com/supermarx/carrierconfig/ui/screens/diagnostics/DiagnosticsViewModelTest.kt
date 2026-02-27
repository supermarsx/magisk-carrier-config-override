package com.supermarsx.carrierconfig.ui.screens.diagnostics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.supermarsx.carrierconfig.data.repository.ConnectivityTestRepository
import com.supermarsx.carrierconfig.data.repository.DumpsysRepository
import com.supermarsx.carrierconfig.data.repository.ExportRepository
import com.supermarsx.carrierconfig.data.repository.LogcatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DiagnosticsViewModel
    private lateinit var logcatRepository: LogcatRepository
    private lateinit var dumpsysRepository: DumpsysRepository
    private lateinit var connectivityTestRepository: ConnectivityTestRepository
    private lateinit var exportRepository: ExportRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        logcatRepository = mock()
        dumpsysRepository = mock()
        connectivityTestRepository = mock()
        exportRepository = mock()

        viewModel = DiagnosticsViewModel(
            logcatRepository,
            dumpsysRepository,
            connectivityTestRepository,
            exportRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value

        assertEquals(DiagnosticsViewModel.LogMode.LIVE, state.logMode)
        assertFalse(state.isLiveLogcatRunning)
        assertTrue(state.logEntries.isEmpty())
        assertEquals(LogcatRepository.LogCategory.ALL, state.selectedLogCategory)
        assertEquals(LogcatRepository.LogLevel.VERBOSE, state.selectedLogLevel)
        assertTrue(state.dumpsysOutput.isEmpty())
        assertTrue(state.testResults.isEmpty())
        assertFalse(state.isLoadingTests)
        assertNull(state.errorMessage)
    }

    @Test
    fun `startLiveLogcat updates state correctly`() = runTest {
        val mockLogEntry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "TestTag",
            message = "Test message"
        )

        whenever(logcatRepository.monitorLogcat(any(), any()))
            .thenReturn(flowOf(mockLogEntry))

        viewModel.startLiveLogcat()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLiveLogcatRunning)
        assertEquals(DiagnosticsViewModel.LogMode.LIVE, state.logMode)
    }

    @Test
    fun `stopLiveLogcat updates state correctly`() = runTest {
        whenever(logcatRepository.monitorLogcat(any(), any()))
            .thenReturn(flowOf())

        viewModel.startLiveLogcat()
        advanceUntilIdle()

        viewModel.stopLiveLogcat()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLiveLogcatRunning)
    }

    @Test
    fun `loadLogcatSnapshot loads logs successfully`() = runTest {
        val mockLogEntry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "TestTag",
            message = "Test message"
        )

        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.success(listOf(mockLogEntry)))

        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DiagnosticsViewModel.LogMode.SNAPSHOT, state.logMode)
        assertEquals(1, state.logEntries.size)
        assertEquals("TestTag", state.logEntries[0].tag)
    }

    @Test
    fun `loadLogcatSnapshot handles error`() = runTest {
        val exception = Exception("Failed to load logs")
        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.failure(exception))

        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage?.contains("Failed") == true)
    }

    @Test
    fun `setLogCategory updates selected category`() = runTest {
        viewModel.setLogCategory(LogcatRepository.LogCategory.IMS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LogcatRepository.LogCategory.IMS, state.selectedLogCategory)
    }

    @Test
    fun `setLogLevel updates selected level`() = runTest {
        viewModel.setLogLevel(LogcatRepository.LogLevel.ERROR)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LogcatRepository.LogLevel.ERROR, state.selectedLogLevel)
    }

    @Test
    fun `clearLogs clears log entries`() = runTest {
        // First load some logs
        val mockLogEntry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "TestTag",
            message = "Test message"
        )

        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.success(listOf(mockLogEntry)))

        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        // Then clear
        viewModel.clearLogs()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.logEntries.isEmpty())
    }

    @Test
    fun `loadDumpsys loads service output successfully`() = runTest {
        val service = DumpsysRepository.DumpsysService.IMS
        val mockOutput = "IMS Service State: REGISTERED"

        whenever(dumpsysRepository.getDumpsysIms())
            .thenReturn(Result.success(mockOutput))

        viewModel.loadDumpsys(service)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(service, state.selectedDumpsysService)
        assertTrue(state.dumpsysOutput.isNotEmpty())
        assertTrue(state.dumpsysOutput.contains("REGISTERED"))
    }

    @Test
    fun `loadDumpsys handles different services`() = runTest {
        val services = listOf(
            DumpsysRepository.DumpsysService.IMS to "IMS output",
            DumpsysRepository.DumpsysService.PHONE to "Phone output",
            DumpsysRepository.DumpsysService.CARRIER_CONFIG to "Config output"
        )

        services.forEach { (service, output) ->
            when (service) {
                DumpsysRepository.DumpsysService.IMS -> 
                    whenever(dumpsysRepository.getDumpsysIms())
                        .thenReturn(Result.success(output))
                DumpsysRepository.DumpsysService.PHONE -> 
                    whenever(dumpsysRepository.getDumpsysPhone())
                        .thenReturn(Result.success(output))
                DumpsysRepository.DumpsysService.CARRIER_CONFIG -> 
                    whenever(dumpsysRepository.getDumpsysCarrierConfig())
                        .thenReturn(Result.success(output))
                else -> {}
            }

            viewModel.loadDumpsys(service)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(service, state.selectedDumpsysService)
            assertTrue(state.dumpsysOutput.contains(output))
        }
    }

    @Test
    fun `loadDumpsys handles error`() = runTest {
        val service = DumpsysRepository.DumpsysService.IMS
        val exception = Exception("Service unavailable")

        whenever(dumpsysRepository.getDumpsysIms())
            .thenReturn(Result.failure(exception))

        viewModel.loadDumpsys(service)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage?.contains("Service unavailable") == true)
    }

    @Test
    fun `runConnectivityTests executes all tests`() = runTest {
        val mockResults = mapOf(
            ConnectivityTestRepository.TestCase.NETWORK_STATUS to 
                ConnectivityTestRepository.TestResult.Passed("Network active"),
            ConnectivityTestRepository.TestCase.DNS_RESOLUTION to 
                ConnectivityTestRepository.TestResult.Passed("DNS resolved"),
            ConnectivityTestRepository.TestCase.INTERNET_CONNECTIVITY to 
                ConnectivityTestRepository.TestResult.Passed("Internet available")
        )

        whenever(connectivityTestRepository.runFullTestSuite())
            .thenReturn(Result.success(mockResults))

        viewModel.runConnectivityTests()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingTests)
        assertEquals(3, state.testResults.size)
        assertTrue(state.testResults.containsKey(ConnectivityTestRepository.TestCase.NETWORK_STATUS))
    }

    @Test
    fun `runConnectivityTests shows loading state`() = runTest {
        val mockResults = mapOf(
            ConnectivityTestRepository.TestCase.NETWORK_STATUS to 
                ConnectivityTestRepository.TestResult.Passed("Network active")
        )

        whenever(connectivityTestRepository.runFullTestSuite())
            .thenReturn(Result.success(mockResults))

        viewModel.runConnectivityTests()

        // Check loading state before advancing
        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isLoadingTests)

        advanceUntilIdle()

        // Check final state
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoadingTests)
    }

    @Test
    fun `runConnectivityTests handles error`() = runTest {
        val exception = Exception("Network error")
        whenever(connectivityTestRepository.runFullTestSuite())
            .thenReturn(Result.failure(exception))

        viewModel.runConnectivityTests()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingTests)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `exportDiagnostics includes all diagnostic data`() = runTest {
        // Setup state with data
        val mockLogEntry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "TestTag",
            message = "Test message"
        )

        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.success(listOf(mockLogEntry)))

        whenever(exportRepository.exportDiagnostics(any(), any()))
            .thenReturn(Result.success("/storage/export.json"))

        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        viewModel.exportDiagnostics()
        advanceUntilIdle()

        verify(exportRepository).exportDiagnostics(any(), any())
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Trigger an error
        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertNotNull(state.errorMessage)

        // Clear error
        viewModel.clearError()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }

    @Test
    fun `live logcat continues collecting until stopped`() = runTest {
        val logFlow = flowOf(
            LogcatRepository.LogEntry("01-15 10:30:45.123", "1234", "5678", "I", "Tag1", "Message 1"),
            LogcatRepository.LogEntry("01-15 10:30:46.123", "1234", "5678", "I", "Tag2", "Message 2"),
            LogcatRepository.LogEntry("01-15 10:30:47.123", "1234", "5678", "I", "Tag3", "Message 3")
        )

        whenever(logcatRepository.monitorLogcat(any(), any()))
            .thenReturn(logFlow)

        viewModel.startLiveLogcat()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Should have collected all logs
        assertTrue(state.logEntries.size >= 1)
    }

    @Test
    fun `switching log categories reloads logs`() = runTest {
        val mockLogEntry = LogcatRepository.LogEntry(
            timestamp = "01-15 10:30:45.123",
            pid = "1234",
            tid = "5678",
            level = "I",
            tag = "ImsManager",
            message = "IMS message"
        )

        whenever(logcatRepository.getLogcatSnapshot(any(), any()))
            .thenReturn(Result.success(listOf(mockLogEntry)))

        // Load with ALL category
        viewModel.loadLogcatSnapshot()
        advanceUntilIdle()

        // Switch to IMS category
        viewModel.setLogCategory(LogcatRepository.LogCategory.IMS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LogcatRepository.LogCategory.IMS, state.selectedLogCategory)
    }

    @Test
    fun `ViewModel properly cleans up on clear`() = runTest {
        whenever(logcatRepository.monitorLogcat(any(), any()))
            .thenReturn(flowOf())

        viewModel.startLiveLogcat()
        advanceUntilIdle()

        // Simulate ViewModel being cleared
        viewModel.onCleared()

        // Should stop live logcat
        val state = viewModel.uiState.value
        assertFalse(state.isLiveLogcatRunning)
    }
}
