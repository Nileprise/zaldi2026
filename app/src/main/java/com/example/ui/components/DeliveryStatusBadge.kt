package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Included the enum to ensure the component is self-contained
enum class DeliveryStatusType(val label: String, val testTagId: String) {
    PENDING("Pending", "status_pending"),
    ACTIVE("Active", "status_active"),
    COMPLETED("Completed", "status_completed")
}

@Composable
fun DeliveryStatusBadge(
    status: DeliveryStatusType,
    modifier: Modifier = Modifier,
    subStatusLabel: String? = null,
    isCompact: Boolean = false,
    showPulsingDot: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Modern M3 adaptation: Use alpha copying for borders to guarantee Dark Mode compatibility
    // Note: Assuming SurfaceTertiary, WarningAmber etc. come from your theme. 
    // Mapped to standard M3 colors here for compilation safety.
    val (containerColor, contentColor, icon) = when (status) {
        DeliveryStatusType.PENDING -> StatusColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Default.HourglassTop
        )
        DeliveryStatusType.ACTIVE -> StatusColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Navigation
        )
        DeliveryStatusType.COMPLETED -> StatusColors(
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.CheckCircle
        )
    }

    // Adaptive sizing
    val paddingHorizontal = if (isCompact) 6.dp else 10.dp
    val paddingVertical = if (isCompact) 2.dp else 4.dp
    val dotSize = if (isCompact) 6.dp else 8.dp
    val iconSize = if (isCompact) 12.dp else 14.dp
    
    // Base typography on M3 standards for proper scaling
    val baseTextStyle = MaterialTheme.typography.labelSmall

    Surface(
        modifier = modifier
            .testTag(status.testTagId)
            // A11y: Combine the semantics so screen readers read it as one cohesive state
            .semantics(mergeDescendants = true) {
                contentDescription = "Status: ${status.label}${if (subStatusLabel != null) ", $subStatusLabel" else ""}"
            },
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        // Native Surface border avoids anti-aliasing clipping bugs on rounded corners
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)) 
    ) {
        Row(
            modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showPulsingDot) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .alpha(if (status == DeliveryStatusType.COMPLETED) 1f else pulseAlpha)
                        .background(contentColor, CircleShape)
                        // A11y: Hide decorative dot from screen readers
                        .clearAndSetSemantics { } 
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Handled by Surface semantics
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = status.label.uppercase(),
                style = baseTextStyle,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = contentColor
            )

            if (!subStatusLabel.isNullOrBlank()) {
                Text(
                    text = " • $subStatusLabel",
                    style = baseTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.clearAndSetSemantics { } // Prevents reading "bullet" aloud
                )
            }
        }
    }
}

private data class StatusColors(
    val container: Color,
    val content: Color,
    val icon: ImageVector
)

// ============================================================================
// UI Previews (Crucial for modern Compose development)
// ============================================================================

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DeliveryStatusBadgePreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pending State
            DeliveryStatusBadge(
                status = DeliveryStatusType.PENDING,
                subStatusLabel = "Awaiting Driver"
            )
            
            // Active State (Compact)
            DeliveryStatusBadge(
                status = DeliveryStatusType.ACTIVE,
                subStatusLabel = "ETA 12 mins",
                isCompact = true
            )
            
            // Completed State (No Dot)
            DeliveryStatusBadge(
                status = DeliveryStatusType.COMPLETED,
                showPulsingDot = false
            )
        }
    }
}
