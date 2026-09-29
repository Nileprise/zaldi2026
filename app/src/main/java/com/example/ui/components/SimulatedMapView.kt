package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.model.DriverLocationData

@Composable
fun SimulatedMapView(
    modifier: Modifier = Modifier,
    isTrackingActiveRide: Boolean = false,
    pickupName: String = "Pickup",
    dropoffName: String = "Drop-off",
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

    // Extracting colors from MaterialTheme for Canvas usage
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val parkColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    val expresswayColor = MaterialTheme.colorScheme.secondaryContainer
    val idleVehicleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    // Determine semantic status for screen readers and UI
    val statusText = when {
        driverLocation != null && isTrackingActiveRide ->
            "Live GPS: ${driverLocation.speedKmh} km/h • ${driverLocation.formattedCoordinates}"
        isTrackingActiveRide ->
            "Driver Arriving in $etaMinutes min"
        driverLocation != null ->
            "Live Telemetry Active • ${driverLocation.formattedCoordinates}"
        else ->
            "Live GPS • Fleet partners nearby"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceVariantColor)
            .semantics {
                contentDescription = "Simulated map view. $statusText"
            }
            .testTag("simulated_map_view")
    ) {
        // Modernized Canvas: drawWithCache prevents Path reallocation on every animation frame
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val height = size.height

                    // Pre-calculate paths once per layout pass
                    val expresswayPath = Path().apply {
                        moveTo(-20f, height * 0.8f)
                        cubicTo(
                            width * 0.35f, height * 0.7f,
                            width * 0.65f, height * 0.3f,
                            width + 20f, height * 0.15f
                        )
                    }

                    val startPoint = Offset(width * 0.22f, height * 0.72f)
                    val dropoffPoint = Offset(width * 0.78f, height * 0.26f)

                    val routePath = Path().apply {
                        moveTo(startPoint.x, startPoint.y)
                        lineTo(width * 0.5f, height * 0.72f)
                        lineTo(width * 0.5f, height * 0.35f)
                        lineTo(dropoffPoint.x, height * 0.35f)
                        lineTo(dropoffPoint.x, dropoffPoint.y)
                    }

                    val idleVehicles = listOf(
                        Offset(width * 0.32f, height * 0.25f),
                        Offset(width * 0.68f, height * 0.5f),
                        Offset(width * 0.15f, height * 0.48f),
                        Offset(width * 0.82f, height * 0.75f)
                    )

                    onDrawBehind {
                        // 1. Base terrain
                        drawRect(color = surfaceVariantColor, size = size)

                        // 2. Parks
                        drawRoundRect(
                            color = parkColor,
                            topLeft = Offset(width * 0.05f, height * 0.1f),
                            size = Size(width * 0.22f, height * 0.28f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = parkColor,
                            topLeft = Offset(width * 0.72f, height * 0.65f),
                            size = Size(width * 0.24f, height * 0.25f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )

                        // 3. Grid Roads
                        val roadStroke = 18f
                        
                        // Horizontal roads
                        drawLine(surfaceColor, Offset(0f, height * 0.25f), Offset(width, height * 0.25f), roadStroke)
                        drawLine(surfaceColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), roadStroke + 6f)
                        drawLine(surfaceColor, Offset(0f, height * 0.75f), Offset(width, height * 0.75f), roadStroke)
                        
                        // Vertical roads
                        drawLine(surfaceColor, Offset(width * 0.2f, 0f), Offset(width * 0.2f, height), roadStroke)
                        drawLine(surfaceColor, Offset(width * 0.5f, 0f), Offset(width * 0.5f, height), roadStroke + 6f)
                        drawLine(surfaceColor, Offset(width * 0.8f, 0f), Offset(width * 0.8f, height), roadStroke)

                        // 4. Expressway
                        drawPath(expresswayPath, expresswayColor, style = Stroke(width = 24f, cap = StrokeCap.Round))

                        // 5. Active Route
                        drawPath(routePath, primaryColor.copy(alpha = 0.25f), style = Stroke(width = 16f, cap = StrokeCap.Round))
                        drawPath(routePath, primaryColor, style = Stroke(width = 8f, cap = StrokeCap.Round))

                        // 6. Pickup Location (Pulsing)
                        drawCircle(Color.Green.copy(alpha = (1f - pulseProgress) * 0.5f), radius = 16f + (pulseProgress * 32f), center = startPoint)
                        drawCircle(Color.Green, radius = 12f, center = startPoint)
                        drawCircle(Color.White, radius = 5f, center = startPoint)

                        // 7. Dropoff Location
                        drawCircle(primaryColor, radius = 14f, center = dropoffPoint)
                        drawCircle(Color.White, radius = 6f, center = dropoffPoint)

                        // 8. Dynamic Vehicle Calculation (Reads animated state)
                        val t = if (isTrackingActiveRide) vehicleProgress else 0.45f
                        val vehiclePos = when {
                            t < 0.35f -> Offset(startPoint.x + (width * 0.5f - startPoint.x) * (t / 0.35f), height * 0.72f)
                            t < 0.7f -> Offset(width * 0.5f, height * 0.72f + (height * 0.35f - height * 0.72f) * ((t - 0.35f) / 0.35f))
                            else -> Offset(width * 0.5f + (dropoffPoint.x - width * 0.5f) * ((t - 0.7f) / 0.3f), height * 0.35f)
                        }

                        drawCircle(Color.Blue.copy(alpha = 0.3f), radius = 24f, center = vehiclePos)
                        drawCircle(Color.Blue, radius = 14f, center = vehiclePos)
                        drawCircle(Color.White, radius = 6f, center = vehiclePos)

                        // 9. Idle Vehicles
                        idleVehicles.forEach { pos ->
                            drawCircle(idleVehicleColor, radius = 7f, center = pos)
                            drawCircle(surfaceColor, radius = 3f, center = pos)
                        }
                    }
                }
        )

        // Top Status Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Green, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Location Recenter Button (Modernized to FAB)
        FloatingActionButton(
            onClick = { /* Recenter logic */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "Recenter Map on GPS"
            )
        }
    }
}

// ============================================================================
// UI Previews
// ============================================================================

@Preview(name = "Light Mode", showBackground = true, widthDp = 400, heightDp = 300)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun SimulatedMapViewPreview() {
    MaterialTheme {
        SimulatedMapView(
            isTrackingActiveRide = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
