package com.svtt.carrierconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.svtt.carrierconfig.ui.theme.*

enum class StatusChipType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    INACTIVE
}

/**
 * Status chip with icon and text
 */
@Composable
fun StatusChip(
    text: String,
    type: StatusChipType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val (backgroundColor, borderColor, textColor) = when (type) {
        StatusChipType.SUCCESS -> Triple(StatusSuccessBg, StatusSuccess, StatusSuccess)
        StatusChipType.WARNING -> Triple(StatusWarningBg, StatusWarning, StatusWarning)
        StatusChipType.ERROR -> Triple(StatusErrorBg, StatusError, StatusError)
        StatusChipType.INFO -> Triple(StatusInfoBg, StatusInfo, StatusInfo)
        StatusChipType.INACTIVE -> Triple(
            Color.White.copy(alpha = 0.05f),
            StatusInactive,
            StatusInactive
        )
    }
    
    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Simple boolean status chip (Yes/No)
 */
@Composable
fun BooleanStatusChip(
    label: String,
    value: Boolean,
    modifier: Modifier = Modifier
) {
    StatusChip(
        text = if (value) "✓ $label" else "✗ $label",
        type = if (value) StatusChipType.SUCCESS else StatusChipType.INACTIVE,
        modifier = modifier
    )
}

/**
 * Loading status chip
 */
@Composable
fun LoadingStatusChip(
    text: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = StatusInfoBg,
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = StatusInfo.copy(alpha = 0.5f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: Add animated loading indicator
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = StatusInfo
        )
    }
}
