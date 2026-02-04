package com.svtt.carrierconfig.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svtt.carrierconfig.ui.theme.GlassSurface
import com.svtt.carrierconfig.ui.theme.GlassSurfaceMedium
import com.svtt.carrierconfig.ui.theme.GlassSurfaceStrong

/**
 * A card with glassmorphism effect
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassSurface,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderRadius: Dp = 16.dp,
    blur: Dp = 24.dp,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(borderRadius),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(borderRadius)
            )
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(radius = blur)
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(borderRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Elevated variant with stronger glass effect
 */
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = GlassSurfaceMedium,
        borderColor = Color.White.copy(alpha = 0.3f),
        elevation = 8.dp,
        blur = 32.dp,
        content = content
    )
}

/**
 * Compact variant with smaller padding
 */
@Composable
fun GlassCardCompact(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                color = GlassSurface,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(radius = 20.dp)
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        content()
    }
}

/**
 * Accent variant with colored border glow
 */
@Composable
fun GlassCardAccent(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = GlassSurface,
        borderColor = accentColor.copy(alpha = 0.5f),
        content = content
    )
}

/**
 * Info panel for key-value pairs
 */
@Composable
fun InfoPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = GlassSurface,
        borderColor = Color.White.copy(alpha = 0.15f),
        borderRadius = 12.dp,
        blur = 20.dp
    ) {
        Column {
            content()
        }
    }
}
