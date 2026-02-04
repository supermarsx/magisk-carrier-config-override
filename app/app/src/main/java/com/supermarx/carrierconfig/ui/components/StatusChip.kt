package com.supermarx.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.supermarx.carrierconfig.ui.theme.AccentError
import com.supermarx.carrierconfig.ui.theme.AccentSuccess
import com.supermarx.carrierconfig.ui.theme.AccentWarning
import com.supermarx.carrierconfig.ui.theme.BackgroundDeepDark
import com.supermarx.carrierconfig.ui.theme.TextPrimary

/**
 * Status chip component with color-coded states
 */
@Composable
fun StatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        ChipStatus.Success -> AccentSuccess.copy(alpha = 0.2f)
        ChipStatus.Warning -> AccentWarning.copy(alpha = 0.2f)
        ChipStatus.Error -> AccentError.copy(alpha = 0.2f)
        ChipStatus.Info -> AccentPrimary.copy(alpha = 0.2f)
        ChipStatus.Neutral -> GlassSurfaceMedium
    }
    
    val textColor = when (status) {
        ChipStatus.Success -> AccentSuccess
        ChipStatus.Warning -> AccentWarning
        ChipStatus.Error -> AccentError
        ChipStatus.Info -> AccentPrimary
        ChipStatus.Neutral -> TextPrimary
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Status types for chips
 */
enum class ChipStatus {
    Success,
    Warning,
    Error,
    Info,
    Neutral
}
