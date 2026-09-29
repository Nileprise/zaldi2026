package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Driver
import com.example.data.model.LocationPoint
import kotlin.math.atan2

@Composable
fun InteractiveMapView(
    modifier: Modifier = Modifier,
    activeBooking: Booking? = null,
    userLocation: LocationPoint? = null,
    allDrivers: List<Driver> = emptyList(),
    showAllDrivers: Boolean = false,
    selectedDriver: Driver? = null,
    onDriverSelected: ((Driver) -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.6f, 3.0f)
        offset += offsetChange
    }

    // Animation for pulse halos and dashed route lines
    val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .transformable(state = transformState)
            .pointerInput(allDrivers, showAllDrivers) {
                detectTapGestures { tapOffset ->
                    if (showAllDrivers && onDriverSelected != null) {
                        // Project tap to find closest driver
                        val clicked = findClickedDriver(tapOffset, size.width, size.height, allDrivers, offset, scale)
                        if (clicked != null) {
                            onDriverSelected(clicked)
                        }
                    }
                }
            }
            .testTag("interactive_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw stylized city map features
            drawCityBackground(width, height, offset, scale)

            // 2. Draw user live GPS position if available
            if (userLocation != null) {
                val gpsPos = latLngToCanvas(userLocation.latitude, userLocation.longitude, width, height, offset, scale)
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                    radius = (pulseRadius + 12f) * scale,
                    center = gpsPos
                )
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 9f * scale,
                    center = gpsPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5f * scale,
                    center = gpsPos
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "YOU ARE HERE",
                    topLeft = Offset(gpsPos.x - 30f, gpsPos.y + 12f),
                    style = TextStyle(color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }

            // 3. Draw active booking route and markers if present
            if (activeBooking != null) {
                drawActiveBookingRoute(
                    booking = activeBooking,
                    width = width,
                    height = height,
                    offset = offset,
                    scale = scale,
                    pulseRadius = pulseRadius,
                    dashPhase = dashPhase,
                    textMeasurer = textMeasurer
                )
            }

            // 3. Draw fleet drivers if admin/dispatch mode
            if (showAllDrivers) {
                drawFleetDrivers(
                    drivers = allDrivers,
                    width = width,
                    height = height,
                    offset = offset,
                    scale = scale,
                    selectedDriver = selectedDriver,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Recenter FAB
        FloatingActionButton(
            onClick = {
                scale = 1f
                offset = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("map_recenter_fab"),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map")
        }

        // Selected driver popup card if tapped
        if (selectedDriver != null && showAllDrivers) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .testTag("selected_driver_overlay"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selectedDriver.isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedDriver.isOnline) "ONLINE" else "OFFLINE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = selectedDriver.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${selectedDriver.vehicleModel} • ${selectedDriver.vehiclePlate}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Convert Lat/Lng in the city bounding box (Bangalore/Urban region ~ 12.9 to 13.05 Lat, 77.5 to 77.7 Lng)
// into localized Canvas coordinates (pixels)
private fun latLngToCanvas(
    lat: Double,
    lng: Double,
    width: Float,
    height: Float,
    offset: Offset,
    scale: Float
): Offset {
    val minLat = 12.9200
    val maxLat = 13.0400
    val minLng = 77.5100
    val maxLng = 77.7100

    val normX = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.05f, 0.95f)
    val normY = (1.0 - (lat - minLat) / (maxLat - minLat)).toFloat().coerceIn(0.05f, 0.95f)

    val centerX = width / 2f
    val centerY = height / 2f

    val rawX = normX * width
    val rawY = normY * height

    val finalX = centerX + (rawX - centerX) * scale + offset.x
    val finalY = centerY + (rawY - centerY) * scale + offset.y

    return Offset(finalX, finalY)
}

private fun DrawScope.drawCityBackground(
    width: Float,
    height: Float,
    offset: Offset,
    scale: Float
) {
    // Water body (river curving through city)
    val riverPath = Path().apply {
        moveTo(0f, height * 0.75f)
        cubicTo(
            width * 0.35f, height * 0.85f,
            width * 0.65f, height * 0.55f,
            width, height * 0.65f
        )
    }
    drawPath(
        path = riverPath,
        color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
        style = Stroke(width = 32f * scale, cap = StrokeCap.Round)
    )

    // Secondary road networks (grid pattern)
    val gridColor = Color(0xFF1E293B)
    val majorRoadColor = Color(0xFF334155)

    val stepX = (width / 8f) * scale
    val stepY = (height / 8f) * scale

    var curX = (offset.x % stepX)
    while (curX < width) {
        drawLine(
            color = gridColor,
            start = Offset(curX, 0f),
            end = Offset(curX, height),
            strokeWidth = 2f
        )
        curX += stepX
    }

    var curY = (offset.y % stepY)
    while (curY < height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, curY),
            end = Offset(width, curY),
            strokeWidth = 2f
        )
        curY += stepY
    }

    // Major Express Highways
    drawLine(
        color = majorRoadColor,
        start = Offset(0f, height * 0.4f + offset.y),
        end = Offset(width, height * 0.4f + offset.y),
        strokeWidth = 8f * scale
    )
    drawLine(
        color = majorRoadColor,
        start = Offset(width * 0.45f + offset.x, 0f),
        end = Offset(width * 0.45f + offset.x, height),
        strokeWidth = 8f * scale
    )
    // Diagonal bypass corridor
    drawLine(
        color = Color(0xFF475569).copy(alpha = 0.6f),
        start = Offset(0f, height * 0.2f),
        end = Offset(width, height * 0.85f),
        strokeWidth = 6f * scale
    )
}

private fun DrawScope.drawActiveBookingRoute(
    booking: Booking,
    width: Float,
    height: Float,
    offset: Offset,
    scale: Float,
    pulseRadius: Float,
    dashPhase: Float,
    textMeasurer: TextMeasurer
) {
    val pickupPos = latLngToCanvas(booking.pickupLat, booking.pickupLng, width, height, offset, scale)
    val dropPos = latLngToCanvas(booking.dropLat, booking.dropLng, width, height, offset, scale)
    val driverPos = latLngToCanvas(booking.driverCurrentLat, booking.driverCurrentLng, width, height, offset, scale)

    // Multi-segment road waypoints to simulate realistic urban routing along highway corridors
    val wp1 = Offset(pickupPos.x + (dropPos.x - pickupPos.x) * 0.35f + 35f * scale, pickupPos.y + (dropPos.y - pickupPos.y) * 0.25f - 25f * scale)
    val wp2 = Offset(pickupPos.x + (dropPos.x - pickupPos.x) * 0.70f - 20f * scale, pickupPos.y + (dropPos.y - pickupPos.y) * 0.75f + 20f * scale)

    val routePath = Path().apply {
        moveTo(pickupPos.x, pickupPos.y)
        lineTo(wp1.x, wp1.y)
        lineTo(wp2.x, wp2.y)
        lineTo(dropPos.x, dropPos.y)
    }

    // 1. Draw glowing route baseline
    drawPath(
        path = routePath,
        color = Color(0xFF0284C7).copy(alpha = 0.35f),
        style = Stroke(width = 12f * scale, cap = StrokeCap.Round)
    )

    // 2. Draw animated dashed route line
    drawPath(
        path = routePath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF10B981), Color(0xFF38BDF8), Color(0xFFF59E0B))
        ),
        style = Stroke(
            width = 5f * scale,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), dashPhase)
        )
    )

    // Draw route distance badge at center waypoint
    val midTag = Offset((wp1.x + wp2.x) / 2f, (wp1.y + wp2.y) / 2f)
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 18f * scale,
        center = midTag
    )
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 18f * scale,
        center = midTag,
        style = Stroke(width = 1.5f * scale)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = "${booking.distanceKm}k",
        topLeft = Offset(midTag.x - 10f, midTag.y - 8f),
        style = TextStyle(color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Black)
    )

    // 3. Draw Pickup Pin (Green)
    drawCircle(
        color = Color(0xFF10B981).copy(alpha = 0.25f),
        radius = pulseRadius * scale,
        center = pickupPos
    )
    drawCircle(
        color = Color(0xFF10B981),
        radius = 10f * scale,
        center = pickupPos
    )
    drawCircle(
        color = Color.White,
        radius = 4f * scale,
        center = pickupPos
    )

    // Pickup Label
    drawText(
        textMeasurer = textMeasurer,
        text = "PICKUP",
        topLeft = Offset(pickupPos.x - 24f, pickupPos.y + 14f),
        style = TextStyle(
            color = Color(0xFF34D399),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )

    // 4. Draw Drop Pin (Amber / Coral Red)
    drawCircle(
        color = Color(0xFFEF4444).copy(alpha = 0.25f),
        radius = 18f * scale,
        center = dropPos
    )
    drawCircle(
        color = Color(0xFFEF4444),
        radius = 11f * scale,
        center = dropPos
    )
    drawCircle(
        color = Color.White,
        radius = 4f * scale,
        center = dropPos
    )

    // Drop Label
    drawText(
        textMeasurer = textMeasurer,
        text = "DROP",
        topLeft = Offset(dropPos.x - 18f, dropPos.y + 14f),
        style = TextStyle(
            color = Color(0xFFF87171),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )

    // 5. Draw Driver Vehicle Marker
    val isTripActive = booking.status in listOf(
        Booking.STATUS_ASSIGNED,
        Booking.STATUS_ARRIVED_PICKUP,
        Booking.STATUS_STARTED,
        Booking.STATUS_IN_TRANSIT
    )

    if (isTripActive) {
        // Vehicle radar ripple
        drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = 0.3f),
            radius = (pulseRadius + 8f) * scale,
            center = driverPos
        )
        // Vehicle circle background
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = 16f * scale,
            center = driverPos
        )
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 12f * scale,
            center = driverPos
        )
        // Center core
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 6f * scale,
            center = driverPos
        )

        // Speed badge
        val speedText = if (booking.status == Booking.STATUS_IN_TRANSIT) "42 km/h" else "ARRIVING"
        drawText(
            textMeasurer = textMeasurer,
            text = speedText,
            topLeft = Offset(driverPos.x - 22f, driverPos.y - 28f),
            style = TextStyle(
                color = Color(0xFFFBBF24),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

private fun DrawScope.drawFleetDrivers(
    drivers: List<Driver>,
    width: Float,
    height: Float,
    offset: Offset,
    scale: Float,
    selectedDriver: Driver?,
    textMeasurer: TextMeasurer
) {
    drivers.forEach { driver ->
        val pos = latLngToCanvas(driver.currentLat, driver.currentLng, width, height, offset, scale)
        val isSelected = selectedDriver?.id == driver.id
        val statusColor = when {
            driver.currentBookingId != null -> Color(0xFF3B82F6) // Active blue
            driver.isOnline -> Color(0xFF10B981) // Available green
            else -> Color(0xFFEF4444) // Offline red
        }

        if (isSelected) {
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 24f * scale,
                center = pos
            )
        }

        // Driver pin outer ring
        drawCircle(
            color = statusColor,
            radius = 14f * scale,
            center = pos
        )
        // Inner slate fill
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 10f * scale,
            center = pos
        )
        // Status dot
        drawCircle(
            color = statusColor,
            radius = 5f * scale,
            center = pos
        )

        // Driver ID short tag
        drawText(
            textMeasurer = textMeasurer,
            text = driver.name.split(" ").firstOrNull() ?: driver.id,
            topLeft = Offset(pos.x - 20f, pos.y + 14f),
            style = TextStyle(
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun findClickedDriver(
    tap: Offset,
    width: Int,
    height: Int,
    drivers: List<Driver>,
    offset: Offset,
    scale: Float
): Driver? {
    val clickThreshold = 50f
    return drivers.firstOrNull { driver ->
        val pos = latLngToCanvas(
            driver.currentLat, driver.currentLng,
            width.toFloat(), height.toFloat(),
            offset, scale
        )
        val distSq = (pos.x - tap.x) * (pos.x - tap.x) + (pos.y - tap.y) * (pos.y - tap.y)
        distSq <= clickThreshold * clickThreshold
    }
}
