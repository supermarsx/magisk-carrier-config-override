package com.supermarsx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.matchParentSize
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
import com.supermarsx.carrierconfig.ui.theme.AccentPrimary
import com.supermarsx.carrierconfig.ui.theme.AccentPrimaryGlow
import com.supermarsx.carrierconfig.ui.theme.AccentSecondary
import com.supermarsx.carrierconfig.ui.theme.GlassSurfaceMedium
import com.supermarsx.carrierconfig.ui.theme.TextPrimary

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
    
    Box(modifier = modifier) {
        // Glow layer behind the button (blurred separately so text stays crisp)
        if (enabled && variant == ButtonVariant.Primary) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(24.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentPrimaryGlow,
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
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
