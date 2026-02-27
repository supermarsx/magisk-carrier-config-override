package com.supermarsx.carrierconfig.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.supermarsx.carrierconfig.ui.components.GlassmorphicCard
import com.supermarsx.carrierconfig.ui.components.GlassButton
import com.supermarsx.carrierconfig.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = GlassBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "CCO uses the following open source libraries:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(licenses) { license ->
                LicenseCard(license)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Thank you to all the open source contributors!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun LicenseCard(license: License) {
    GlassmorphicCard(
        glassStrength = GlassStrength.Medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = license.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            if (license.version.isNotEmpty()) {
                Text(
                    text = "Version ${license.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Text(
                text = license.license,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentCyan,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            if (license.description.isNotEmpty()) {
                Text(
                    text = license.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (license.url.isNotEmpty()) {
                TextButton(
                    onClick = { /* Open URL */ },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(text = "View on GitHub", color = AccentPurple)
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

data class License(
    val name: String,
    val version: String,
    val license: String,
    val description: String,
    val url: String
)

private val licenses = listOf(
    License(
        name = "Jetpack Compose",
        version = "1.6.0",
        license = "Apache License 2.0",
        description = "Android's recommended modern toolkit for building native UI",
        url = "https://github.com/androidx/androidx"
    ),
    License(
        name = "Material 3",
        version = "1.2.0",
        license = "Apache License 2.0",
        description = "Material Design 3 components for Jetpack Compose",
        url = "https://github.com/material-components/material-components-android-compose"
    ),
    License(
        name = "Kotlin",
        version = "1.9.22",
        license = "Apache License 2.0",
        description = "Modern programming language for Android development",
        url = "https://github.com/JetBrains/kotlin"
    ),
    License(
        name = "Kotlin Coroutines",
        version = "1.7.3",
        license = "Apache License 2.0",
        description = "Library for asynchronous programming with Kotlin",
        url = "https://github.com/Kotlin/kotlinx.coroutines"
    ),
    License(
        name = "Kotlinx Serialization",
        version = "1.6.3",
        license = "Apache License 2.0",
        description = "Kotlin multiplatform / multi-format serialization",
        url = "https://github.com/Kotlin/kotlinx.serialization"
    ),
    License(
        name = "Hilt",
        version = "2.50",
        license = "Apache License 2.0",
        description = "Dependency injection library for Android",
        url = "https://github.com/google/dagger"
    ),
    License(
        name = "Navigation Compose",
        version = "2.7.7",
        license = "Apache License 2.0",
        description = "Navigation component for Jetpack Compose",
        url = "https://github.com/androidx/androidx"
    ),
    License(
        name = "DataStore",
        version = "1.0.0",
        license = "Apache License 2.0",
        description = "Jetpack DataStore for data storage",
        url = "https://github.com/androidx/androidx"
    ),
    License(
        name = "Room",
        version = "2.6.1",
        license = "Apache License 2.0",
        description = "Persistence library providing abstraction layer over SQLite",
        url = "https://github.com/androidx/androidx"
    ),
    License(
        name = "libsu",
        version = "5.2.2",
        license = "Apache License 2.0",
        description = "Android library providing APIs to work with root access",
        url = "https://github.com/topjohnwu/libsu"
    ),
    License(
        name = "Gson",
        version = "2.10.1",
        license = "Apache License 2.0",
        description = "Java library for JSON serialization/deserialization",
        url = "https://github.com/google/gson"
    ),
    License(
        name = "WorkManager",
        version = "2.9.0",
        license = "Apache License 2.0",
        description = "Schedule deferrable, asynchronous tasks",
        url = "https://github.com/androidx/androidx"
    ),
    License(
        name = "JUnit",
        version = "4.13.2",
        license = "Eclipse Public License 1.0",
        description = "Testing framework for Java and Android",
        url = "https://github.com/junit-team/junit4"
    ),
    License(
        name = "Mockito",
        version = "5.10.0",
        license = "MIT License",
        description = "Mocking framework for unit tests",
        url = "https://github.com/mockito/mockito"
    ),
    License(
        name = "Turbine",
        version = "1.0.0",
        license = "Apache License 2.0",
        description = "Testing library for Kotlin Flows",
        url = "https://github.com/cashapp/turbine"
    ),
    License(
        name = "Truth",
        version = "1.4.0",
        license = "Apache License 2.0",
        description = "Fluent assertions for Java and Android",
        url = "https://github.com/google/truth"
    )
)
