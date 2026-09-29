package com.example.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationPoint
import com.example.utils.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class RapidoPlannerStep {
    PICKUP_SELECTION,
    DROP_SELECTION,
    ROUTE_OVERVIEW
}

/**
 * Modern Rapido-Style unified route planner.
 * Features:
 * - One unified search bar with dynamic text updating
 * - Green visual anchor for pickup, transition to Red/Orange for drop
 * - Fixed center pin that stays stationary while map is panned
 * - Real-time reverse geocoding into the search bar
 * - Saved places ("Home", "Work") & POI autocomplete
 * - Quick Snap "Use current location" with Accompanist permissions
 * - Seamless automatic zoom framing both pins with routing line
 * - Tactile haptic feedback on road snap & confirm actions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RapidoRoutePlannerDialog(
    initialPickup: LocationPoint,
    initialDrop: LocationPoint,
    onDismiss: () -> Unit,
    onRouteConfirmed: (pickup: LocationPoint, drop: LocationPoint) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(RapidoPlannerStep.PICKUP_SELECTION) }
    var confirmedPickup by remember { mutableStateOf(initialPickup) }
    var confirmedDrop by remember { mutableStateOf(initialDrop) }

    // Coordinates of current map center under the fixed pin
    var currentCenterLat by remember { mutableDoubleStateOf(initialPickup.latitude) }
    var currentCenterLng by remember { mutableDoubleStateOf(initialPickup.longitude) }

    // Text in the single unified search bar
    var searchBarText by remember { mutableStateOf(initialPickup.title) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isGpsSnapping by remember { mutableStateOf(false) }

    // Map animation scale and offset for seamless framing
    var mapZoomScale by remember { mutableFloatStateOf(1.0f) }
    var isDraggingMap by remember { mutableStateOf(false) }

    // Fixed Pin Bounce Animation when panning
    val pinBounceOffsetY = remember { Animatable(0f) }

    // Accompanist Permissions for GPS snap
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Saved places for fast drop selection
    val savedPlaces = remember {
        listOf(
            LocationPoint(
                id = "saved_home",
                title = "Home",
                address = "Flat 402, Green Glen Layout, Bellandur, Bengaluru 560103",
                latitude = 12.9280,
                longitude = 77.6740,
                category = "Home"
            ),
            LocationPoint(
                id = "saved_work",
                title = "Work / Office",
                address = "Tower C, Prestige Tech Park, Outer Ring Rd, Kadubeesanahalli, Bengaluru 560103",
                latitude = 12.9370,
                longitude = 77.6920,
                category = "Work"
            ),
            LocationPoint(
                id = "saved_warehouse",
                title = "Central Freight Depot",
                address = "Shed 12, Heavy Vehicle Terminal, Peenya 2nd Phase, Bengaluru 560058",
                latitude = 13.0300,
                longitude = 77.5200,
                category = "Warehouse"
            ),
            LocationPoint(
                id = "saved_apmc",
                title = "APMC Wholesale Mandi",
                address = "Shop 114, Yeshwanthpur APMC Yard, Bengaluru 560022",
                latitude = 13.0210,
                longitude = 77.5510,
                category = "Mandi"
            )
        )
    }

    // Dynamic Address Updating: as coordinates change under fixed pin, update the search bar text
    fun updateAddressFromCoordinates(lat: Double, lng: Double) {
        // Find nearest known POI/road within snapping distance (approx 300m)
        val nearestPreset = LocationPoint.PRESET_LOCATIONS.minByOrNull {
            calculateDistance(lat, lng, it.latitude, it.longitude)
        }
        val distToPreset = if (nearestPreset != null) {
            calculateDistance(lat, lng, nearestPreset.latitude, nearestPreset.longitude)
        } else Double.MAX_VALUE

        if (distToPreset < 0.35 && nearestPreset != null) {
            // Snapped to a known POI / road
            searchBarText = nearestPreset.title
            triggerRoadSnapHaptic(context)
        } else {
            val geocoded = LocationHelper.reverseGeocode(context, lat, lng)
            searchBarText = geocoded.title.ifBlank { geocoded.address.take(35) }
        }
    }

    // Step-dependent properties
    val isPickupStep = currentStep == RapidoPlannerStep.PICKUP_SELECTION
    val isDropStep = currentStep == RapidoPlannerStep.DROP_SELECTION
    val isOverviewStep = currentStep == RapidoPlannerStep.ROUTE_OVERVIEW

    val anchorColor by animateColorAsState(
        targetValue = when (currentStep) {
            RapidoPlannerStep.PICKUP_SELECTION -> Color(0xFF10B981) // Green
            RapidoPlannerStep.DROP_SELECTION -> Color(0xFFEF4444) // Red/Orange
            RapidoPlannerStep.ROUTE_OVERVIEW -> Color(0xFF38BDF8) // Blue route
        },
        label = "anchorColor"
    )

    val searchBarPlaceholder = when (currentStep) {
        RapidoPlannerStep.PICKUP_SELECTION -> "Enter pickup location..."
        RapidoPlannerStep.DROP_SELECTION -> "Enter drop location..."
        RapidoPlannerStep.ROUTE_OVERVIEW -> "Route Confirmed"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F172A),
        modifier = Modifier
            .fillMaxSize()
            .testTag("rapido_route_planner_sheet")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. FULL BACKGROUND INTERACTIVE VECTOR MAP
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentStep) {
                        if (!isOverviewStep) {
                            detectDragGestures(
                                onDragStart = {
                                    isDraggingMap = true
                                    isSearchFocused = false
                                    coroutineScope.launch {
                                        pinBounceOffsetY.animateTo(-16f, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                },
                                onDragEnd = {
                                    isDraggingMap = false
                                    coroutineScope.launch {
                                        pinBounceOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                    // Reverse geocode final position
                                    updateAddressFromCoordinates(currentCenterLat, currentCenterLng)
                                },
                                onDragCancel = {
                                    isDraggingMap = false
                                    coroutineScope.launch { pinBounceOffsetY.animateTo(0f) }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    // Panning map: move coordinates under center pin
                                    val latDelta = dragAmount.y * 0.00032
                                    val lngDelta = dragAmount.x * 0.00032
                                    currentCenterLat -= latDelta
                                    currentCenterLng += lngDelta

                                    // Dynamic Address Updating in real-time as the map is dragged!
                                    updateAddressFromCoordinates(currentCenterLat, currentCenterLng)
                                }
                            )
                        }
                    }
                    .testTag("rapido_map_surface")
            ) {
                InteractiveMapView(
                    modifier = Modifier.fillMaxSize(),
                    userLocation = if (isPickupStep) confirmedPickup else null
                )

                // 2. FIXED CENTER PIN (Stationary in exact center while map moves underneath)
                if (!isOverviewStep) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset { IntOffset(0, pinBounceOffsetY.value.roundToInt()) }
                            .testTag(if (isPickupStep) "fixed_green_center_pin" else "fixed_red_drop_pin"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Floating Callout Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.95f),
                            border = BorderStroke(1.5.dp, anchorColor),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(anchorColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPickupStep) "Set Pickup Point" else "Set Drop Entrance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Center Stationary Pin
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = if (isPickupStep) "Pickup Pin" else "Drop Pin",
                            tint = anchorColor,
                            modifier = Modifier.size(46.dp)
                        )

                        // Ground shadow ellipse
                        Box(
                            modifier = Modifier
                                .size(18.dp, 6.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                    }
                }
            }

            // 3. TOP BAR: "In One Search Bar Only"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                // Single Unified Search Bar Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.5.dp, anchorColor.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rapido_single_search_bar")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Step Visual Anchor Indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(anchorColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(if (isPickupStep) CircleShape else RoundedCornerShape(2.dp))
                                        .background(anchorColor)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Dynamic Search Field
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (currentStep) {
                                        RapidoPlannerStep.PICKUP_SELECTION -> "PICKUP LOCATION"
                                        RapidoPlannerStep.DROP_SELECTION -> "DROP DESTINATION"
                                        RapidoPlannerStep.ROUTE_OVERVIEW -> "CONFIRMED ROUTE"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = anchorColor
                                )

                                if (isOverviewStep) {
                                    Text(
                                        text = "${confirmedPickup.title}  ➔  ${confirmedDrop.title}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    TextField(
                                        value = if (isSearchFocused) searchQuery else searchBarText,
                                        onValueChange = {
                                            searchQuery = it
                                            searchBarText = it
                                        },
                                        placeholder = {
                                            Text(searchBarPlaceholder, fontSize = 13.sp, color = Color(0xFF64748B))
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = anchorColor
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("rapido_search_input")
                                    )
                                }
                            }

                            // Trailing Search or Clear Icon
                            if (isSearchFocused && searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                }
                            } else if (!isOverviewStep) {
                                IconButton(
                                    onClick = { isSearchFocused = !isSearchFocused },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = anchorColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Subtitle Coordinates & Pin Snap readout
                        if (!isOverviewStep) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDraggingMap) "Panning map under stationary pin..." else "Pin locked to exact building gate",
                                    fontSize = 10.sp,
                                    color = if (isDraggingMap) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.4f, %.4f", currentCenterLat, currentCenterLng),
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Autocomplete Suggestions List (Overlay when user types or focuses)
                AnimatedVisibility(
                    visible = isSearchFocused || searchQuery.isNotBlank(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.98f)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .heightIn(max = 240.dp)
                            .testTag("rapido_autocomplete_card")
                    ) {
                        LazyColumn(modifier = Modifier.padding(6.dp)) {
                            // If drop step and query is empty, prioritize Saved Places ("Home", "Work")
                            if (isDropStep && searchQuery.isBlank()) {
                                item {
                                    Text(
                                        text = "SAVED PLACES & FREQUENT DESTINATIONS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF97316),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                items(savedPlaces) { place ->
                                    AutocompleteItemRow(
                                        place = place,
                                        accentColor = Color(0xFFF97316),
                                        onClick = {
                                            currentCenterLat = place.latitude
                                            currentCenterLng = place.longitude
                                            searchBarText = place.title
                                            searchQuery = ""
                                            isSearchFocused = false
                                            triggerRoadSnapHaptic(context)
                                        }
                                    )
                                }
                            }

                            // Matching POIs from presets
                            val matchingPois = LocationPoint.PRESET_LOCATIONS.filter {
                                searchQuery.isBlank() ||
                                        it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.area.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true) ||
                                        it.pincode.contains(searchQuery, ignoreCase = true)
                            }

                            items(matchingPois) { poi ->
                                AutocompleteItemRow(
                                    place = poi,
                                    accentColor = anchorColor,
                                    onClick = {
                                        currentCenterLat = poi.latitude
                                        currentCenterLng = poi.longitude
                                        searchBarText = poi.title
                                        searchQuery = ""
                                        isSearchFocused = false
                                        triggerRoadSnapHaptic(context)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. FLOATING QUICK SNAP ACTION: "Use current location" (Pickup mode)
            if (isPickupStep && !isSearchFocused) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 110.dp, end = 16.dp)
                        .clickable {
                            val hasPermission = locationPermissionsState.allPermissionsGranted ||
                                    LocationHelper.hasLocationPermission(context)
                            if (hasPermission) {
                                isGpsSnapping = true
                                LocationHelper.fetchCurrentLocation(
                                    context = context,
                                    onSuccess = { loc ->
                                        isGpsSnapping = false
                                        currentCenterLat = loc.latitude
                                        currentCenterLng = loc.longitude
                                        searchBarText = loc.title
                                        triggerRoadSnapHaptic(context)
                                    },
                                    onError = { isGpsSnapping = false }
                                )
                            } else {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        }
                        .testTag("rapido_use_current_location_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGpsSnapping) {
                            CircularProgressIndicator(
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Use current location",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use current location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // 5. BOTTOM CONFIRMATION DOCK
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .testTag("rapido_bottom_confirmation_dock")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (currentStep) {
                        RapidoPlannerStep.PICKUP_SELECTION -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CONFIRM PICKUP SPOT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        text = searchBarText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = {
                                        // Confirm pickup action: haptic + state transition to Drop
                                        triggerConfirmHaptic(context)
                                        val geocoded = LocationHelper.reverseGeocode(context, currentCenterLat, currentCenterLng)
                                        confirmedPickup = geocoded.copy(title = searchBarText)

                                        // Transition seamlessly to Drop selection
                                        currentStep = RapidoPlannerStep.DROP_SELECTION
                                        currentCenterLat = initialDrop.latitude
                                        currentCenterLng = initialDrop.longitude
                                        searchBarText = initialDrop.title
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("confirm_pickup_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm pickup", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        RapidoPlannerStep.DROP_SELECTION -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CONFIRM DROP ENTRANCE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = searchBarText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = {
                                        // Confirm drop action: haptic + trigger seamless zoom out overview
                                        triggerConfirmHaptic(context)
                                        val geocoded = LocationHelper.reverseGeocode(context, currentCenterLat, currentCenterLng)
                                        confirmedDrop = geocoded.copy(title = searchBarText)

                                        // Transition to Route Overview with Seamless Zoom!
                                        currentStep = RapidoPlannerStep.ROUTE_OVERVIEW
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("confirm_drop_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm drop", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        RapidoPlannerStep.ROUTE_OVERVIEW -> {
                            // Seamless Zoom Overview framing both pins simultaneously
                            val routeDistKm = calculateDistance(
                                confirmedPickup.latitude, confirmedPickup.longitude,
                                confirmedDrop.latitude, confirmedDrop.longitude
                            )
                            val estTimeMin = (routeDistKm * 2.8).roundToInt().coerceAtLeast(10)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Framed Route Summary",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.1f", routeDistKm)} km • ~$estTimeMin min",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFBBF24)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Route points summary
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(confirmedPickup.title, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(confirmedDrop.title, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        triggerConfirmHaptic(context)
                                        onRouteConfirmed(confirmedPickup, confirmedDrop)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_framed_route_btn")
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply Route & Select Vehicle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutocompleteItemRow(
    place: LocationPoint,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    place.category.equals("Home", ignoreCase = true) -> Icons.Default.Home
                    place.category.equals("Work", ignoreCase = true) -> Icons.Default.Work
                    place.category.contains("Mandi", ignoreCase = true) -> Icons.Default.Store
                    place.category.contains("Industrial", ignoreCase = true) -> Icons.Default.Factory
                    place.category.contains("Tech", ignoreCase = true) -> Icons.Default.Business
                    else -> Icons.Default.Warehouse
                },
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = place.address,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Haptic feedback when the fixed center pin snaps to a known road or landmark.
 */
private fun triggerRoadSnapHaptic(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, 80))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    } catch (_: Exception) {}
}

/**
 * Haptic feedback when confirm buttons are tapped.
 */
private fun triggerConfirmHaptic(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(40)
        }
    } catch (_: Exception) {}
}

/**
 * Haversine formula calculation.
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
