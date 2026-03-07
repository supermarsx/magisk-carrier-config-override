package com.supermarsx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.supermarsx.carrierconfig.ui.theme.*

/**
 * Glassmorphic modal bottom sheet with gradient overlay and glass styling.
 *
 * Wraps [ModalBottomSheet] with the glassmorphism design system used by
 * [GlassmorphicCard] and [GlassDialog].
 *
 * @param onDismissRequest Called when the bottom sheet should be dismissed
 * @param modifier Modifier for the bottom sheet
 * @param sheetState [SheetState] controlling the bottom sheet visibility
 * @param cornerRadius Top corner radius for the sheet
 * @param glassStrength Glass effect strength
 * @param content Sheet body content
 */
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    cornerRadius: Dp = 24.dp,
    glassStrength: GlassStrength = GlassStrength.Medium,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassSurfaceColor = when (glassStrength) {
        GlassStrength.Subtle -> GlassSurfaceSubtle
        GlassStrength.Light -> GlassSurface
        GlassStrength.Medium -> GlassSurfaceMedium
        GlassStrength.Strong -> GlassSurfaceStrong
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        containerColor = BackgroundDark,
        tonalElevation = 8.dp,
        dragHandle = {
            // Custom glass drag handle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = GlassBorder,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassSurfaceColor,
                            BackgroundDeepDark
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}
