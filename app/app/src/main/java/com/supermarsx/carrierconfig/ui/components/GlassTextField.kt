package com.supermarsx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.supermarsx.carrierconfig.ui.theme.AccentError
import com.supermarsx.carrierconfig.ui.theme.AccentPrimary
import com.supermarsx.carrierconfig.ui.theme.GlassSurfaceMedium
import com.supermarsx.carrierconfig.ui.theme.TextPrimary
import com.supermarsx.carrierconfig.ui.theme.TextSecondary

/**
 * Glassmorphic text field with blur effect and transparency
 * 
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param label Optional label text
 * @param placeholder Placeholder text when empty
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 * @param singleLine Whether the field is single-line
 * @param maxLines Maximum number of lines
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param visualTransformation Visual transformation (e.g., password masking)
 * @param keyboardOptions Keyboard options
 * @param keyboardActions Keyboard actions
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier
    ) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) AccentError else TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Text field container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurfaceMedium)
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> AccentError
                        !enabled -> TextSecondary.copy(alpha = 0.2f)
                        else -> AccentPrimary.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (leadingIcon != null) 32.dp else 0.dp,
                        end = if (trailingIcon != null) 32.dp else 0.dp
                    ),
                textStyle = LocalTextStyle.current.copy(
                    color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.5f)
                ),
                enabled = enabled,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                cursorBrush = SolidColor(AccentPrimary),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Leading icon
                        if (leadingIcon != null) {
                            Box(
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                leadingIcon()
                            }
                        }
                        
                        // Placeholder
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Actual text field
                        innerTextField()
                        
                        // Trailing icon
                        if (trailingIcon != null) {
                            Box(
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                trailingIcon()
                            }
                        } else if (isError) {
                            // Error icon if no custom trailing icon
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = AccentError,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            )
        }
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AccentError,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}

/**
 * Glassmorphic multiline text field for longer text input
 */
@Composable
fun GlassTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 10,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    GlassTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        singleLine = false,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
