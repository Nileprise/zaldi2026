package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverLocationData
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun SimulatedMapView(
    modifier: Modifier = Modifier,
    isTrackingActiveRide: Boolean = false,
    pickupName: String = "Indiranagar 100ft Rd",
    dropoffName: String = "Koramangala 4th Block",
    etaMinutes: Int = 11,
    driverLocation: DriverLocationData? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE8ECEF))
            .testTag("simulated_map_view")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base terrain blocks
            drawRect(
                color = Color(0xFFF0F3F5),
                size = size
            )

            // Green park areas
            drawRoundRect(
                color = Color(0xFFD8EED8),
                topLeft = Offset(width * 0.05f, height * 0.1f),
                size = Size(width * 0.22f, height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFD8EED8),
                topLeft = Offset(width * 0.72f, height * 0.65f),
                size = Size(width * 0.24f, height * 0.25f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Secondary city street grid
            val secondaryRoadColor = Color(0xFFFFFFFF)
            val roadStroke = 18f

            // Horizontal roads
            drawLine(
                color = secondaryRoadColor,
                start = Offset(0f, height * 0.25f),
                end = Offset(width, height * 0.25f),
                strokeWidth = roadStroke
            )
            drawLine(
                color = secondaryRoadColor,
                start = Offset(0f, height * 0.5f),
                end = Offset(width, height * 0.5f),
                strokeWidth = roadStroke + 6f
            )
            drawLine(
                color = secondaryRoadColor,
                start = Offset(0f, height * 0.75f),
                end = Offset(width, height * 0.75f),
                strokeWidth = roadStroke
            )

            // Vertical roads
            drawLine(
                color = secondaryRoadColor,
                start = Offset(width * 0.2f, 0f),
                end = Offset(width * 0.2f, height),
                strokeWidth = roadStroke
            )
            drawLine(
                color = secondaryRoadColor,
                start = Offset(width * 0.5f, 0f),
                end = Offset(width * 0.5f, height),
                strokeWidth = roadStroke + 6f
            )
            drawLine(
                color = secondaryRoadColor,
                start = Offset(width * 0.8f, 0f),
                end = Offset(width * 0.8f, height),
                strokeWidth = roadStroke
            )

            // Diagonal arterial expressway
            val expresswayPath = Path().apply {
                moveTo(-20f, height * 0.8f)
                cubicTo(
                    width * 0.35f, height * 0.7f,
                    width * 0.65f, height * 0.3f,
                    width + 20f, height * 0.15f
                )
            }
            drawPath(
                path = expresswayPath,
                color = Color(0xFFFFE0B2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            // Planned Active Route Line
            val startPoint = Offset(width * 0.22f, height * 0.72f)
            val dropoffPoint = Offset(width * 0.78f, height * 0.26f)

            val routePath = Path().apply {
                moveTo(startPoint.x, startPoint.y)
                lineTo(width * 0.5f, height * 0.72f)
                lineTo(width * 0.5f, height * 0.35f)
                lineTo(dropoffPoint.x, height * 0.35f)
                lineTo(dropoffPoint.x, dropoffPoint.y)
            }

            // Glow under active route
            drawPath(
                path = routePath,
                color = AmberPrimary.copy(alpha = 0.25f),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Main route line
            drawPath(
                path = routePath,
                color = AmberPrimary,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Radar pulse at pickup
            drawCircle(
                color = SuccessGreen.copy(alpha = (1f - pulseProgress) * 0.5f),
                radius = 16f + (pulseProgress * 32f),
                center = startPoint
            )
            drawCircle(
                color = SuccessGreen,
                radius = 12f,
                center = startPoint
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = startPoint
            )

            // Destination Pin at dropoff
            drawCircle(
                color = AmberPrimary,
                radius = 14f,
                center = dropoffPoint
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = dropoffPoint
            )

            // Live Moving Vehicle Marker on route
            val t = if (isTrackingActiveRide) vehicleProgress else 0.45f
            val vehiclePos = when {
                t < 0.35f -> {
                    val subT = t / 0.35f
                    Offset(
                        startPoint.x + (width * 0.5f - startPoint.x) * subT,
                        height * 0.72f
                    )
                }
                t < 0.7f -> {
                    val subT = (t - 0.35f) / 0.35f
                    Offset(
                        width * 0.5f,
                        height * 0.72f + (height * 0.35f - height * 0.72f) * subT
                    )
                }
                else -> {
                    val subT = (t - 0.7f) / 0.3f
                    Offset(
                        width * 0.5f + (dropoffPoint.x - width * 0.5f) * subT,
                        height * 0.35f
                    )
                }
            }

            // Vehicle radar pulse
            drawCircle(
                color = LogisticsBlue.copy(alpha = 0.3f),
                radius = 24f,
                center = vehiclePos
            )
            drawCircle(
                color = LogisticsBlue,
                radius = 14f,
                center = vehiclePos
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = vehiclePos
            )

            // Nearby idle delivery vehicle dots
            val idleVehicles = listOf(
                Offset(width * 0.32f, height * 0.25f),
                Offset(width * 0.68f, height * 0.5f),
                Offset(width * 0.15f, height * 0.48f),
                Offset(width * 0.82f, height * 0.75f)
            )
            idleVehicles.forEach { pos ->
                drawCircle(
                    color = Color(0xFF4B5563),
                    radius = 7f,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = pos
                )
            }
        }

        // Top Status Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SuccessGreen, CircleShape)
                )
                val statusText = when {
                    driverLocation != null && isTrackingActiveRide ->
                        " Live GPS: ${driverLocation.speedKmh} km/h • ${driverLocation.formattedCoordinates}"
                    isTrackingActiveRide ->
                        " Driver Arriving in $etaMinutes min"
                    driverLocation != null ->
                        " Live Telemetry Active • ${driverLocation.formattedCoordinates}"
                    else ->
                        " Live GPS • 14 fleet partners nearby"
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // Location Recenter Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Recenter GPS",
                    tint = AmberPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
