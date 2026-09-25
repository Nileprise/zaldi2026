package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverLocationData
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
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

@Composable
fun GoogleMapsView(
    driverLocation: DriverLocationData?,
    modifier: Modifier = Modifier,
    pickupLatLng: LatLng = LatLng(12.9784, 77.6408), // Indiranagar 100ft Rd
    dropoffLatLng: LatLng = LatLng(12.9352, 77.6245), // Koramangala 4th Block
    pickupTitle: String = "Pickup: Indiranagar",
    dropoffTitle: String = "Drop-off: Koramangala",
    isInteractive: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()

    val currentDriverLatLng = remember(driverLocation?.latitude, driverLocation?.longitude) {
        if (driverLocation != null) {
            LatLng(driverLocation.latitude, driverLocation.longitude)
        } else {
            LatLng(12.9710, 77.6350) // Default midpoint along freight route
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentDriverLatLng, 14.5f)
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isTrafficEnabled by remember { mutableStateOf(true) }
    var isVectorFallback by remember { mutableStateOf(true) }

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

    if (isVectorFallback) {
        Box(modifier = modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))) {
            SimulatedMapView(
                modifier = Modifier.fillMaxSize(),
                isTrackingActiveRide = true,
                pickupName = pickupTitle,
                dropoffName = dropoffTitle,
                driverLocation = driverLocation
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { isVectorFallback = false }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Google Maps",
                        tint = AmberPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Maps SDK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }
        return
    }

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

    // Route points from pickup -> live driver location -> dropoff
    val routePoints = remember(currentDriverLatLng) {
        listOf(
            pickupLatLng,
            LatLng(12.9650, 77.6380),
            currentDriverLatLng,
            LatLng(12.9480, 77.6310),
            dropoffLatLng
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
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
                snippet = "Scheduled Goods Pickup",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )

            // Dropoff Marker
            Marker(
                state = MarkerState(position = dropoffLatLng),
                title = dropoffTitle,
                snippet = "Consignee Destination",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )

            // Live Driver GPS Marker
            Marker(
                state = MarkerState(position = currentDriverLatLng),
                title = "Driver: Ravi Kumar (Tata Ace)",
                snippet = "Speed: ${driverLocation?.speedKmh ?: 24.5f} km/h • KA 05 MX 2190",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )

            // Accuracy Radius Circle
            Circle(
                center = currentDriverLatLng,
                radius = (driverLocation?.accuracyMeters?.toDouble() ?: 20.0).coerceAtLeast(35.0),
                fillColor = LogisticsBlue.copy(alpha = 0.2f),
                strokeColor = LogisticsBlue,
                strokeWidth = 2f
            )

            // Route Polyline Glow
            Polyline(
                points = routePoints,
                color = AmberPrimary.copy(alpha = 0.4f),
                width = 16f
            )

            // Main Polyline
            Polyline(
                points = routePoints,
                color = AmberPrimary,
                width = 8f
            )
        }

        // Overlay Controls (when interactive)
        if (isInteractive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable { isVectorFallback = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Vector Map",
                            tint = LogisticsBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vector Map",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LogisticsBlue
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
                    },
                    containerColor = Color.White,
                    contentColor = AmberPrimary,
                    modifier = Modifier.testTag("toggle_map_type_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map Style"
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            runCatching {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(currentDriverLatLng, 16f),
                                    durationMs = 600
                                )
                            }
                        }
                    },
                    containerColor = AmberPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("recenter_driver_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Recenter Driver GPS",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
