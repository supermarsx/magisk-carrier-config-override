package com.svtt.carrierconfig.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.svtt.carrierconfig.data.model.*
import com.svtt.carrierconfig.ui.components.*
import com.svtt.carrierconfig.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToMethod1: () -> Unit = {},
    onNavigateToMethod2: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onOpenWfcSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var exportResult by remember { mutableStateOf<com.svtt.carrierconfig.data.repository.ExportResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeepDark.copy(alpha = 0.8f)
                ),
                actions = {
                    GlassIconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (state.isLoading) TextDisabled else TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
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
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingView()
            } else if (state.error != null) {
                ErrorView(error = state.error!!, onRetry = { viewModel.refresh() })
            } else {
                DashboardContent(
                    state = state,
                    onOpenWfcSettings = onOpenWfcSettings,
                    onRunDiagnostics = onNavigateToDiagnostics,
                    onExportReport = { /* TODO */ },
                    onNavigateToMethod
                        scope.launch {
                            exportResult = viewModel.exportReport()
                        }
                    },
                    onNavigateToMethod1 = onNavigateToMethod1,
                    onNavigateToMethod2 = onNavigateToMethod2
                )
                
                // Show export result dialog
                exportResult?.let { result ->
                    AlertDialog(
                        onDismissRequest = { exportResult = null },
                        title = {
                            Text(if (result.success) "Export Successful" else "Export Failed")
                        },
                        text = {
                            Text(
                                if (result.success) {
                                    "Report exported to:\n${result.path}"
                                } else {
                                    result.error ?: "Unknown error"
                                }
                            )
                        },
                        confirmButton = {
                            GlassButton(
                                text = "OK",
                                onClick = { exportResult = null }
                            )
                        }
                    )
                }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    onOpenWfcSettings: () -> Unit,
    onRunDiagnostics: () -> Unit,
    onExportReport: () -> Unit,
    onNavigateToMethod1: () -> Unit,
    onNavigateToMethod2: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Info Card
        state.deviceInfo?.let { deviceInfo ->
            DeviceInfoCard(deviceInfo = deviceInfo)
        }
        
        // SIM Info Cards
        if (state.simInfo.isNotEmpty()) {
            state.simInfo.forEach { simInfo ->
                SimInfoCard(simInfo = simInfo)
            }
        } else {
            NoSimCard()
        }
        
        // IMS Status Card
        state.imsStatus?.let { imsStatus ->
            ImsStatusCard(imsStatus = imsStatus)
        }
        
        // WFC UI Status Card
        state.wfcUiStatus?.let { wfcUiStatus ->
            WfcUiStatusCard(wfcUiStatus = wfcUiStatus)
        }
        
        // Blocker Analysis Card
        state.blockerAnalysis?.let { blockerAnalysis ->
            BlockerAnalysisCard(
                blockerAnalysis = blockerAnalysis,
                onNavigateToMethod1 = onNavigateToMethod1,
                onNavigateToMethod2 = onNavigateToMethod2
            )
        }
        
        // Action Buttons
        ActionButtons(
            onRunDiagnostics = onRunDiagnostics,
            onOpenWfcSettings = onOpenWfcSettings,
            onExportReport = onExportReport,
            wfcUiExists = state.wfcUiStatus?.settingsActivityExists == true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DeviceInfoCard(deviceInfo: DeviceInfo) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Device Info",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            InfoRow("Model", deviceInfo.model)
            InfoRow("Manufacturer", deviceInfo.manufacturer)
            InfoRow("One UI", deviceInfo.oneUiVersion)
            InfoRow("Android", "${deviceInfo.androidVersion} (SDK ${deviceInfo.sdkVersion})")
            InfoRow("Root Access", if (deviceInfo.isRooted) "✓ Available" else "✗ Not Available")
        }
    }
}

