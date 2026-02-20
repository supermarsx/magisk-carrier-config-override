package dev.mars.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mars.carrierconfig.ui.theme.GlassSurface
import dev.mars.carrierconfig.ui.theme.GlassSurfaceMedium

/**
 * Glassmorphic card component with blur effect and transparency
 * 
 * @param modifier Modifier for the card
 * @param blurRadius Blur effect strength (default 16dp)
 * @param borderColor Border color with glow effect
 * @param borderWidth Border width
 * @param cornerRadius Corner radius for rounded corners
 * @param glassStrength Glass transparency strength (Light, Medium, Strong)
 * @param content Content to display inside the card
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    glassStrength: GlassStrength = GlassStrength.Medium,
    content: @Composable BoxScope.() -> Unit
) {
    val glassSurfaceColor = when (glassStrength) {
        GlassStrength.Light -> GlassSurface
        GlassStrength.Medium -> GlassSurfaceMedium
        GlassStrength.Strong -> GlassSurfaceStrong
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = glassSurfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (borderColor != null) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    borderColor.copy(alpha = 0.3f),
                                    borderColor.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                    } else Modifier
                )
                .padding(borderWidth)
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glassSurfaceColor)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Glass strength levels for glassmorphic effects
 */
enum class GlassStrength {
    Light,   // 10% opacity
    Medium,  // 20% opacity
    Strong   // 30% opacity
}
