package dev.mars.carrierconfig.ui.screens.entitlement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarx.carrierconfig.ui.components.*
import com.supermarx.carrierconfig.ui.theme.*
import com.supermarx.carrierconfig.util.CreateFileContract
import com.supermarx.carrierconfig.util.PickConfigFileContract

/**
 * Runtime Entitlement Screen
 * 
 * Manages Frida and LSPosed instrumentation sessions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntitlementScreen(
    viewModel: EntitlementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }
    
    // File pickers
    val importLauncher = rememberLauncherForActivityResult(
        contract = PickConfigFileContract()
    ) { uri: Uri? ->
        uri?.let { viewModel.importProfile(context, it) }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = CreateFileContract("application/json", "profile.json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportSelectedProfile(context, it) }
    }
    
    // Show messages
    LaunchedEffect(state.message, state.error) {
        state.message?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let { 
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Runtime Entitlement",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                ),
                actions = {
                    // Import button
                    IconButton(onClick = { importLauncher.launch(Unit) }) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Import Profile",
                            tint = TextPrimary
                        )
                    }
                    
                    // Export button
                    Box {
                        IconButton(
                            onClick = { showExportMenu = true },
                            enabled = state.selectedProfile != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export Profile",
                                tint = if (state.selectedProfile != null) TextPrimary else TextTertiary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Selected Profile") },
                                onClick = {
                                    showExportMenu = false
                                    exportLauncher.launch("${state.selectedProfile?.id ?: "profile"}.json")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                }
                            )
                        }
                    }
                    
                    // Refresh button
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientTop,
                            GradientMiddle,
                            GradientBottom
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Backend Selection
                item {
                    Text(
                        text = "Select Backend",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                
                item {
                    BackendSelector(
                        selectedBackend = state.selectedBackend,
                        onSelect = { viewModel.selectBackend(it) }
                    )
                }
                
                // Status Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusCard(state)
                }
                
                // Frida Backend
                if (state.selectedBackend == InstrumentationBackend.Frida) {
                    item {
                        FridaControls(state, viewModel)
                    }
                }
                
                // LSPosed Backend
                if (state.selectedBackend == InstrumentationBackend.LSPosed) {
                    item {
                        LSPosedInfo(state)
                    }
                }
                
                // Profile Selection
                if (state.profiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "Hook Profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    items(state.profiles) { profile ->
                        ProfileCard(
                            profile = profile,
                            isSelected = state.selectedProfile == profile,
                            onClick = { viewModel.selectProfile(profile) }
                        )
                    }
                }
            }
            
            // Loading Overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            }
        }
    }
}

@Composable
private fun BackendSelector(
    selectedBackend: InstrumentationBackend,
    onSelect: (InstrumentationBackend) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(
            modifier = Modifier.weight(1f),
            variant = if (selectedBackend == InstrumentationBackend.Frida) 
                GlassCardVariant.Emphasized else GlassCardVariant.Default,
            onClick = { onSelect(InstrumentationBackend.Frida) }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = if (selectedBackend == InstrumentationBackend.Frida) 
                        AccentPrimary else TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Frida",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Dynamic",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        GlassCard(
            modifier = Modifier.weight(1f),
            variant = if (selectedBackend == InstrumentationBackend.LSPosed) 
                GlassCardVariant.Emphasized else GlassCardVariant.Default,
            onClick = { onSelect(InstrumentationBackend.LSPosed) }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = if (selectedBackend == InstrumentationBackend.LSPosed) 
                        AccentPrimary else TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "LSPosed",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Persistent",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatusCard(state: EntitlementState) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        variant = GlassCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Session Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                
                StatusChip(
                    text = if (state.isSessionActive) "Active" else "Inactive",
                    status = if (state.isSessionActive) StatusType.Success else StatusType.Info
                )
            }
            
            if (state.selectedBackend == InstrumentationBackend.Frida) {
                Divider(color = GlassBorder, thickness = 1.dp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Frida Server:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = if (state.fridaStatus?.isRunning == true) "Running" else "Stopped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.fridaStatus?.isRunning == true) AccentSuccess else TextTertiary
                    )
                }
                
                if (state.fridaStatus?.version != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Version:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = state.fridaStatus.version,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FridaControls(state: EntitlementState, viewModel: EntitlementViewModel) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Frida Controls",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            if (state.fridaStatus?.isInstalled == false) {
                GlassButton(
                    onClick = { viewModel.installFridaServer() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Install Frida Server")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.fridaStatus?.isRunning == false) {
                        GlassButton(
                            onClick = { viewModel.startFridaServer() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Server")
                        }
                    } else {
                        GlassButton(
                            onClick = { viewModel.stopFridaServer() },
                            modifier = Modifier.weight(1f),
                            variant = GlassButtonVariant.Outlined
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Server")
                        }
                    }
                }
                
                if (state.fridaStatus?.isRunning == true && !state.isSessionActive) {
                    GlassButton(
                        onClick = { viewModel.startSession() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Instrumentation Session")
                    }
                } else if (state.isSessionActive) {
                    GlassButton(
                        onClick = { viewModel.stopSession() },
                        modifier = Modifier.fillMaxWidth(),
                        variant = GlassButtonVariant.Outlined
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Session")
                    }
                }
            }
        }
    }
}

@Composable
private fun LSPosedInfo(state: EntitlementState) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LSPosed Module",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            Text(
                text = "LSPosed module provides persistent hooks that remain active after reboot. Configure the module in LSPosed Manager.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = if (state.lsposedModuleActive) "Active" else "Install in LSPosed Manager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.lsposedModuleActive) AccentSuccess else AccentWarning
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: HookProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (isSelected) GlassCardVariant.Emphasized else GlassCardVariant.Default,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = profile.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// Placeholder types
enum class InstrumentationBackend {
    Frida, LSPosed
}

data class HookProfile(
    val id: String,
    val name: String,
    val description: String
)

data class FridaStatus(
    val isInstalled: Boolean,
    val isRunning: Boolean,
    val version: String?
)

data class EntitlementState(
    val isLoading: Boolean = false,
    val selectedBackend: InstrumentationBackend = InstrumentationBackend.Frida,
    val isSessionActive: Boolean = false,
    val fridaStatus: FridaStatus? = null,
    val lsposedModuleActive: Boolean = false,
    val profiles: List<HookProfile> = emptyList(),
    val selectedProfile: HookProfile? = null,
    val message: String? = null,
    val error: String? = null
)