@Composable
private fun SimInfoCard(simInfo: SimInfo) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SimCard,
                    contentDescription = null,
                    tint = AccentSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SIM ${simInfo.slot + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            InfoRow("Carrier", simInfo.carrierName)
            InfoRow("MCC/MNC", "${simInfo.mcc}/${simInfo.mnc}")
            InfoRow("Network Type", simInfo.networkType)
            InfoRow("State", simInfo.simState)
            if (simInfo.isRoaming) {
                StatusChip(
                    text = "Roaming",
                    type = StatusChipType.WARNING
                )
            }
        }
    }
}

@Composable
private fun NoSimCard() {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusWarning,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "No SIM card detected",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ImsStatusCard(imsStatus: ImsStatus) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NetworkCell,
                    contentDescription = null,
                    tint = AccentSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "IMS Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BooleanStatusChip("Registered", imsStatus.isRegistered)
                BooleanStatusChip("VoLTE", imsStatus.isVoLteAvailable)
                BooleanStatusChip("VoWiFi", imsStatus.isVoWiFiAvailable)
            }
            
            imsStatus.registrationTech?.let {
                InfoRow("Technology", it)
            }
        }
    }
}

@Composable
private fun WfcUiStatusCard(wfcUiStatus: WfcUiStatus) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = AccentInfo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "WFC UI Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            Divider(color = Color.White.copy(alpha = 0.1f))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BooleanStatusChip("Settings Activity", wfcUiStatus.settingsActivityExists)
                BooleanStatusChip("Page Populates", wfcUiStatus.pagePopulates)
                BooleanStatusChip("Toggle Present", wfcUiStatus.togglePresent)
            }
        }
    }
}

@Composable
private fun BlockerAnalysisCard(
    blockerAnalysis: BlockerAnalysis,
    onNavigateToMethod1: () -> Unit,
    onNavigateToMethod2: () -> Unit
) {
    val (color, chipType) = when (blockerAnalysis.blockerType) {
        BlockerType.NONE -> AccentSuccess to StatusChipType.SUCCESS
        BlockerType.IMS_NOT_REGISTERED -> AccentError to StatusChipType.ERROR
        BlockerType.CARRIER_CONFIG -> AccentWarning to StatusChipType.WARNING
        BlockerType.CSC_ENTITLEMENT -> AccentWarning to StatusChipType.WARNING
        BlockerType.ENTITLEMENT -> AccentWarning to StatusChipType.WARNING
        BlockerType.MULTIPLE -> AccentError to StatusChipType.ERROR
        BlockerType.UNKNOWN -> StatusInactive to StatusChipType.INACTIVE
    }
    
    GlassCardAccent(accentColor = color) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            
            StatusChip(
                text = blockerAnalysis.blockerType.name.replace("_", " "),
                type = chipType
            )
            
            Text(
                blockerAnalysis.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Text(
                "💡 ${blockerAnalysis.suggestedAction}",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            
            // Quick action buttons based on blocker type
            when (blockerAnalysis.blockerType) {
                BlockerType.CARRIER_CONFIG -> {
                    GlassButtonSecondary(
                        text = "Try Method 1",
                        onClick = onNavigateToMethod1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BlockerType.CSC_ENTITLEMENT, BlockerType.ENTITLEMENT -> {
                    GlassButtonSecondary(
                        text = "Try Method 2",
                        onClick = onNavigateToMethod2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onRunDiagnostics: () -> Unit,
    onOpenWfcSettings: () -> Unit,
    onExportReport: () -> Unit,
    wfcUiExists: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassButton(
            text = "Run Diagnostic Scan",
            onClick = onRunDiagnostics,
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        GlassButtonSecondary(
            text = "Open Wi-Fi Calling Settings",
            onClick = onOpenWfcSettings,
            enabled = wfcUiExists,
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (wfcUiExists) AccentPrimary else TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        GlassButtonOutlined(
            text = "Export Report",
            onClick = onExportReport,
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentPrimary)
    }
}

@Composable
private fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = AccentError,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            error,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        GlassButton(text = "Retry", onClick = onRetry)
    }
}
