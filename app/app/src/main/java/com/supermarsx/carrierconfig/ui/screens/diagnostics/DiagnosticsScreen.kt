package com.supermarsx.carrierconfig.ui.screens.diagnostics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.supermarsx.carrierconfig.data.repository.*
import com.supermarsx.carrierconfig.ui.components.*
import com.supermarsx.carrierconfig.ui.theme.*

/**
 * Advanced Diagnostics screen with real-time monitoring
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    navController: NavHostController? = null,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val logcatEntries by viewModel.logcatEntries.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                        "Advanced Diagnostics",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f),
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.exportDiagnostics() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { 
                        if (state.isLiveLogging) {
                            viewModel.stopLiveLogcat()
                        } else {
                            viewModel.startLiveLogcat()
                        }
                    },
                    containerColor = if (state.isLiveLogging) AccentError else AccentPrimary
                ) {
                    Icon(
                        imageVector = if (state.isLiveLogging) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (state.isLiveLogging) "Stop" else "Start"
                    )
                }
            }
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
                    selectedTabIndex = state.selectedTab,
                    containerColor = BackgroundDark.copy(alpha = 0.8f),
                    contentColor = TextPrimary
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        text = { Text("Logs") },
                        icon = { Icon(Icons.Default.List, null) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        text = { Text("Dumpsys") },
                        icon = { Icon(Icons.Default.DataObject, null) }
                    )
                    Tab(
                        selected = state.selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        text = { Text("Tests") },
                        icon = { Icon(Icons.Default.CheckCircle, null) }
                    )
                }
                
                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (state.selectedTab) {
                        0 -> LogsTab(
                            state = state,
                            entries = logcatEntries,
                            onFilterChange = { viewModel.setLogcatFilter(it) },
                            onLogLevelChange = { viewModel.setLogLevel(it) },
                            onClear = { viewModel.clearLogcat() },
                            onExport = { viewModel.exportLogs() }
                        )
                        1 -> DumpsysTab(
                            state = state,
                            onLoadService = { viewModel.loadDumpsys(it) },
                            onLoadAll = { viewModel.loadAllDumpsys() }
                        )
                        2 -> TestsTab(
                            state = state,
                            onRunTests = { viewModel.runConnectivityTests() }
                        )
                    }
                    
                    // Loading overlay
                    if (state.isLoadingLogs || state.isLoadingDumpsys || 
                        state.isRunningTests || state.isExporting) {
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
    }
}

@Composable
private fun LogsTab(
    state: DiagnosticsState,
    entries: List<LogcatEntry>,
    onFilterChange: (LogcatFilterType) -> Unit,
    onLogLevelChange: (LogLevel) -> Unit,
    onClear: () -> Unit,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter controls
        Surface(
            color = BackgroundDark.copy(alpha = 0.9f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter type chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    LogcatFilterType.values().forEach { filterType ->
                        FilterChip(
                            selected = state.logcatFilterType == filterType,
                            onClick = { onFilterChange(filterType) },
                            label = { Text(filterType.displayName) }
                        )
                    }
                }
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                    OutlinedButton(
                        onClick = onExport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export")
                    }
                }
            }
        }
        
        // Log entries
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        if (state.isLiveLogging) "Waiting for logs..." else "No logs to display",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Text(
                        if (state.isLiveLogging) "Listening..." else "Tap play button to start live monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint
                    )
                }
            }
        } else {
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(entries) { entry ->
                    LogEntryItem(entry)
                }
            }
            
            // Auto-scroll to bottom when live logging
            if (state.isLiveLogging && entries.isNotEmpty()) {
                LaunchedEffect(entries.size) {
                    listState.animateScrollToItem(entries.size - 1)
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogcatEntry) {
    val levelColor = when (entry.level) {
        LogLevel.VERBOSE -> TextHint
        LogLevel.DEBUG -> TextSecondary
        LogLevel.INFO -> AccentPrimary
        LogLevel.WARNING -> AccentWarning
        LogLevel.ERROR, LogLevel.FATAL -> AccentError
    }
    
    Surface(
        color = BackgroundDark.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Level indicator
            Text(
                text = entry.level.priority,
                style = MaterialTheme.typography.labelSmall,
                color = levelColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(16.dp)
            )
            
            // Tag
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.labelSmall,
                color = AccentSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(120.dp)
            )
            
            // Message
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DumpsysTab(
    state: DiagnosticsState,
    onLoadService: (String) -> Unit,
    onLoadAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Service selector buttons
        Text(
            "Select Service:",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf("ims", "phone", "carrier_config", "telecom", "connectivity").forEach { service ->
                FilterChip(
                    selected = state.selectedDumpsysService == service,
                    onClick = { onLoadService(service) },
                    label = { Text(service) }
                )
            }
        }
        
        Button(
            onClick = onLoadAll,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Load All Services")
        }
        
        // Display result
        state.dumpsysResult?.let { result ->
            when (result) {
                is DumpsysResult.Success -> {
                    GlassmorphicCard(
                        glassStrength = GlassStrength.Medium,
                        borderColor = AccentSuccess
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    result.service,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    "${result.lineCount} lines",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            
                            HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                            
                            // Output preview (first 100 lines)
                            Surface(
                                color = BackgroundDeepDark,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                val preview = result.output.lines().take(100).joinToString("\n")
                                Text(
                                    text = preview,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                )
                            }
                            
                            if (result.lineCount > 100) {
                                Text(
                                    "Showing first 100 lines of ${result.lineCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextHint
                                )
                            }
                        }
                    }
                }
                is DumpsysResult.Error -> {
                    GlassmorphicCard(
                        glassStrength = GlassStrength.Medium,
                        borderColor = AccentError
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = AccentError,
                                modifier = Modifier.size(40.dp)
                            )
                            Column {
                                Text(
                                    "Error: ${result.service}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    result.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            // No result yet
            GlassmorphicCard(
                glassStrength = GlassStrength.Medium,
                borderColor = AccentPrimary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        "Select a service to view dumpsys output",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TestsTab(
    state: DiagnosticsState,
    onRunTests: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Run tests button
        Button(
            onClick = onRunTests,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isRunningTests
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.isRunningTests) "Running Tests..." else "Run All Tests")
        }
        
        // Test results
        state.testResults?.let { suite ->
            // Overall status card
            GlassmorphicCard(
                glassStrength = GlassStrength.Medium,
                borderColor = if (suite.allPassed) AccentSuccess else AccentError
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            "Test Suite",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            if (suite.allPassed) "All tests passed" else "${suite.failedCount} test(s) failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusChip(
                        text = if (suite.allPassed) "PASS" else "FAIL",
                        status = if (suite.allPassed) ChipStatus.Success else ChipStatus.Error
                    )
                }
            }
            
            // Individual test results
            Text(
                "Test Results:",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            
            TestResultItem("Network Status", suite.networkStatus)
            TestResultItem("DNS Resolution", suite.dnsResolution)
            TestResultItem("Internet Connectivity", suite.internetConnectivity)
            TestResultItem("Wi-Fi Calling", suite.wifiCalling)
            TestResultItem("IMS Registration", suite.imsRegistration)
            TestResultItem("Cellular Data", suite.cellularData)
        } ?: run {
            // No results yet
            GlassmorphicCard(
                glassStrength = GlassStrength.Medium,
                borderColor = AccentPrimary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Connectivity Test Suite",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        "Run automated tests to verify network connectivity, IMS registration, and Wi-Fi Calling availability",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TestResultItem(
    name: String,
    result: TestResult
) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Subtle,
        borderColor = when (result) {
            is TestResult.Passed -> AccentSuccess
            is TestResult.Failed -> AccentError
            is TestResult.Error -> AccentWarning
            is TestResult.Skipped -> TextHint
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    when (result) {
                        is TestResult.Passed -> result.message
                        is TestResult.Failed -> result.message
                        is TestResult.Error -> result.message
                        is TestResult.Skipped -> result.reason
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = when (result) {
                    is TestResult.Passed -> Icons.Default.CheckCircle
                    is TestResult.Failed -> Icons.Default.Cancel
                    is TestResult.Error -> Icons.Default.Error
                    is TestResult.Skipped -> Icons.Default.SkipNext
                },
                contentDescription = null,
                tint = when (result) {
                    is TestResult.Passed -> AccentSuccess
                    is TestResult.Failed -> AccentError
                    is TestResult.Error -> AccentWarning
                    is TestResult.Skipped -> TextHint
                },
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
