package com.supermarsx.carrierconfig.ui.screens.about

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.supermarsx.carrierconfig.ui.components.*
import com.supermarsx.carrierconfig.ui.theme.*

/**
 * About screen showing app information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "About CCO",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = TextPrimary
                        )
                    }
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // App Icon/Logo Placeholder
                GlassmorphicCard(
                    glassStrength = GlassStrength.Strong,
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "CCO Logo",
                            tint = AccentPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // App Name and Version
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "CCO Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        "CarrierConfig Override",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    StatusChip(
                        text = "v1.0.0-alpha",
                        status = ChipStatus.Info
                    )
                }
                
                // Description
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "About",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPrimary
                        )
                        Text(
                            "A comprehensive toolkit to control Wi-Fi Calling (VoWiFi), VoLTE, IMS and related CarrierConfig behavior on Samsung Galaxy devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Start
                        )
                    }
                }
                
                // Features
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Key Features",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPrimary
                        )
                        
                        FeatureItem(Icons.Default.Dashboard, "Live device diagnostics")
                        FeatureItem(Icons.Default.Build, "CarrierConfig override system")
                        FeatureItem(Icons.Default.Security, "Safe backup & revert")
                        FeatureItem(Icons.Default.Tune, "6 predefined presets")
                        FeatureItem(Icons.Default.Code, "Custom key support")
                    }
                }
                
                // Developer Info
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Developer",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPrimary
                        )
                        
                        InfoRow("Author", "supermarsx")
                        InfoRow("License", "MIT License")
                        InfoRow("Repository", "github.com/supermarsx/cco")
                    }
                }
                
                // Requirements
                GlassmorphicCard(glassStrength = GlassStrength.Medium) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Requirements",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPrimary
                        )
                        
                        InfoRow("Device", "Samsung Galaxy (One UI 5-7)")
                        InfoRow("Android", "13+ (API 33+)")
                        InfoRow("Root", "Required (Magisk 24+)")
                    }
                }
                
                // Legal
                GlassmorphicCard(
                    glassStrength = GlassStrength.Subtle,
                    borderColor = AccentWarning
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccentWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Disclaimer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AccentWarning
                                )
                                Text(
                                    "This app modifies system configuration files. Use at your own risk. Always backup your data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                // Copyright
                Text(
                    "© 2026 supermarsx\nMIT License",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentSuccess,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
