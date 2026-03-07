package com.supermarsx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.supermarsx.carrierconfig.ui.theme.*

/**
 * Glassmorphic dialog component with gradient overlay and glass styling.
 *
 * Provides a reusable dialog that matches the glassmorphism theme of the app,
 * using the same surface colors and gradient overlays as [GlassmorphicCard].
 *
 * @param onDismissRequest Called when the dialog should be dismissed
 * @param modifier Modifier for the dialog surface
 * @param cornerRadius Corner radius for the dialog shape
 * @param glassStrength Glass effect strength
 * @param properties Dialog window properties
 * @param title Optional title composable
 * @param buttons Optional bottom button row composable
 * @param content Dialog body content
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glassStrength: GlassStrength = GlassStrength.Medium,
    properties: DialogProperties = DialogProperties(),
    title: @Composable (() -> Unit)? = null,
    buttons: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassSurfaceColor = when (glassStrength) {
        GlassStrength.Subtle -> GlassSurfaceSubtle
        GlassStrength.Light -> GlassSurface
        GlassStrength.Medium -> GlassSurfaceMedium
        GlassStrength.Strong -> GlassSurfaceStrong
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            color = BackgroundDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                glassSurfaceColor,
                                BackgroundDeepDark
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (title != null) {
                    title()
                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                }

                content()

                if (buttons != null) {
                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        content = { buttons() }
                    )
                }
            }
        }
    }
}
