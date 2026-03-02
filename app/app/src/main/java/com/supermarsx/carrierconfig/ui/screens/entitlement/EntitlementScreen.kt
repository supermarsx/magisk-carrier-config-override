package com.supermarsx.carrierconfig.ui.screens.entitlement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.supermarsx.carrierconfig.ui.components.*
import com.supermarsx.carrierconfig.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Entitlement (Method 2) — Runtime Entitlement Simulation
 * Spec Section 4.3 — Three-tab layout: Profiles, Hooks, Session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntitlementScreen(
    viewModel: EntitlementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sessionEvents by viewModel.sessionEvents.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = EntitlementTab.entries

    // Snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Runtime Hooks",
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
                        colors = listOf(GradientTop, BackgroundDeepDark, GradientBottom)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundDark.copy(alpha = 0.7f),
                    contentColor = AccentPrimary
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    tab.displayName,
                                    color = if (selectedTab == index) AccentPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }

                // Tab content
                when (tabs[selectedTab]) {
                    EntitlementTab.PROFILES -> ProfilesTab(state, viewModel)
                    EntitlementTab.HOOKS -> HooksTab(state, viewModel)
                    EntitlementTab.SESSION -> SessionTab(state, sessionEvents, viewModel)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Profiles Tab
// ═══════════════════════════════════════════════════════════

@Composable
private fun ProfilesTab(
    state: EntitlementState,
    viewModel: EntitlementViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Backend selector
        Text("Backend", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        GlassmorphicCard(glassStrength = GlassStrength.Medium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HookBackend.entries.forEach { backend ->
                    FilterChip(
                        selected = state.selectedBackend == backend,
                        onClick = { viewModel.setBackend(backend) },
                        label = { Text(backend.displayName) },
                        leadingIcon = if (state.selectedBackend == backend) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // Profile list
        Text("Hook Profiles", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        if (state.isLoadingProfiles) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else if (state.profiles.isEmpty()) {
            GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
                Text("No profiles loaded", color = TextSecondary)
            }
        } else {
            state.profiles.forEach { profile ->
                val isSelected = state.selectedProfileId == profile.id
                GlassmorphicCard(
                    glassStrength = if (isSelected) GlassStrength.Strong else GlassStrength.Medium,
                    borderColor = if (isSelected) AccentPrimary else null,
                    modifier = Modifier.clickable { viewModel.selectProfile(profile.id) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                profile.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) AccentPrimary else TextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            profile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            profile.oneuiVersions.forEach { v ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("OneUI $v", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Text(
                            "${profile.targets.size} hook target(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Metadata
        state.profilesMetadata?.let { meta ->
            GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
                Column {
                    Text(
                        "Profile Database v${meta.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        "Updated: ${meta.lastUpdated}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Hooks Tab
// ═══════════════════════════════════════════════════════════

@Composable
private fun HooksTab(
    state: EntitlementState,
    viewModel: EntitlementViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Entitlement Packages", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            IconButton(onClick = { viewModel.scanEntitlementPackages() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = AccentPrimary)
            }
        }

        if (state.isScanningPackages) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else if (state.entitlementPackages.isEmpty()) {
            GlassmorphicCard(
                glassStrength = GlassStrength.Medium,
                borderColor = AccentWarning
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AccentWarning)
                    Column {
                        Text(
                            "No entitlement packages found",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Text(
                            "Install a SIM or check device compatibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            state.entitlementPackages.forEach { pkg ->
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                pkg.packageName,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                "v${pkg.versionName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = pkg.isHookTarget,
                            onCheckedChange = { viewModel.togglePackageHook(pkg.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = AccentPrimary,
                                checkedThumbColor = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Selected profile hook targets preview
        val selectedProfile = state.profiles.find { it.id == state.selectedProfileId }
        if (selectedProfile != null) {
            Spacer(Modifier.height(8.dp))
            Text("Profile Hooks", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

            selectedProfile.targets.forEach { target ->
                GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${target.`class`}.${target.method}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentPrimary
                        )
                        Text(
                            target.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            "Return → ${target.returnValue}  |  ${target.signature}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Session Tab
// ═══════════════════════════════════════════════════════════

@Composable
private fun SessionTab(
    state: EntitlementState,
    sessionEvents: List<SessionEvent>,
    viewModel: EntitlementViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Frida status card
        GlassmorphicCard(
            glassStrength = GlassStrength.Medium,
            borderColor = if (state.fridaRunning) AccentSuccess else AccentWarning
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frida Server", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.fridaRunning) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (state.fridaRunning) AccentSuccess else AccentError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (state.fridaRunning) "Running" else if (state.fridaInstalled) "Stopped" else "Not Installed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.fridaRunning) AccentSuccess else TextSecondary
                        )
                    }
                }
                state.fridaVersion?.let {
                    Text("Version: $it", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                state.fridaPid?.let {
                    Text("PID: $it", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                if (!state.fridaInstalled) {
                    Button(
                        onClick = { viewModel.installFrida() },
                        enabled = !state.isInstallingFrida,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        if (state.isInstallingFrida) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (state.isInstallingFrida) "Installing…" else "Install Frida Server")
                    }
                }
            }
        }

        // Session controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val canStart = state.selectedProfileId != null &&
                    state.fridaInstalled &&
                    !state.sessionActive &&
                    state.selectedBackend == HookBackend.FRIDA

            Button(
                onClick = { viewModel.startSession() },
                enabled = canStart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Start")
            }

            Button(
                onClick = { viewModel.stopSession() },
                enabled = state.sessionActive,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentError)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Stop")
            }
        }

        if (state.selectedProfileId == null) {
            Text(
                "Select a profile in the Profiles tab first",
                style = MaterialTheme.typography.bodySmall,
                color = AccentWarning
            )
        }

        // Live event stream
        Text("Session Events", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        if (sessionEvents.isEmpty()) {
            GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
                Text(
                    if (state.sessionActive) "Waiting for events…" else "No session running",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sessionEvents.reversed()) { event ->
                    SessionEventRow(event)
                }
            }

            // Export trace button
            OutlinedButton(
                onClick = {
                    // In a real app this would trigger a share intent / file save
                    viewModel.exportSessionTrace()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export Trace")
            }
        }
    }
}

@Composable
private fun SessionEventRow(event: SessionEvent) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val color = when (event.type) {
        SessionEventType.INFO -> TextSecondary
        SessionEventType.HOOK -> AccentPrimary
        SessionEventType.ERROR -> AccentError
    }
    val icon = when (event.type) {
        SessionEventType.INFO -> Icons.Default.Info
        SessionEventType.HOOK -> Icons.Default.Code
        SessionEventType.ERROR -> Icons.Default.ErrorOutline
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            timeFormat.format(Date(event.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            event.message,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}
