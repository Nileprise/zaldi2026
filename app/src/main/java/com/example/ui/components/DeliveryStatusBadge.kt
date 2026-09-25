package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

enum class DeliveryStatusType(
    val label: String,
    val testTagId: String
) {
    PENDING("Pending", "status_indicator_pending"),
    ACTIVE("Active", "status_indicator_active"),
    COMPLETED("Completed", "status_indicator_completed");

    companion object {
        fun fromOrderStatus(status: String): DeliveryStatusType {
            return when (status.uppercase().trim()) {
                "COMPLETED", "DELIVERED", "FINISHED" -> COMPLETED
                "IN_TRANSIT", "ARRIVED_AT_PICKUP", "ACCEPTED", "PICKED_UP", "ACTIVE" -> ACTIVE
                else -> PENDING
            }
        }
    }
}

@Composable
fun DeliveryStatusBadge(
    status: DeliveryStatusType,
    modifier: Modifier = Modifier,
    subStatusLabel: String? = null,
    isCompact: Boolean = false,
    showPulsingDot: Boolean = true
) {
    // Pulse animation for Pending and Active
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

    val (containerColor, contentColor, borderColor, icon) = when (status) {
        DeliveryStatusType.PENDING -> StatusColors(
            container = Color(0xFFFFF3E0),
            content = Color(0xFFE65100),
            border = Color(0xFFFFB74D),
            icon = Icons.Default.HourglassTop
        )
        DeliveryStatusType.ACTIVE -> StatusColors(
            container = Color(0xFFE3F2FD),
            content = Color(0xFF0D47A1),
            border = Color(0xFF90CAF9),
            icon = Icons.Default.Navigation
        )
        DeliveryStatusType.COMPLETED -> StatusColors(
            container = Color(0xFFE8F5E9),
            content = Color(0xFF2E7D32),
            border = Color(0xFFA5D6A7),
            icon = Icons.Default.CheckCircle
        )
    }

    val paddingHorizontal = if (isCompact) 8.dp else 10.dp
    val paddingVertical = if (isCompact) 3.dp else 5.dp
    val fontSize = if (isCompact) 10.sp else 11.sp
    val iconSize = if (isCompact) 13.dp else 15.dp
    val dotSize = if (isCompact) 6.dp else 8.dp

    Surface(
        modifier = modifier
            .testTag(status.testTagId)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pulsing live indicator dot for Pending and Active
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

            // Primary Status text
            Text(
                text = status.label.uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = contentColor
            )

            // Optional Sub-status (e.g. " • In Transit" or " • Arrived")
            if (!subStatusLabel.isNullOrBlank()) {
                Text(
                    text = " • $subStatusLabel",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
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
