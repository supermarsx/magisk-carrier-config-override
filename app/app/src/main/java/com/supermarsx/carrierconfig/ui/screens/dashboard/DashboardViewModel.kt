package com.supermarsx.carrierconfig.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.supermarsx.carrierconfig.data.model.DashboardState
import com.supermarsx.carrierconfig.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the dashboard screen
 * Manages device status, IMS status, and blocker detection
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Load device info
                val deviceInfo = deviceRepository.getDeviceInfo()
                _state.value = _state.value.copy(deviceInfo = deviceInfo)
                
                // Load SIM info
                val simInfo = deviceRepository.getSIMInfo()
                _state.value = _state.value.copy(simInfo = simInfo)
                
                // Load IMS status
                val imsStatus = deviceRepository.getIMSStatus()
                _state.value = _state.value.copy(imsStatus = imsStatus)
                
                // Load WFC UI status
                val wfcStatus = deviceRepository.getWFCUIStatus()
                _state.value = _state.value.copy(wfcUIStatus = wfcStatus)
                
                // Detect blocker
                val blocker = deviceRepository.detectBlocker(
                    imsStatus = imsStatus,
                    wfcStatus = wfcStatus
                )
                _state.value = _state.value.copy(
                    detectedBlocker = blocker,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }
    
    fun runDiagnostics() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Reload all dashboard data for fresh diagnostics
                loadDashboardData()
                
                // Run additional checks
                val deviceInfo = deviceRepository.getDeviceInfo()
                val simInfo = deviceRepository.getSIMInfo()
                val imsStatus = deviceRepository.getIMSStatus()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    deviceInfo = deviceInfo,
                    simInfo = simInfo,
                    imsStatus = imsStatus
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Diagnostics scan failed: ${e.message}"
                )
            }
        }
    }
    
    fun openWFCSettings() {
        viewModelScope.launch {
            deviceRepository.openWiFiCallingSettings()
        }
    }
    
    fun exportReport() {
        viewModelScope.launch {
            deviceRepository.exportReport(_state.value)
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
}
