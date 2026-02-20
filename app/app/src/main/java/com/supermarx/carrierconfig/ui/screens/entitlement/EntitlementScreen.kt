package dev.mars.carrierconfig.ui.screens.entitlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import dev.mars.carrierconfig.ui.components.*
import dev.mars.carrierconfig.ui.theme.*

/**
 * Entitlement (Method 2) - Runtime hooks screen
 * Milestone 3 - Coming Soon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntitlementScreen() {
    Scaffold(
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
                // Coming Soon Banner
                GlassmorphicCard(
                    glassStrength = GlassStrength.Medium,
                    borderColor = AccentWarning
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            tint = AccentWarning,
                            modifier = Modifier.size(48.dp)
                        )
                        Column {
                            Text(
                                "Milestone 3: In Development",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                "Runtime entitlement simulation coming soon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Feature Preview
                Text(
                    "Planned Features",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                FeatureCard(
                    icon = Icons.Default.BugReport,
                    title = "Frida Integration",
                    description = "Dynamic runtime instrumentation for IMS entitlement spoofing"
                )
                
                FeatureCard(
                    icon = Icons.Default.Extension,
                    title = "LSPosed Support",
                    description = "Persistent hooks without needing Frida server"
                )
                
                FeatureCard(
                    icon = Icons.Default.DeviceHub,
                    title = "One UI Profiles",
                    description = "Version-specific hook profiles for One UI 5/6/7"
                )
                
                FeatureCard(
                    icon = Icons.Default.PlayCircle,
                    title = "Session Management",
                    description = "Start/stop hook sessions with live monitoring"
                )
                
                FeatureCard(
                    icon = Icons.Default.FiberManualRecord,
                    title = "Record & Replay",
                    description = "Capture and replay entitlement check sequences"
                )
                
                // Progress Info
                GlassmorphicCard(glassStrength = GlassStrength.Subtle) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Development Roadmap",
                            style = MaterialTheme.typography.titleSmall,
                            color = AccentPrimary
                        )
                        Text(
                            "• Phase 1: Frida script architecture",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            "• Phase 2: Hook profiles implementation",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            "• Phase 3: UI integration",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            "• Phase 4: LSPosed module",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    GlassmorphicCard(glassStrength = GlassStrength.Medium) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(32.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
