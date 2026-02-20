package com.svtt.carrierconfig.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svtt.carrierconfig.data.model.DashboardState
import com.svtt.carrierconfig.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import com.svtt.carrierconfig.data.repository.ExportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val simInfoRepository: SimInfoRepository,
    private val imsStatusRepository: ImsStatusRepository,
    private val wfcUiStatusRepository: WfcUiStatusRepository,
    private val blockerDetectionService: BlockerDetectionService,
    private val diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                Timber.d("Loading dashboard data...")
                
                // Load device info
                val deviceInfo = deviceInfoRepository.getDeviceInfo()
                _state.value = _state.value.copy(deviceInfo = deviceInfo)
                
                // Load SIM info
                val simInfo = simInfoRepository.getSimInfo()
                _state.value = _state.value.copy(simInfo = simInfo)
                
                // Load IMS status
                val imsStatus = imsStatusRepository.getImsStatus()
                _state.value = _state.value.copy(imsStatus = imsStatus)
                
                // Load WFC UI status
                val wfcUiStatus = wfcUiStatusRepository.getWfcUiStatus()
                _state.value = _state.value.copy(wfcUiStatus = wfcUiStatus)
                
                // Detect blocker
                val blockerAnalysis = blockerDetectionService.detectBlocker(
                    imsStatus = imsStatus,
                    wfcUiStatus = wfcUiStatus,
                    simInfo = simInfo
                )
                _state.value = _state.value.copy(
                    blockerAnalysis = blockerAnalysis,
                    isLoading = false
                )
                
                Timber.d("Dashboard loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load dashboard")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard: ${e.message}"
                )
            }
        }
    }
    
    fun refresh() {
        loadDashboard()
    }
    
    suspend fun exportReport(): ExportResult {
        return try {
            val currentState = _state.value
            val report = diagnosticsRepository.generateReport(
                deviceInfo = currentState.deviceInfo,
                simInfo = currentState.simInfo,
                imsStatus = currentState.imsStatus,
                wfcUiStatus = currentState.wfcUiStatus,
                blockerAnalysis = currentState.blockerAnalysis
            )
            diagnosticsRepository.exportReport(report)
        } catch (e: Exception) {
            Timber.e(e, "Failed to export report")
            ExportResult(
                success = false,
                error = "Failed to export: ${e.message}"
            )
        }
    }
}
