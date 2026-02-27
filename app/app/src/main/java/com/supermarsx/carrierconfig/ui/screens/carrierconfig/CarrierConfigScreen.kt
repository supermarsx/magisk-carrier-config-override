package com.supermarsx.carrierconfig.ui.screens.carrierconfig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarsx.carrierconfig.data.model.CarrierConfigDeployment
import com.supermarsx.carrierconfig.data.model.CarrierConfigPreset
import com.supermarsx.carrierconfig.data.model.CarrierConfigState
import com.supermarsx.carrierconfig.data.model.ConfigKey
import com.supermarsx.carrierconfig.data.model.Prerequisites
import com.supermarsx.carrierconfig.ui.components.AddKeyDialog
import com.supermarsx.carrierconfig.ui.components.ButtonVariant
import com.supermarsx.carrierconfig.ui.components.ChipStatus
import com.supermarsx.carrierconfig.ui.components.GlassButton
import com.supermarsx.carrierconfig.ui.components.GlassStrength
import com.supermarsx.carrierconfig.ui.components.GlassmorphicCard
import com.supermarsx.carrierconfig.ui.components.StatusChip
import com.supermarsx.carrierconfig.ui.components.XMLPreviewDialog
import com.supermarsx.carrierconfig.ui.theme.AccentError
import com.supermarsx.carrierconfig.ui.theme.AccentPrimary
import com.supermarsx.carrierconfig.ui.theme.AccentSuccess
import com.supermarsx.carrierconfig.ui.theme.AccentWarning
import com.supermarsx.carrierconfig.ui.theme.BackgroundDark
import com.supermarsx.carrierconfig.ui.theme.BackgroundDeepDark
import com.supermarsx.carrierconfig.ui.theme.GlassBorder
import com.supermarsx.carrierconfig.ui.theme.GradientBottom
import com.supermarsx.carrierconfig.ui.theme.GradientTop
import com.supermarsx.carrierconfig.ui.theme.TextDisabled
import com.supermarsx.carrierconfig.ui.theme.TextPrimary
import com.supermarsx.carrierconfig.ui.theme.TextSecondary

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
                title = { Text("CarrierConfig Override") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { showXMLPreview = true },
                        enabled = state.selectedPreset != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Preview XML",
                            tint = if (state.selectedPreset != null) AccentPrimary else TextDisabled
                        )
                    }
                    IconButton(onClick = viewModel::checkPrerequisites) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh checks",
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
                        colors = listOf(GradientTop, BackgroundDeepDark, GradientBottom)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    LinearLoadingBanner()
                }

                TabRow(
                    selectedTabIndex = state.currentTab,
                    containerColor = BackgroundDark.copy(alpha = 0.85f),
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
                        selected = state.currentTab == 2,
                        onClick = { viewModel.switchTab(2) },
                        text = { Text("Deploy") }
                    )
                }

                when (state.currentTab) {
                    0 -> PresetsTab(state, viewModel)
                    1 -> KeysTab(state, viewModel, onAddKey = { showAddKeyDialog = true })
                    else -> DeployTab(state, viewModel)
                }
            }

            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    if (showAddKeyDialog) {
        AddKeyDialog(
            onDismiss = { showAddKeyDialog = false },
            onAdd = {
                viewModel.addCustomKey(it)
                showAddKeyDialog = false
            }
        )
    }

    if (showXMLPreview) {
        XMLPreviewDialog(
            xml = viewModel.generateXMLPreview(),
            onDismiss = { showXMLPreview = false }
        )
    }
}

@Composable
private fun LinearLoadingBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp), strokeWidth = 2.dp)
            Text("Working...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
        items(state.presets) { preset ->
            PresetCard(
                preset = preset,
                selected = state.selectedPreset?.id == preset.id,
                onClick = { viewModel.selectPreset(preset) }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: CarrierConfigPreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        glassStrength = if (selected) GlassStrength.Strong else GlassStrength.Medium,
        borderColor = if (selected) AccentPrimary else GlassBorder
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) AccentPrimary else TextPrimary
                )
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = "Select preset",
                        tint = if (selected) AccentSuccess else TextSecondary
                    )
                }
            }

            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Keys: ${preset.keys.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun KeysTab(
    state: CarrierConfigState,
    viewModel: CarrierConfigViewModel,
    onAddKey: () -> Unit
) {
    val keys = remember(state.selectedPreset, state.customKeys) { viewModel.getSelectedKeys() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Selected Keys (${keys.size})", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            GlassButton(
                text = "Add Key",
                onClick = onAddKey,
                variant = ButtonVariant.Secondary
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(keys) { key ->
                KeyCard(key = key, onRemove = {
                    if (key.isCustom) {
                        viewModel.removeCustomKey(key.key)
                    }
                })
            }
        }
    }
}

@Composable
private fun KeyCard(
    key: ConfigKey,
    onRemove: () -> Unit
) {
    GlassmorphicCard(glassStrength = GlassStrength.Light) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(key.key, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text(key.value.displayValue, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (key.isCustom) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = "CUSTOM", status = ChipStatus.Info)
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove custom key",
                            tint = AccentError
                        )
                    }
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrerequisitesCard(state.prerequisites)
        DeploymentCard(state.deployment)

        val canDeploy = state.prerequisites?.allMet == true && state.selectedPreset != null && !state.isLoading
        GlassButton(
            text = "Deploy Override",
            onClick = viewModel::deploy,
            enabled = canDeploy,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Primary
        )
        GlassButton(
            text = "Revert Override",
            onClick = viewModel::revert,
            enabled = state.deployment?.isDeployed == true && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Outlined
        )
    }
}

@Composable
private fun PrerequisitesCard(prerequisites: Prerequisites?) {
    GlassmorphicCard(glassStrength = GlassStrength.Medium) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Prerequisites", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            if (prerequisites == null) {
                Text("Run refresh to evaluate requirements.", color = TextSecondary)
                return@GlassmorphicCard
            }
            StatusRow("Root", prerequisites.hasRoot)
            StatusRow("Magisk", prerequisites.hasMagisk, prerequisites.magiskVersion ?: "unknown")
            StatusRow("Config Path", !prerequisites.carrierConfigPath.isNullOrBlank(), prerequisites.carrierConfigPath)
            StatusRow("Writable", prerequisites.pathWritable)
        }
    }
}

@Composable
private fun DeploymentCard(deployment: CarrierConfigDeployment?) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium,
        borderColor = if (deployment?.isDeployed == true) AccentSuccess else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Deployment", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            StatusChip(
                text = if (deployment?.isDeployed == true) "DEPLOYED" else "NOT DEPLOYED",
                status = if (deployment?.isDeployed == true) ChipStatus.Success else ChipStatus.Neutral
            )
            deployment?.deploymentPath?.let {
                Text("Path: $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (deployment?.backupExists == true) {
                Text("Backup available", style = MaterialTheme.typography.bodySmall, color = AccentWarning)
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    ok: Boolean,
    detail: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary)
            if (!detail.isNullOrBlank()) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        Icon(
            imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Undo,
            contentDescription = null,
            tint = if (ok) AccentSuccess else AccentError
        )
    }
}
