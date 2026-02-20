package dev.mars.carrierconfig.ui.screens.carrierconfig

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
import com.supermarx.carrierconfig.data.model.CarrierConfigPreset
import com.supermarx.carrierconfig.ui.components.*
import com.supermarx.carrierconfig.util.CreateFileContract
import com.supermarx.carrierconfig.util.PickConfigFileContract
import dev.mars.carrierconfig.ui.theme.*

/**
 * CarrierConfig Override Screen
 * 
 * Allows users to select and deploy CarrierConfig presets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierConfigScreen(
    viewModel: CarrierConfigViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }
    
    // File pickers
    val importLauncher = rememberLauncherForActivityResult(
        contract = PickConfigFileContract()
    ) { uri: Uri? ->
        uri?.let { viewModel.importPreset(context, it) }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = CreateFileContract("application/json", "preset.json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportSelectedPreset(context, it) }
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
                        "CarrierConfig Override",
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
                            contentDescription = "Import Preset",
                            tint = TextPrimary
                        )
                    }
                    
                    // Export button
                    Box {
                        IconButton(
                            onClick = { showExportMenu = true },
                            enabled = state.selectedPreset != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export Preset",
                                tint = if (state.selectedPreset != null) TextPrimary else TextTertiary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Selected Preset") },
                                onClick = {
                                    showExportMenu = false
                                    exportLauncher.launch("${state.selectedPreset?.id ?: "preset"}.json")
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
                // Status Card
                item {
                    StatusCard(state)
                }
                
                // Presets Section
                item {
                    Text(
                        text = "Available Presets",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                
                items(state.presets) { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = state.selectedPreset == preset,
                        isDeployed = state.deployedPresetId == preset.id,
                        onSelect = { viewModel.selectPreset(preset) },
                        onDeploy = { viewModel.deployPreset(preset) }
                    )
                }
                
                // Revert Button
                if (state.isOverrideActive) {
                    item {
                        GlassButton(
                            onClick = { viewModel.revertOverride() },
                            modifier = Modifier.fillMaxWidth(),
                            variant = GlassButtonVariant.Outlined
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Revert to Original Config")
                        }
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
private fun StatusCard(state: CarrierConfigState) {
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
                    text = "Override Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                
                StatusChip(
                    text = if (state.isOverrideActive) "Active" else "Inactive",
                    status = if (state.isOverrideActive) StatusType.Success else StatusType.Info
                )
            }
            
            if (state.isOverrideActive && state.deployedPresetId != null) {
                Divider(color = GlassBorder, thickness = 1.dp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Active Preset:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = state.deployedPresetId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentPrimary
                    )
                }
            }
            
            if (state.currentPath != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Config Path:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = state.currentPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: CarrierConfigPreset,
    isSelected: Boolean,
    isDeployed: Boolean,
    onSelect: () -> Unit,
    onDeploy: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (isSelected) GlassCardVariant.Emphasized else GlassCardVariant.Default,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                
                if (isDeployed) {
                    StatusChip(
                        text = "Deployed",
                        status = StatusType.Success
                    )
                }
            }
            
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            // Key count
            Text(
                text = "${preset.overrides.size} configuration keys",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            
            if (isSelected && !isDeployed) {
                Spacer(modifier = Modifier.height(8.dp))
                
                GlassButton(
                    onClick = onDeploy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deploy Preset")
                }
            }
        }
    }
}

// State and ViewModel placeholder
data class CarrierConfigState(
    val isLoading: Boolean = false,
    val isOverrideActive: Boolean = false,
    val currentPath: String? = null,
    val deployedPresetId: String? = null,
    val presets: List<CarrierConfigPreset> = emptyList(),
    val selectedPreset: CarrierConfigPreset? = null,
    val message: String? = null,
    val error: String? = null
)
