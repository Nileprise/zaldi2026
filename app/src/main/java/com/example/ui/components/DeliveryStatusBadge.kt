package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ... (Keep existing DeliveryStatusType enum)

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

    // Updated to use the modern corporate palette
    val (containerColor, contentColor, borderColor, icon) = when (status) {
        DeliveryStatusType.PENDING -> StatusColors(
            container = SurfaceTertiary,
            content = WarningAmber,
            border = BorderLight,
            icon = Icons.Default.HourglassTop
        )
        DeliveryStatusType.ACTIVE -> StatusColors(
            container = AccentBlueContainer,
            content = AccentBlue,
            border = Color(0xFFBFDBFE), // Subtle blue border
            icon = Icons.Default.Navigation
        )
        DeliveryStatusType.COMPLETED -> StatusColors(
            container = SuccessContainer,
            content = SuccessGreen,
            border = Color(0xFFA7F3D0), // Subtle green border
            icon = Icons.Default.CheckCircle
        )
    }

    val paddingHorizontal = if (isCompact) 6.dp else 8.dp
    val paddingVertical = if (isCompact) 2.dp else 4.dp
    val fontSize = if (isCompact) 10.sp else 11.sp
    val iconSize = if (isCompact) 12.dp else 14.dp
    val dotSize = if (isCompact) 6.dp else 8.dp

    Surface(
        modifier = modifier
            .testTag(status.testTagId)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp)), // Sharpened corners to 6dp
        shape = RoundedCornerShape(6.dp),
        color = containerColor
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
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = status.label,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = status.label.uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold, // Reduced from ExtraBold
                letterSpacing = 0.5.sp,
                color = contentColor
            )

            if (!subStatusLabel.isNullOrBlank()) {
                Text(
                    text = " • $subStatusLabel",
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private data class StatusColors(
    val container: Color,
    val content: Color,
    val border: Color,
    val icon: ImageVector
)
