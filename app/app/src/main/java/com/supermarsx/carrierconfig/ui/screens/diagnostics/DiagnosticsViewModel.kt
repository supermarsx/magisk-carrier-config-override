package com.supermarsx.carrierconfig.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarsx.carrierconfig.data.repository.ConnectivityTestRepository
import com.supermarsx.carrierconfig.data.repository.ConnectivityTestSuite
import com.supermarsx.carrierconfig.data.repository.DumpsysRepository
import com.supermarsx.carrierconfig.data.repository.DumpsysResult
import com.supermarsx.carrierconfig.data.repository.ExportRepository
import com.supermarsx.carrierconfig.data.repository.LogLevel
import com.supermarsx.carrierconfig.data.repository.LogcatEntry
import com.supermarsx.carrierconfig.data.repository.LogcatFilterType
import com.supermarsx.carrierconfig.data.repository.LogcatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val logcatRepository: LogcatRepository,
    private val dumpsysRepository: DumpsysRepository,
    private val connectivityTestRepository: ConnectivityTestRepository,
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()

    private val _logcatEntries = MutableStateFlow<List<LogcatEntry>>(emptyList())
    val logcatEntries: StateFlow<List<LogcatEntry>> = _logcatEntries.asStateFlow()

    private var liveLogcatJob: Job? = null

    init {
        loadLogcatSnapshot()
    }

    fun setLogcatFilter(filterType: LogcatFilterType) {
        _state.update { it.copy(logcatFilterType = filterType) }
        loadLogcatSnapshot()
    }

    fun setLogLevel(level: LogLevel) {
        _state.update { it.copy(logLevel = level) }
        loadLogcatSnapshot()
    }

    fun startLiveLogcat() {
        if (liveLogcatJob?.isActive == true) return
        _state.update { it.copy(isLiveLogging = true) }

        liveLogcatJob = viewModelScope.launch {
            logcatRepository.monitorLogcat(
                filterType = _state.value.logcatFilterType,
                minLevel = _state.value.logLevel
            ).collect { entry ->
                if (!_state.value.isLiveLogging) return@collect
                _logcatEntries.update { current -> (current + entry).takeLast(1000) }
            }
        }
    }

    fun stopLiveLogcat() {
        _state.update { it.copy(isLiveLogging = false) }
        liveLogcatJob?.cancel()
        liveLogcatJob = null
    }

    fun loadLogcatSnapshot() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLogs = true) }
            val entries = logcatRepository.getLogcatSnapshot(
                filterType = _state.value.logcatFilterType,
                lineCount = 500
            )
            _logcatEntries.value = entries
            _state.update { it.copy(isLoadingLogs = false) }
        }
    }

    fun clearLogcat() {
        viewModelScope.launch {
            logcatRepository.clearLogcat()
            _logcatEntries.value = emptyList()
            _state.update { it.copy(message = "Logcat cleared") }
        }
    }

    fun loadDumpsys(service: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDumpsys = true, selectedDumpsysService = service) }
            val result = when (service) {
                "ims" -> dumpsysRepository.getDumpsysIms()
                "phone" -> dumpsysRepository.getDumpsysPhone()
                "carrier_config" -> dumpsysRepository.getDumpsysCarrierConfig()
                "telecom" -> dumpsysRepository.getDumpsysTelecom()
                "connectivity" -> dumpsysRepository.getDumpsysConnectivity()
                else -> dumpsysRepository.getDumpsysIms()
            }
            _state.update { it.copy(isLoadingDumpsys = false, dumpsysResult = result) }
        }
    }

    fun loadAllDumpsys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDumpsys = true) }
            val results = dumpsysRepository.getAllDumpsys()
            _state.update { it.copy(isLoadingDumpsys = false, allDumpsysResults = results) }
        }
    }

    fun runConnectivityTests() {
        viewModelScope.launch {
            _state.update { it.copy(isRunningTests = true) }
            val testSuite = connectivityTestRepository.runFullTestSuite()
            _state.update { it.copy(isRunningTests = false, testResults = testSuite) }
        }
    }

    fun runSingleTest(testName: String) {
        viewModelScope.launch {
            _state.update { it.copy(message = "Single test not yet wired: $testName") }
        }
    }

    fun exportDiagnostics() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            try {
                val deviceInfo = buildString {
                    appendLine("Model: ${android.os.Build.MODEL}")
                    appendLine("Manufacturer: ${android.os.Build.MANUFACTURER}")
                    appendLine("Android: ${android.os.Build.VERSION.RELEASE}")
                    appendLine("SDK: ${android.os.Build.VERSION.SDK_INT}")
                }

                val simInfo = "SIM diagnostics loaded: ${_state.value.dumpsysResult != null}"
                val imsStatus = when (val result = _state.value.dumpsysResult) {
                    is DumpsysResult.Success -> result.output
                    is DumpsysResult.Error -> "Error: ${result.message}"
                    null -> "Not loaded"
                }

                when (val result = exportRepository.exportDiagnostics(deviceInfo, simInfo, imsStatus)) {
                    is com.supermarsx.carrierconfig.data.repository.ExportResult.Success -> {
                        _state.update {
                            it.copy(
                                isExporting = false,
                                message = "Diagnostics exported to:\n${result.filePath}"
                            )
                        }
                    }
                    is com.supermarsx.carrierconfig.data.repository.ExportResult.Error -> {
                        _state.update { it.copy(isExporting = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, error = "Export failed: ${e.message}") }
            }
        }
    }

    fun exportLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            try {
                val logsText = _logcatEntries.value.joinToString("\n") { entry ->
                    "${entry.timestamp} ${entry.pid}/${entry.tid} ${entry.level} ${entry.tag}: ${entry.message}"
                }
                when (val result = exportRepository.exportTextFile("logcat_export", "txt", logsText)) {
                    is com.supermarsx.carrierconfig.data.repository.ExportResult.Success -> {
                        _state.update {
                            it.copy(isExporting = false, message = "Logs exported to:\n${result.filePath}")
                        }
                    }
                    is com.supermarsx.carrierconfig.data.repository.ExportResult.Error -> {
                        _state.update { it.copy(isExporting = false, error = "Export failed: ${result.message}") }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, error = "Failed to export logs: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }

    fun setSelectedTab(tab: Int) {
        _state.update { it.copy(selectedTab = tab) }
    }
}

data class DiagnosticsState(
    val selectedTab: Int = 0,
    val logcatFilterType: LogcatFilterType = LogcatFilterType.ALL,
    val logLevel: LogLevel = LogLevel.DEBUG,
    val isLiveLogging: Boolean = false,
    val isLoadingLogs: Boolean = false,
    val selectedDumpsysService: String = "ims",
    val dumpsysResult: DumpsysResult? = null,
    val allDumpsysResults: Map<String, DumpsysResult>? = null,
    val isLoadingDumpsys: Boolean = false,
    val testResults: ConnectivityTestSuite? = null,
    val isRunningTests: Boolean = false,
    val isExporting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
