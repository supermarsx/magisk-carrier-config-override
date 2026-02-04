package com.supermarx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.supermarx.carrierconfig.ui.theme.*

/**
 * Theme selection dialog
 */
@Composable
fun ThemePickerDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GradientTop.copy(alpha = 0.3f),
                                BackgroundDeepDark
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select Theme",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                
                val themes = listOf(
                    "dark" to "Dark (Default)",
                    "amoled" to "AMOLED Black",
                    "auto" to "Auto (System)"
                )
                
                themes.forEach { (value, label) ->
                    ThemeOption(
                        label = label,
                        selected = currentTheme == value,
                        onClick = { onThemeSelected(value) }
                    )
                }
                
                HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) AccentPrimary.copy(alpha = 0.2f) else BackgroundDark.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) AccentPrimary else TextPrimary
            )
            
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = AccentPrimary
                )
            }
        }
    }
}

/**
 * Glass strength picker dialog
 */
@Composable
fun GlassStrengthPickerDialog(
    currentStrength: String,
    onDismiss: () -> Unit,
    onStrengthSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GradientTop.copy(alpha = 0.3f),
                                BackgroundDeepDark
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Glass Effect Strength",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                
                val strengths = listOf(
                    "subtle" to "Subtle (Minimal blur)",
                    "medium" to "Medium (Balanced)",
                    "strong" to "Strong (Maximum blur)",
                    "none" to "None (Disabled)"
                )
                
                strengths.forEach { (value, label) ->
                    GlassStrengthOption(
                        label = label,
                        value = value,
                        selected = currentStrength == value,
                        onClick = { onStrengthSelected(value) }
                    )
                }
                
                HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassStrengthOption(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val glassStrength = when (value) {
        "subtle" -> GlassStrength.Subtle
        "medium" -> GlassStrength.Medium
        "strong" -> GlassStrength.Strong
        else -> GlassStrength.Medium
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (value != "none") {
            GlassTint.copy(alpha = glassStrength.alpha)
        } else {
            BackgroundDark.copy(alpha = 0.3f)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) AccentPrimary else TextPrimary
                )
                if (value != "none") {
                    Text(
                        "Alpha: ${glassStrength.alpha}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = AccentPrimary
                )
            }
        }
    }
}

/**
 * Confirmation dialog for destructive actions
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isDestructive) AccentError else TextPrimary
            )
        },
        text = {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) AccentError else AccentPrimary
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = BackgroundDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
