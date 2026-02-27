package com.supermarsx.carrierconfig.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.supermarsx.carrierconfig.data.model.ConfigKey
import com.supermarsx.carrierconfig.data.model.ConfigValue
import com.supermarsx.carrierconfig.ui.theme.*

/**
 * Dialog for adding custom configuration keys
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKeyDialog(
    onDismiss: () -> Unit,
    onAdd: (ConfigKey) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(KeyType.BOOLEAN) }
    var booleanValue by remember { mutableStateOf(true) }
    var intValue by remember { mutableStateOf("") }
    var stringValue by remember { mutableStateOf("") }
    var arrayValues by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        GlassmorphicCard(
            glassStrength = GlassStrength.Strong,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Add Custom Key",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
                
                // Key name input
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { 
                        keyName = it
                        error = null
                    },
                    label = { Text("Key Name") },
                    placeholder = { Text("carrier_wfc_ims_available_bool") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                
                // Type selector
                Text(
                    "Value Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeyType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPrimary.copy(alpha = 0.3f),
                                selectedLabelColor = AccentPrimary,
                                containerColor = GlassSurfaceSubtle,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                
                // Value input based on type
                when (selectedType) {
                    KeyType.BOOLEAN -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Boolean Value",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Switch(
                                checked = booleanValue,
                                onCheckedChange = { booleanValue = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentPrimary,
                                    checkedTrackColor = AccentPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    KeyType.INT -> {
                        OutlinedTextField(
                            value = intValue,
                            onValueChange = { 
                                intValue = it
                                error = null
                            },
                            label = { Text("Integer Value") },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = AccentPrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                    KeyType.STRING -> {
                        OutlinedTextField(
                            value = stringValue,
                            onValueChange = { 
                                stringValue = it
                                error = null
                            },
                            label = { Text("String Value") },
                            placeholder = { Text("Enter string value") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = AccentPrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                    KeyType.STRING_ARRAY -> {
                        OutlinedTextField(
                            value = arrayValues,
                            onValueChange = { 
                                arrayValues = it
                                error = null
                            },
                            label = { Text("String Array Values") },
                            placeholder = { Text("value1,value2,value3") },
                            supportingText = { Text("Comma-separated values") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = AccentPrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            minLines = 2
                        )
                    }
                }
                
                // Error message
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentError
                    )
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = ButtonVariant.Outlined,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Add Key",
                        onClick = {
                            // Validate and create key
                            if (keyName.isBlank()) {
                                error = "Key name is required"
                                return@GlassButton
                            }
                            
                            val configValue = try {
                                when (selectedType) {
                                    KeyType.BOOLEAN -> ConfigValue.BooleanValue(booleanValue)
                                    KeyType.INT -> {
                                        val value = intValue.toIntOrNull()
                                        if (value == null) {
                                            error = "Invalid integer value"
                                            return@GlassButton
                                        }
                                        ConfigValue.IntValue(value)
                                    }
                                    KeyType.STRING -> {
                                        if (stringValue.isBlank()) {
                                            error = "String value is required"
                                            return@GlassButton
                                        }
                                        ConfigValue.StringValue(stringValue)
                                    }
                                    KeyType.STRING_ARRAY -> {
                                        if (arrayValues.isBlank()) {
                                            error = "Array values are required"
                                            return@GlassButton
                                        }
                                        val values = arrayValues.split(",").map { it.trim() }
                                        ConfigValue.StringArrayValue(values)
                                    }
                                }
                            } catch (e: Exception) {
                                error = "Invalid value: ${e.message}"
                                return@GlassButton
                            }
                            
                            val key = ConfigKey(
                                key = keyName,
                                value = configValue,
                                isCustom = true
                            )
                            
                            onAdd(key)
                        },
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Key type enumeration
 */
private enum class KeyType(val displayName: String) {
    BOOLEAN("Boolean"),
    INT("Integer"),
    STRING("String"),
    STRING_ARRAY("Array")
}
