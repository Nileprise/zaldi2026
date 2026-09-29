package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.SimulatedMapView
import com.google.android.gms.maps.model.LatLng

@Composable
fun LiveMapScreen(
    driverLocation: DriverLocationData?,
    isServiceRunning: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    activeOrder: BookingOrder? = null,
    pickupLatLng: LatLng? = null,
    dropoffLatLng: LatLng? = null,
    pickupTitle: String? = null,
    dropoffTitle: String? = null,
    driverName: String = "Assigned Driver",
    vehicleInfo: String = "Commercial Fleet"
) {
    var isGoogleMapsMode by remember { mutableStateOf(true) }

    // Resolve coordinates from active order or explicit parameters
    val resolvedPickup = remember(activeOrder, pickupLatLng) {
        pickupLatLng ?: activeOrder?.let { LatLng(it.pickupLat, it.pickupLng) }
    }
    val resolvedDropoff = remember(activeOrder, dropoffLatLng) {
        dropoffLatLng ?: activeOrder?.let { LatLng(it.dropoffLat, it.dropoffLng) }
    }

    val resolvedPickupTitle = pickupTitle ?: activeOrder?.pickupAddress ?: "Origin"
    val resolvedDropoffTitle = dropoffTitle ?: activeOrder?.dropoffAddress ?: "Destination"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Base Map Layer ---
        if (isGoogleMapsMode && resolvedPickup != null && resolvedDropoff != null) {
            GoogleMapsView(
                driverLocation = driverLocation,
                modifier = Modifier.fillMaxSize(),
                pickupLatLng = resolvedPickup,
                dropoffLatLng = resolvedDropoff,
                pickupTitle = resolvedPickupTitle,
                dropoffTitle = resolvedDropoffTitle,
                pickupPlaceId = activeOrder?.pickupPlaceId,
                dropoffPlaceId = activeOrder?.dropoffPlaceId,
                vehicleType = activeOrder?.vehicleTierId ?: "standard",
                isInteractive = true
            )
        } else {
            SimulatedMapView(
                driverLocation = driverLocation,
                modifier = Modifier.fillMaxSize(),
                isTrackingActiveRide = activeOrder != null,
                pickupName = resolvedPickupTitle,
                dropoffName = resolvedDropoffTitle,
                etaMinutes = activeOrder?.etaMinutes ?: 0
            )
        }

        // --- Top Floating Header HUD ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("live_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "Live Telemetry Tracking",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        if (isServiceRunning) Color(0xFF059669) else MaterialTheme.colorScheme.tertiary,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServiceRunning) "Foreground GPS streaming" else "Standard provider",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Map Style Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapToggleOption(
                            text = "SDK",
                            isSelected = isGoogleMapsMode,
                            onClick = { isGoogleMapsMode = true }
                        )
                        MapToggleOption(
                            text = "Vector",
                            isSelected = !isGoogleMapsMode,
                            onClick = { isGoogleMapsMode = false }
                        )
                    }
                }
            }
        }

        // --- Bottom Telemetry Dashboard Card ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SNAP-TO-ROADS REAL-TIME TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${activeOrder?.driverName ?: driverName} • ${activeOrder?.driverVehicleNumber ?: vehicleInfo}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeOrder != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (activeOrder != null) "In Transit" else "Monitoring",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (activeOrder != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryMetric(
                        icon = Icons.Default.Speed,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Speed",
                        value = "${driverLocation?.speedKmh?.toInt() ?: 0} km/h"
                    )

                    TelemetryMetric(
                        icon = Icons.Default.Navigation,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        label = "Heading",
                        value = formatHeading(driverLocation?.bearing)
                    )

                    TelemetryMetric(
                        icon = Icons.Default.GpsFixed,
                        iconTint = Color(0xFF059669),
                        label = "GPS Accuracy",
                        value = "±${driverLocation?.accuracyMeters?.toInt() ?: 0}m"
                    )
                }
            }
        }
    }
}

@Composable
private fun MapToggleOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier.clickable(role = Role.Tab, onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TelemetryMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatHeading(bearing: Float?): String {
    if (bearing == null || bearing == 0f) return "--"
    val normalized = (bearing % 360 + 360) % 360
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((normalized + 22.5) / 45).toInt() % 8
    return "${normalized.toInt()}° ${directions[index]}"
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LiveMapScreenPreview() {
    MaterialTheme {
        LiveMapScreen(
            driverLocation = DriverLocationData(
                latitude = 12.9784,
                longitude = 77.6408,
                speedKmh = 34f,
                bearing = 135f,
                accuracyMeters = 4f
            ),
            isServiceRunning = true,
            onBack = {}
        )
    }
}
