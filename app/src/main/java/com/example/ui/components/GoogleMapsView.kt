package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DriverLocationData
import com.example.util.GoogleMapsRoutingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toPersistentList

@Composable
fun GoogleMapsView(
    driverLocation: DriverLocationData?,
    modifier: Modifier = Modifier,
    pickupLatLng: LatLng = LatLng(12.9784, 77.6408), // Default: Indiranagar 100ft Rd
    dropoffLatLng: LatLng = LatLng(12.9352, 77.6245), // Default: Koramangala 4th Block
    pickupTitle: String = "Pickup",
    dropoffTitle: String = "Drop-off",
    pickupPlaceId: String? = null,
    dropoffPlaceId: String? = null,
    customWaypoints: List<LatLng>? = null,
    vehicleType: String = "tata",
    isInteractive: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()

    // Determine road polyline connecting pickup to dropoff
    val activeRouteWaypoints = remember(pickupLatLng, dropoffLatLng, customWaypoints) {
        if (!customWaypoints.isNullOrEmpty()) {
            customWaypoints.toPersistentList()
        } else {
            listOf(
                pickupLatLng,
                LatLng(
                    (pickupLatLng.latitude * 2 + dropoffLatLng.latitude) / 3,
                    (pickupLatLng.longitude * 2 + dropoffLatLng.longitude) / 3
                ),
                LatLng(
                    (pickupLatLng.latitude + dropoffLatLng.latitude * 2) / 3,
                    (pickupLatLng.longitude + dropoffLatLng.longitude * 2) / 3
                ),
                dropoffLatLng
            ).toPersistentList()
        }
    }

    // Apply Roads API: Snap-to-Roads algorithm to GPS driver coordinate
    val currentDriverLatLng = remember(driverLocation?.latitude, driverLocation?.longitude, activeRouteWaypoints) {
        if (driverLocation != null) {
            val raw = LatLng(driverLocation.latitude, driverLocation.longitude)
            GoogleMapsRoutingService.snapToRoad(raw, activeRouteWaypoints)
        } else {
            // Fallback to Midpoint on route if no driver location exists
            activeRouteWaypoints[activeRouteWaypoints.size / 2]
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentDriverLatLng, 14.2f)
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isTrafficEnabled by remember { mutableStateOf(true) }
    var isVectorFallback by remember { mutableStateOf(true) } // Toggle between vector/real map

    // Auto-pan camera smoothly when driver moves in interactive mode
    LaunchedEffect(currentDriverLatLng, isVectorFallback) {
        if (!isVectorFallback) {
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLng(currentDriverLatLng),
                    durationMs = 800
                )
            }
        }
    }

    // --- Vector Fallback View ---
    if (isVectorFallback) {
        Box(modifier = modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) {
            SimulatedMapView(
                modifier = Modifier.fillMaxSize(),
                isTrackingActiveRide = true,
                pickupName = pickupTitle,
                dropoffName = dropoffTitle,
                driverLocation = driverLocation
            )
            
            // Modernized button for Accessibility and Material 3 compliance
            ElevatedButton(
                onClick = { isVectorFallback = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Switch to Google Maps",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Real Map", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // --- Google Maps Integration ---
    val mapProperties = remember(mapType, isTrafficEnabled) {
        MapProperties(
            mapType = mapType,
            isTrafficEnabled = isTrafficEnabled,
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember(isInteractive) {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            scrollGesturesEnabled = isInteractive,
            zoomGesturesEnabled = isInteractive,
            tiltGesturesEnabled = isInteractive,
            rotationGesturesEnabled = isInteractive
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .testTag("google_maps_view_container")
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // Pickup Marker
            Marker(
                state = MarkerState(position = pickupLatLng),
                title = pickupTitle,
                snippet = pickupPlaceId?.let { "ID: ${it.take(8)}..." },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )

            // Dropoff Marker
            Marker(
                state = MarkerState(position = dropoffLatLng),
                title = dropoffTitle,
                snippet = dropoffPlaceId?.let { "ID: ${it.take(8)}..." },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )

            // Live Driver GPS Marker (Snapped to Road)
            val driverHue = when (vehicleType) {
                "bike" -> BitmapDescriptorFactory.HUE_VIOLET
                "auto" -> BitmapDescriptorFactory.HUE_YELLOW
                else -> BitmapDescriptorFactory.HUE_AZURE
            }
            
            Marker(
                state = MarkerState(position = currentDriverLatLng),
                title = "Assigned Driver ($vehicleType)",
                snippet = "Speed: ${driverLocation?.speedKmh ?: 0.0f} km/h",
                icon = BitmapDescriptorFactory.defaultMarker(driverHue)
            )

            // Accuracy Radius Circle (Only show if driver location exists)
            if (driverLocation != null) {
                Circle(
                    center = currentDriverLatLng,
                    radius = driverLocation.accuracyMeters.toDouble().coerceAtLeast(20.0),
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2f
                )
            }

            // Route Polyline Glow (Shadow effect)
            Polyline(
                points = activeRouteWaypoints,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                width = 16f
            )

            // Main Route Polyline (Core path)
            Polyline(
                points = activeRouteWaypoints,
                color = MaterialTheme.colorScheme.primary,
                width = 8f
            )
        }

        // Overlay Controls (when interactive)
        if (isInteractive) {
            // Switch back to Vector Map
            ElevatedButton(
                onClick = { isVectorFallback = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Switch to Vector Map",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vector Map", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            // Map Style & Traffic Controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { isTrafficEnabled = !isTrafficEnabled },
                    containerColor = if (isTrafficEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isTrafficEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("toggle_traffic_button")
                ) {
                    Icon(imageVector = Icons.Default.Traffic, contentDescription = "Toggle Traffic")
                }

                SmallFloatingActionButton(
                    onClick = { mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("toggle_map_type_button")
                ) {
                    Icon(imageVector = Icons.Default.Layers, contentDescription = "Toggle Map Style")
                }
            }

            // Recenter Button
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        runCatching {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentDriverLatLng, 15.5f),
                                durationMs = 600
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("recenter_driver_gps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Recenter on Driver"
                )
            }
        }
    }
}
