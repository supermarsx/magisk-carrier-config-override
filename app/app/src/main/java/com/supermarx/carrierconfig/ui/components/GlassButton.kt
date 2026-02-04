package com.supermarx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.supermarx.carrierconfig.ui.theme.AccentPrimary
import com.supermarx.carrierconfig.ui.theme.AccentPrimaryGlow
import com.supermarx.carrierconfig.ui.theme.GlassSurfaceMedium
import com.supermarx.carrierconfig.ui.theme.TextPrimary

/**
 * Primary glassmorphic button with gradient and glow effect
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    val colors = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = AccentPrimary,
            contentColor = TextPrimary,
            disabledContainerColor = GlassSurfaceMedium,
            disabledContentColor = TextPrimary.copy(alpha = 0.5f)
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = AccentSecondary,
            contentColor = TextPrimary,
            disabledContainerColor = GlassSurfaceMedium,
            disabledContentColor = TextPrimary.copy(alpha = 0.5f)
        )
        ButtonVariant.Outlined -> ButtonDefaults.outlinedButtonColors(
            containerColor = GlassSurfaceMedium,
            contentColor = AccentPrimary,
            disabledContainerColor = GlassSurfaceMedium,
            disabledContentColor = TextPrimary.copy(alpha = 0.5f)
        )
    }
    
    Box(
        modifier = modifier
            .background(
                brush = if (enabled && variant == ButtonVariant.Primary) {
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPrimaryGlow,
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                },
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (enabled && variant == ButtonVariant.Primary) Modifier.blur(24.dp) else Modifier)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Button style variants
 */
enum class ButtonVariant {
    Primary,
    Secondary,
    Outlined
}
