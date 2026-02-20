package dev.mars.carrierconfig.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mars.carrierconfig.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Diagnostics screen
 */
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
    
    init {
        // Load initial logcat snapshot
        loadLogcatSnapshot()
    }
    
    // =========================================================================
    // Logcat Tab
    // =========================================================================
    
    fun setLogcatFilter(filterType: LogcatFilterType) {
        _state.update { it.copy(logcatFilterType = filterType) }
        loadLogcatSnapshot()
    }
    
    fun setLogLevel(level: LogLevel) {
        _state.update { it.copy(logLevel = level) }
        loadLogcatSnapshot()
    }
    
    fun startLiveLogcat() {
        viewModelScope.launch {
            _state.update { it.copy(isLiveLogging = true) }
            
            logcatRepository.monitorLogcat(
                filterType = _state.value.logcatFilterType,
                minLevel = _state.value.logLevel
            ).collect { entry ->
                _logcatEntries.update { entries ->
                    (entries + entry).takeLast(1000) // Keep last 1000 entries
                }
            }
        }
    }
    
    fun stopLiveLogcat() {
        _state.update { it.copy(isLiveLogging = false) }
        // The flow collection will be cancelled automatically
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
    
    // =========================================================================
    // Dumpsys Tab
    // =========================================================================
    
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
            
            _state.update { 
                it.copy(
                    isLoadingDumpsys = false,
                    dumpsysResult = result
                )
            }
        }
    }
    
    fun loadAllDumpsys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDumpsys = true) }
            
            val results = dumpsysRepository.getAllDumpsys()
            
            _state.update { 
                it.copy(
                    isLoadingDumpsys = false,
                    allDumpsysResults = results
                )
            }
        }
    }
    
    // =========================================================================
    // Tests Tab
    // =========================================================================
    
    fun runConnectivityTests() {
        viewModelScope.launch {
            _state.update { it.copy(isRunningTests = true) }
            
            val testSuite = connectivityTestRepository.runFullTestSuite()
            
            _state.update { 
                it.copy(
                    isRunningTests = false,
                    testResults = testSuite
                )
            }
        }
    }
    
    fun runSingleTest(testName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isRunningTests = true) }
            
            // Run test based on name
            // This would trigger individual tests
            
            _state.update { it.copy(isRunningTests = false) }
        }
    }
    
    // =========================================================================
    // Export Functionality
    // =========================================================================
    
    fun exportDiagnostics() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            
            try {
                // Gather all diagnostic information
                val deviceInfo = buildString {
                    appendLine("Device Information:")
                    appendLine("Model: ${android.os.Build.MODEL}")
                    appendLine("Manufacturer: ${android.os.Build.MANUFACTURER}")
                    appendLine("Android: ${android.os.Build.VERSION.RELEASE}")
                    appendLine("SDK: ${android.os.Build.VERSION.SDK_INT}")
                }
                
                val simInfo = "SIM Status: ${if (state.value.dumpsysResult != null) "Available" else "Unknown"}"
                
                val imsStatus = state.value.dumpsysResult?.let { 
                    if (it is DumpsysResult.Success) it.output else "N/A" 
                } ?: "Not loaded"
                
                val result = exportRepository.exportDiagnostics(
                    deviceInfo = deviceInfo,
                    simInfo = simInfo,
                    imsStatus = imsStatus
                )
            
            when (result) {
                is dev.mars.carrierconfig.data.repository.ExportResult.Success -> {
                    _state.update { 
                        it.copy(
                            isExporting = false,
                            message = "Diagnostics exported to:\n${result.filePath}"
                        )
                    }
                }
                is dev.mars.carrierconfig.data.repository.ExportResult.Error -> {
                    _state.update { 
                        it.copy(
                            isExporting = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun exportLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            
            try {
                // Export current logcat entries
                val logsText = _logcatEntries.value.joinToString("\n") { entry ->
                    "${entry.timestamp} ${entry.pid}/${entry.tid} ${entry.level} ${entry.tag}: ${entry.message}"
                }
                
                // Save to file in exports directory
                val timestamp = System.currentTimeMillis()
                val fileName = "logcat_export_$timestamp.txt"
                val result = exportRepository.exportToFile(
                    fileName = fileName,
                    content = logsText,
                    mimeType = "text/plain"
                )
                
                when (result) {
                    is ExportResult.Success -> {
                        _state.update { 
                            it.copy(
                                isExporting = false,
                                successMessage = "Logs exported to: ${result.filePath}"
                            )
                        }
                    }
                    is ExportResult.Error -> {
                        _state.update { 
                            it.copy(
                                isExporting = false,
                                error = "Export failed: ${result.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isExporting = false,
                        error = "Failed to export logs: ${e.message}"
                    )
                }
            }
        }
    }
            
            _state.update { 
                it.copy(
                    isExporting = false,
                    message = "Logs exported successfully"
                )
            }
        }
    }
    
    // =========================================================================
    // Utility
    // =========================================================================
    
    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
    
    fun setSelectedTab(tab: Int) {
        _state.update { it.copy(selectedTab = tab) }
    }
}

/**
 * Diagnostics screen state
 */
data class DiagnosticsState(
    // Tab selection
    val selectedTab: Int = 0,
    
    // Logcat state
    val logcatFilterType: LogcatFilterType = LogcatFilterType.ALL,
    val logLevel: LogLevel = LogLevel.DEBUG,
    val isLiveLogging: Boolean = false,
    val isLoadingLogs: Boolean = false,
    
    // Dumpsys state
    val selectedDumpsysService: String = "ims",
    val dumpsysResult: DumpsysResult? = null,
    val allDumpsysResults: Map<String, DumpsysResult>? = null,
    val isLoadingDumpsys: Boolean = false,
    
    // Tests state
    val testResults: ConnectivityTestSuite? = null,
    val isRunningTests: Boolean = false,
    
    // Export state
    val isExporting: Boolean = false,
    
    // UI state
    val message: String? = null,
    val error: String? = null
)
