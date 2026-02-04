package com.supermarx.carrierconfig.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarx.carrierconfig.data.model.*
import com.supermarx.carrierconfig.ui.components.*
import com.supermarx.carrierconfig.ui.theme.*

/**
 * Main dashboard screen showing device status and WFC availability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "CCO Dashboard",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientTop,
                            BackgroundDeepDark,
                            GradientBottom
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingContent()
            } else if (state.error != null) {
                ErrorContent(error = state.error!!)
            } else {
                DashboardContent(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    viewModel: DashboardViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Root status banner
        RootStatusBanner(isRooted = state.deviceInfo?.isRooted ?: false)
        
        // Device info card
        state.deviceInfo?.let { DeviceInfoCard(it) }
        
        // SIM info cards
        if (state.simInfo.isNotEmpty()) {
            state.simInfo.forEach { sim ->
                SIMInfoCard(sim)
            }
        }
        
        // IMS status card
        state.imsStatus?.let { IMSStatusCard(it) }
        
        // WFC UI status card
        state.wfcUIStatus?.let { WFCStatusCard(it) }
        
        // Blocker indicator
        BlockerIndicatorCard(blocker = state.detectedBlocker)
        
        // Action buttons
        ActionButtons(
            onRunDiagnostics = { viewModel.runDiagnostics() },
            onOpenWFCSettings = { viewModel.openWFCSettings() },
            onExportReport = { viewModel.exportReport() }
        )
    }
}

@Composable
private fun RootStatusBanner(isRooted: Boolean) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium,
        borderColor = if (isRooted) AccentSuccess else AccentError
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isRooted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isRooted) AccentSuccess else AccentError
                )
                Text(
                    text = if (isRooted) "Root Access: Available" else "Root Access: Not Available",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            StatusChip(
                text = if (isRooted) "READY" else "LIMITED",
                status = if (isRooted) ChipStatus.Success else ChipStatus.Warning
            )
        }
    }
}

@Composable
private fun DeviceInfoCard(deviceInfo: DeviceInfo) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = AccentPrimary
                )
                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            
            Divider(color = GlassSurfaceMedium)
            
            InfoRow("Model", "${deviceInfo.manufacturer} ${deviceInfo.model}")
            InfoRow("Android", deviceInfo.androidVersion)
            deviceInfo.oneUIVersion?.let { InfoRow("One UI", it) }
            InfoRow("Build", deviceInfo.buildFingerprint)
            InfoRow("Security Patch", deviceInfo.securityPatch)
        }
    }
}

@Composable
private fun SIMInfoCard(simInfo: SIMInfo) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium,
        borderColor = if (simInfo.isActive) AccentPrimary else null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SdCard,
                        contentDescription = null,
                        tint = AccentPrimary
                    )
                    Text(
                        text = "SIM ${simInfo.slotIndex + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                StatusChip(
                    text = if (simInfo.isActive) "ACTIVE" else "INACTIVE",
                    status = if (simInfo.isActive) ChipStatus.Success else ChipStatus.Neutral
                )
            }
            
            Divider(color = GlassSurfaceMedium)
            
            InfoRow("Carrier", simInfo.carrierName ?: "Unknown")
            InfoRow("MCC/MNC", "${simInfo.mcc}/${simInfo.mnc}")
        }
    }
}

@Composable
private fun IMSStatusCard(imsStatus: IMSStatus) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = AccentPrimary
                )
                Text(
                    text = "IMS Status",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            
            Divider(color = GlassSurfaceMedium)
            
            StatusRow("IMS Registered", imsStatus.isRegistered)
            StatusRow("VoLTE Available", imsStatus.isVoLTEAvailable)
            StatusRow("VoWiFi Available", imsStatus.isVoWiFiAvailable)
            InfoRow("State", imsStatus.registrationState)
        }
    }
}

@Composable
private fun WFCStatusCard(wfcStatus: WFCUIStatus) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = AccentPrimary
                )
                Text(
                    text = "Wi-Fi Calling UI",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            
            Divider(color = GlassSurfaceMedium)
            
            StatusRow("Settings Activity", wfcStatus.settingsActivityExists)
            StatusRow("Page Populates", wfcStatus.pagePopulates)
            StatusRow("Toggle Present", wfcStatus.togglePresent)
        }
    }
}

@Composable
private fun BlockerIndicatorCard(blocker: WFCBlocker) {
    val (blockerText, blockerStatus) = when (blocker) {
        WFCBlocker.NONE -> "No Blocker Detected" to ChipStatus.Success
        WFCBlocker.IMS_NOT_REGISTERED -> "IMS Not Registered" to ChipStatus.Error
        WFCBlocker.CARRIER_CONFIG_GATE -> "CarrierConfig Gate" to ChipStatus.Warning
        WFCBlocker.CSC_GATE -> "CSC Gate Suspected" to ChipStatus.Warning
        WFCBlocker.ENTITLEMENT_GATE -> "Entitlement Gate" to ChipStatus.Warning
        WFCBlocker.SETTINGS_MISSING -> "Settings Missing" to ChipStatus.Error
        WFCBlocker.UNKNOWN -> "Unknown Blocker" to ChipStatus.Neutral
    }
    
    GlassmorphicCard(
        glassStrength = GlassStrength.Strong,
        borderColor = when (blockerStatus) {
            ChipStatus.Success -> AccentSuccess
            ChipStatus.Warning -> AccentWarning
            ChipStatus.Error -> AccentError
            else -> null
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Likely Blocker",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            StatusChip(text = blockerText, status = blockerStatus)
        }
    }
}

@Composable
private fun ActionButtons(
    onRunDiagnostics: () -> Unit,
    onOpenWFCSettings: () -> Unit,
    onExportReport: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassButton(
            text = "Run Diagnostic Scan",
            onClick = onRunDiagnostics,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Primary
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassButton(
                text = "Open WFC Settings",
                onClick = onOpenWFCSettings,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Outlined
            )
            
            GlassButton(
                text = "Export Report",
                onClick = onExportReport,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun StatusRow(label: String, value: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        StatusChip(
            text = if (value) "YES" else "NO",
            status = if (value) ChipStatus.Success else ChipStatus.Error
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentPrimary)
    }
}

@Composable
private fun ErrorContent(error: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassmorphicCard(
            glassStrength = GlassStrength.Medium,
            borderColor = AccentError
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = AccentError,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
