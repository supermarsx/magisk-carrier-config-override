package com.supermarsx.carrierconfig.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarsx.carrierconfig.ui.components.*
import com.supermarsx.carrierconfig.ui.theme.*
import com.supermarsx.carrierconfig.util.PickConfigFileContract
import com.supermarsx.carrierconfig.util.PickDirectoryContract
import com.supermarsx.carrierconfig.util.UriHelper

/**
 * Settings and preferences screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Activity result launchers
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = PickDirectoryContract()
    ) { uri: Uri? ->
        uri?.let {
            UriHelper.takePersistablePermission(context, it)
            val path = UriHelper.getDisplayPath(context, it)
            viewModel.setExportDirectory(path)
            viewModel.showMessage("Export directory set to: $path")
        }
    }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = PickConfigFileContract()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importConfigurationFromUri(context, it)
        }
    }
    
    // Dialog states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGlassStrengthDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    
    // Show messages
    LaunchedEffect(state.message, state.error) {
        state.message?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                )
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
                            BackgroundDeepDark,
                            GradientBottom
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // General Section
                SettingsSectionHeader("General")
                
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column {
                        SettingsToggleItem(
                            icon = Icons.Default.Refresh,
                            title = "Auto-refresh dashboard",
                            subtitle = "Automatically refresh device status on app open",
                            checked = state.autoRefresh,
                            onCheckedChange = { viewModel.setAutoRefresh(it) }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Show notifications for deployment status",
                            checked = state.enableNotifications,
                            onCheckedChange = { viewModel.setEnableNotifications(it) }
                        )
                    }
                }
                
                // Appearance Section
                SettingsSectionHeader("Appearance")
                
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column {
                        SettingsClickableItem(
                            icon = Icons.Default.Palette,
                            title = "Theme",
                            subtitle = state.theme.replaceFirstChar { it.uppercase() },
                            onClick = { showThemeDialog = true }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Contrast,
                            title = "Glass Effect Strength",
                            subtitle = state.glassStrength.replaceFirstChar { it.uppercase() },
                            onClick = { showGlassStrengthDialog = true }
                        )
                    }
                }
                
                // Advanced Section
                SettingsSectionHeader("Advanced")
                
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column {
                        SettingsToggleItem(
                            icon = Icons.Default.BugReport,
                            title = "Debug Mode",
                            subtitle = "Enable verbose logging and debug features",
                            checked = state.debugMode,
                            onCheckedChange = { viewModel.setDebugMode(it) }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Folder,
                            title = "Export Directory",
                            subtitle = state.exportDirectory.ifEmpty { "/sdcard/CCO/exports" },
                            onClick = {
                                directoryPickerLauncher.launch(null)
                            }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.DeleteSweep,
                            title = "Clear Cache",
                            subtitle = "Current cache size: ${viewModel.getFormattedCacheSize()}",
                            onClick = { 
                                viewModel.clearCache()
                                // Refresh size after clearing (will be 0)
                            }
                        )
                    }
                }
                
                // Backup & Data Section
                SettingsSectionHeader("Backup & Data")
                
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column {
                        SettingsClickableItem(
                            icon = Icons.Default.Backup,
                            title = "Export Configuration",
                            subtitle = "Save all settings and presets",
                            onClick = { viewModel.exportConfiguration() }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Upload,
                            title = "Import Configuration",
                            subtitle = "Restore settings from file",
                            onClick = {
                                filePickerLauncher.launch(Unit)
                            }
                        )
                    }
                }
                
                // About Section
                SettingsSectionHeader("About")
                
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column {
                        SettingsClickableItem(
                            icon = Icons.Default.Info,
                            title = "About",
                            subtitle = "Version 1.0.0-alpha",
                            onClick = onNavigateToAbout
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Description,
                            title = "Licenses",
                            subtitle = "Open source licenses",
                            onClick = { onNavigateToAbout }
                        )
                        
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Code,
                            title = "Source Code",
                            subtitle = "github.com/supermarsx/cco",
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/supermarsx/magisk-carrier-config-override")
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    viewModel.showMessage("Unable to open browser")
                                }
                            }
                        )
                    }
                }
                
                // Danger Zone
                SettingsSectionHeader("Danger Zone")
                
                GlassmorphicCard(
                    glassStrength = GlassStrength.Medium,
                    borderColor = AccentError
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.Warning,
                        title = "Reset All Settings",
                        subtitle = "Restore app to default state",
                        titleColor = AccentError,
                        onClick = { showResetConfirmDialog = true }
                    )
                }
                
                // Version info at bottom
                Text(
                    "CCO Manager v1.0.0-alpha",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            // Loading overlay
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
        // Dialogs
        if (showThemeDialog) {
            ThemePickerDialog(
                currentTheme = state.theme,
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { 
                    viewModel.setTheme(it)
                    showThemeDialog = false
                }
            )
        }
        
        if (showGlassStrengthDialog) {
            GlassStrengthPickerDialog(
                currentStrength = state.glassStrength,
                onDismiss = { showGlassStrengthDialog = false },
                onStrengthSelected = { 
                    viewModel.setGlassStrength(it)
                    showGlassStrengthDialog = false
                }
            )
        }
        
        if (showResetConfirmDialog) {
            ConfirmationDialog(
                title = "Reset Settings?",
                message = "This will restore all settings to their default values. This action cannot be undone.",
                confirmText = "Reset",
                onConfirm = { viewModel.resetSettings() },
                onDismiss = { showResetConfirmDialog = false },
                isDestructive = true
            )
        }    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = AccentPrimary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentPrimary,
                checkedTrackColor = AccentPrimary.copy(alpha = 0.5f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = GlassSurfaceSubtle
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = TextHint,
            modifier = Modifier.size(20.dp)
        )
    }
}
