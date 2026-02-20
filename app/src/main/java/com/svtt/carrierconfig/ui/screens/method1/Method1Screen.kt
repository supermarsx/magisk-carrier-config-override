package com.svtt.carrierconfig.ui.screens.method1

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
import com.topjohnwu.superuser.Shell
import com.svtt.carrierconfig.data.model.*
import com.svtt.carrierconfig.ui.components.*
import com.svtt.carrierconfig.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Method1Screen(
    viewModel: Method1ViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val selectedKeys by viewModel.selectedKeys.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Method 1: CarrierConfig",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeepDark.copy(alpha = 0.8f)
                ),
                actions = {
                    GlassIconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
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
                        colors = listOf(GradientTop, BackgroundDeepDark, GradientBottom)
                    )
                )
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundDark.copy(alpha = 0.6f),
                    contentColor = AccentPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Presets",
                                color = if (selectedTab == 0) AccentPrimary else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Keys (${selectedKeys.size})",
                                color = if (selectedTab == 1) AccentPrimary else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Deploy",
                                color = if (selectedTab == 2) AccentPrimary else TextSecondary
                            )
                        }
                    )
                }
                
                // Tab Content
                when (selectedTab) {
                    0 -> PresetsTab(
                        presets = state.presets,
                        selectedPreset = state.selectedPreset,
                        onPresetSelected = { viewModel.selectPreset(it) }
                    )
                    1 -> KeysTab(
                        selectedKeys = selectedKeys,
                        customKeys = state.customKeys,
                        onAddKey = { viewModel.addCustomKey(it) },
                        onRemoveKey = { viewModel.removeCustomKey(it) },
                        onUpdateKey = { key, value -> viewModel.updateKeyValue(key, value) }
                    )
                    2 -> DeployTab(
                        state = state,
                        selectedKeys = selectedKeys,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetsTab(
    presets: List<CarrierConfigPreset>,
    selectedPreset: CarrierConfigPreset?,
    onPresetSelected: (CarrierConfigPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Select a preset to quickly configure Wi-Fi Calling",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(presets) { preset ->
            PresetCard(
                preset = preset,
                isSelected = preset.id == selectedPreset?.id,
                onSelect = { onPresetSelected(preset) }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: CarrierConfigPreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    GlassCardAccent(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (isSelected) AccentPrimary else Color.Transparent
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = AccentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Text(
                "${preset.keys.size} keys configured",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            
            GlassButtonSecondary(
                text = if (isSelected) "Selected" else "Select Preset",
                onClick = onSelect,
                enabled = !isSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun KeysTab(
    selectedKeys: List<CarrierConfigKey>,
    customKeys: List<CarrierConfigKey>,
    onAddKey: (CarrierConfigKey) -> Unit,
    onRemoveKey: (CarrierConfigKey) -> Unit,
    onUpdateKey: (CarrierConfigKey, Any) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Selected Keys (${selectedKeys.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (selectedKeys.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StatusInfo,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No keys selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            "Select a preset from the Presets tab or add custom keys",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        } else {
            items(selectedKeys) { key ->
                KeyCard(
                    key = key,
                    onRemove = { onRemoveKey(key) },
                    onUpdate = { value -> onUpdateKey(key, value) },
                    isCustom = customKeys.any { it.key == key.key }
                )
            }
        }
    }
}

@Composable
private fun KeyCard(
    key: CarrierConfigKey,
    onRemove: () -> Unit,
    onUpdate: (Any) -> Unit,
    isCustom: Boolean
) {
    GlassCardCompact {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        key.key,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = AccentPrimary
                    )
                    if (key.description.isNotEmpty()) {
                        Text(
                            key.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
                
                if (isCustom) {
                    GlassIconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = AccentError
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Type: ${key.type.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                
                Text(
                    "Value: ${key.value}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = AccentSuccess
                )
            }
        }
    }
}

@Composable
private fun DeployTab(
    state: Method1State,
    selectedKeys: List<CarrierConfigKey>,
    viewModel: Method1ViewModel
) {
    val scope = rememberCoroutineScope()
    var showRebootDialog by remember { mutableStateOf(false) }
    var deploymentResult by remember { mutableStateOf<DeploymentResult?>(null) }
    var isDeploying by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prerequisites Card
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Prerequisites",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    
                    PrerequisiteRow(
                        label = "Root Access",
                        isMet = state.isRootAvailable,
                        description = "Required to modify system files"
                    )
                    
                    PrerequisiteRow(
                        label = "Magisk Installed",
                        isMet = state.isMagiskInstalled,
                        description = "Required for persistent overrides"
                    )
                    
                    PrerequisiteRow(
                        label = "Keys Selected",
                        isMet = selectedKeys.isNotEmpty(),
                        description = "${selectedKeys.size} keys ready to deploy"
                    )
                    
                    if (state.detectedPaths.isNotEmpty()) {
                        PrerequisiteRow(
                            label = "Paths Detected",
                            isMet = true,
                            description = "${state.detectedPaths.size} potential override path(s)"
                        )
                    }
                }
            }
        }
        
        // Active Override Status
        if (state.activeOverride != null) {
            item {
                GlassCardAccent(accentColor = AccentSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentSuccess,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Override Active",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                "Changes will take effect after reboot",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        // Deployment Result
        deploymentResult?.let { result ->
            item {
                GlassCardAccent(
                    accentColor = if (result.success) AccentSuccess else AccentError
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success) AccentSuccess else AccentError
                            )
                            Text(
                                if (result.success) "Success!" else "Failed",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                        Text(
                            result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(
                    text = if (isDeploying) "Deploying..." else "Deploy Override",
                    onClick = {
                        scope.launch {
                            isDeploying = true
                            val result = viewModel.deployOverride(DeploymentConfig())
                            deploymentResult = result
                            isDeploying = false
                            if (result.success) {
                                showRebootDialog = true
                            }
                        }
                    },
                    enabled = !isDeploying && 
                             state.isRootAvailable && 
                             selectedKeys.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                )
                
                if (state.activeOverride != null) {
                    GlassButtonSecondary(
                        text = "Revert Override",
                        onClick = {
                            scope.launch {
                                val success = viewModel.revertOverride()
                                deploymentResult = DeploymentResult(
                                    success = success,
                                    message = if (success) "Override reverted successfully" 
                                             else "Failed to revert override"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = null,
                                tint = AccentPrimary
                            )
                        }
                    )
                }
                
                GlassButtonOutlined(
                    text = "Reboot Device",
                    onClick = { showRebootDialog = true },
                    enabled = state.isRootAvailable,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
            }
        }
        
        // Info Card
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StatusInfo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Important Notes",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                    }
                    Text(
                        "• Changes require a reboot to take effect\n" +
                        "• Overrides are persistent across reboots\n" +
                        "• You can revert at any time\n" +
                        "• Always create backups before modifying",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
    
    // Reboot Dialog
    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text("Reboot Required") },
            text = {
                Text("For changes to take effect, you need to reboot your device. Reboot now?")
            },
            confirmButton = {
                GlassButton(
                    text = "Reboot Now",
                    onClick = {
                        scope.launch {
                            Shell.cmd("reboot").exec()
                        }
                        showRebootDialog = false
                    }
                )
            },
            dismissButton = {
                GlassButtonOutlined(
                    text = "Later",
                    onClick = { showRebootDialog = false }
                )
            }
        )
    }
}

@Composable
private fun PrerequisiteRow(
    label: String,
    isMet: Boolean,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) AccentSuccess else AccentError,
            modifier = Modifier.size(24.dp)
        )
    }
}
