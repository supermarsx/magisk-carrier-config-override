package com.supermarx.carrierconfig.ui.screens.carrierconfig

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarx.carrierconfig.data.model.*
import com.supermarx.carrierconfig.ui.components.*
import com.supermarx.carrierconfig.ui.theme.*

/**
 * CarrierConfig override configuration screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierConfigScreen(
    viewModel: CarrierConfigViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var showXMLPreview by remember { mutableStateOf(false) }
    
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
                actions = {                    // XML Preview button
                    IconButton(
                        onClick = { showXMLPreview = true },
                        enabled = state.selectedPreset != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "View XML",
                            tint = if (state.selectedPreset != null) AccentPrimary else TextDisabled
                        )
                    }                    IconButton(onClick = { viewModel.checkPrerequisites() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
                        )
                    }
                }
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = state.currentTab,
                    containerColor = BackgroundDark.copy(alpha = 0.8f),
                    contentColor = TextPrimary
                ) {
                    Tab(
                        selected = state.currentTab == 0,
                        onClick = { viewModel.switchTab(0) },
                        text = { Text("Presets") }
                    )
                    Tab(
                        selected = state.currentTab == 1,
                        onClick = { viewModel.switchTab(1) },
                        text = { Text("Keys") }
                    )
                    Tab(
                        selected = state.currentT, onAddKey = { showAddKeyDialog = true })
                    2 -> DeployTab(state, viewModel)
                }
            }
            
            // Dialogs
            if (showAddKeyDialog) {
                AddKeyDialog(
                    onDismiss = { showAddKeyDialog = false },
                    onAdd = { key ->
                        viewModel.addCustomKey(key)
                        showAddKeyDialog = false
                    }
                )
            }
            
            if (showXMLPreview) {
                XMLPreviewDialog(
                    xml = viewModel.generateXMLPreview(),
                    onDismiss = { showXMLPreview = false }
                )       text = { Text("Deploy") }
                    )
                }
                
                // Tab Content
                when (state.currentTab) {
                    0 -> PresetsTab(state, viewModel)
                    1 -> KeysTab(state, viewModel)
                    2 -> DeployTab(state, viewModel)
                }
            }
            
            // Error Snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun PresetsTab(
    state: CarrierConfigState,
    viewModel: CarrierConfigViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Select a configuration preset",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(state.presets) { preset ->
            PresetCard(
                preset = preset,
                isSelected = state.selectedPreset?.id == preset.id,
                onClick = { viewModel.selectPreset(preset) }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: CarrierConfigPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        glassStrength = if (isSelected) GlassStrength.Strong else GlassStrength.Medium,
        borderColor = if (isSelected) AccentPrimary else GlassBorder,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    color = if (isSelected) AccentPrimary else TextPrimary
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = AccentPrimary
                    )
                }
            }
            
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Text(
                text = "Keys: ${prese,
    onAddKey: () -> Unit
) {
    val allKeys = remember(state.selectedPreset, state.customKeys) {
        viewModel.getSelectedKeys()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Configuration Keys (${allKeys.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            // Add custom key button
            IconButton(onClick = onAddKey) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom key",
                    tint = AccentPrimary
                )
            }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Configuration Keys (${allKeys.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            GlassButton(
                text = "Add Key",
                onClick = {
                    // Show dialog to add custom key
                    viewModel.showMessage("Custom key editor coming soon!")
                },
                modifier = Modifier.height(36.dp),
                containerColor = GlassSurface,
                contentColor = AccentCyan
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Keys List
        if (allKeys.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Select a preset to view keys",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allKeys) { key ->
                    KeyCard(key)
                }
            }
        }
    }
}

@Composable
private fun KeyCard(key: ConfigKey) {
    GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = key.key,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (key.isCustom) {
                    StatusChip(text = "CUSTOM", status = ChipStatus.Info)
                }
            }
            
            Text(
                text = key.value.displayValue,
                style = MaterialTheme.typography.bodySmall,
                color = AccentPrimary
            )
        }
    }
}

@Composable
private fun DeployTab(
    state: CarrierConfigState,
    viewModel: CarrierConfigViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Deployment",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        
        // Prerequisites Check
        PrerequisitesCard(state.prerequisites)
        
        // Deployment Status
        DeploymentStatusCard(state.deployment)
        
        // Actions
        if (state.prerequisites?.allMet == true) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(
                    text = "Deploy Override",
                    onClick = { viewModel.deploy() },
                    variant = ButtonVariant.Primary,
                    enabled = !state.isLoading && state.selectedPreset != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (state.deployment?.isDeployed == true) {
                    GlassButton(
                        text = "Revert Override",
                        onClick = { viewModel.revert() },
                        variant = ButtonVariant.Secondary,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Instructions
        GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Important Notes:",
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentWarning
                )
                Text(
                    "• Device reboot required for changes to take effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "• Original configuration is backed up automatically",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "• Use 'Revert' to restore original settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PrerequisitesCard(prerequisites: Prerequisites?) {
    GlassmorphicCard(glassStrength = GlassStrength.Medium) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Prerequisites",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            if (prerequisites == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AccentPrimary
                )
            } else {
                PrerequisiteItem("Root Access", prerequisites.hasRoot)
                PrerequisiteItem("Magisk", prerequisites.hasMagisk, prerequisites.magiskVersion)
                PrerequisiteItem("Config Path", prerequisites.carrierConfigPath != null, prerequisites.carrierConfigPath)
                PrerequisiteItem("Writable", prerequisites.pathWritable)
            }
        }
    }
}

@Composable
private fun PrerequisiteItem(
    label: String,
    met: Boolean,
    details: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            details?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
        }
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (met) AccentSuccess else AccentError
        )
    }
}

@Composable
private fun DeploymentStatusCard(deployment: CarrierConfigDeployment?) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium,
        borderColor = if (deployment?.isDeployed == true) AccentSuccess else GlassBorder
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Deployment Status",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                StatusChip(
                    text = if (deployment?.isDeployed == true) "DEPLOYED" else "NOT DEPLOYED",
                    status = if (deployment?.isDeployed == true) ChipStatus.Success else ChipStatus.Neutral
                )
            }
            
            deployment?.deploymentPath?.let {
                Text(
                    "Path: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            if (deployment?.backupExists == true) {
                Text(
                    "✓ Backup available",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentSuccess
                )
            }
        }
    }
}
