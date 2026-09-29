package com.example.ui.components

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationPoint
import com.example.utils.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class PlaceDialogMode {
    SEARCH_LIST,
    MAP_PICKER,
    ADDRESS_DETAILS_FORM
}

/**
 * Reusable location indicator element.
 * Green location indicator for pickup, Red/Orange destination indicator for drop.
 */
@Composable
fun LocationIndicatorDot(
    isPickup: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    val indicatorColor = if (isPickup) Color(0xFF10B981) else Color(0xFFEF4444)
    val ringColor = if (isPickup) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFEF4444).copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .size((size + 8).dp)
            .clip(CircleShape)
            .background(ringColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(if (isPickup) CircleShape else RoundedCornerShape(3.dp))
                .background(indicatorColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PlaceSelectionDialog(
    title: String? = null,
    isPickup: Boolean = true,
    initialLocation: LocationPoint,
    onDismiss: () -> Unit,
    onLocationConfirmed: (LocationPoint) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(PlaceDialogMode.SEARCH_LIST) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("All") }
    var isGpsLoading by remember { mutableStateOf(false) }
    var gpsError by remember { mutableStateOf<String?>(null) }

    // Active location being edited or selected
    var activeLocation by remember { mutableStateOf(initialLocation) }

    // Form fields for Indian structured address
    var flatPlotNo by remember { mutableStateOf(initialLocation.flatPlotNo) }
    var buildingName by remember { mutableStateOf(initialLocation.buildingName) }
    var street by remember { mutableStateOf(initialLocation.street) }
    var landmark by remember { mutableStateOf(initialLocation.landmark) }
    var area by remember { mutableStateOf(initialLocation.area) }
    var city by remember { mutableStateOf(initialLocation.city) }
    var pincode by remember { mutableStateOf(initialLocation.pincode) }
    var contactName by remember { mutableStateOf(initialLocation.contactPerson) }
    var contactPhone by remember { mutableStateOf(initialLocation.contactPhone) }

    // Accompanist Runtime Permission Handler for Location
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Thematic color branding: Green for pickup, Red/Orange for drop
    val themeColor = if (isPickup) Color(0xFF10B981) else Color(0xFFEF4444)
    val themeBgLight = if (isPickup) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
    val headerTitle = title ?: if (isPickup) "Enter Pickup Location" else "Enter Drop Location"
    val roleBadgeText = if (isPickup) "PICKUP LOCATION" else "DROP DESTINATION"
    val searchPlaceholder = if (isPickup) "Enter pickup location..." else "Enter drop location..."
    val confirmBtnText = if (isPickup) "Confirm Pickup" else "Confirm Drop"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1E293B),
        modifier = Modifier.testTag(if (isPickup) "pickup_selection_sheet" else "drop_selection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row with Indicator & Close Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LocationIndicatorDot(isPickup = isPickup, size = 14)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = roleBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor
                            )
                        }
                        Text(
                            text = headerTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_place_dialog_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (mode) {
                PlaceDialogMode.SEARCH_LIST -> {
                    // Search Bar with Location Indicator
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(searchPlaceholder, fontSize = 13.sp, color = Color(0xFF64748B)) },
                        leadingIcon = {
                            LocationIndicatorDot(isPickup = isPickup, size = 10)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = themeColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(if (isPickup) "enter_pickup_location_input" else "enter_drop_location_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Actions Row: Use Current Location (Pickup) & Pick on Map (Fixed Pin)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Use Current Location Button
                        if (isPickup) {
                            Card(
                                onClick = {
                                    val hasPermission = locationPermissionsState.allPermissionsGranted ||
                                            LocationHelper.hasLocationPermission(context)
                                    if (hasPermission) {
                                        isGpsLoading = true
                                        gpsError = null
                                        LocationHelper.fetchCurrentLocation(
                                            context = context,
                                            onSuccess = { loc ->
                                                isGpsLoading = false
                                                activeLocation = loc
                                                flatPlotNo = loc.flatPlotNo
                                                buildingName = loc.buildingName
                                                street = loc.street
                                                landmark = loc.landmark
                                                area = loc.area
                                                city = loc.city
                                                pincode = loc.pincode
                                                mode = PlaceDialogMode.ADDRESS_DETAILS_FORM
                                            },
                                            onError = { err ->
                                                isGpsLoading = false
                                                gpsError = err
                                            }
                                        )
                                    } else {
                                        locationPermissionsState.launchMultiplePermissionRequest()
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("use_current_location_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGpsLoading) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MyLocation,
                                                contentDescription = "Use Current Location",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Use Current Location",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF34D399)
                                        )
                                        Text(
                                            text = "GPS auto-detect",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        // Pick on Map with Fixed Pin Button
                        Card(
                            onClick = { mode = PlaceDialogMode.MAP_PICKER },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(if (isPickup) "pick_pickup_on_map_btn" else "pick_drop_on_map_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(themeBgLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "Pick on Map",
                                        tint = themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Fixed Map Pin",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Position on map",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    if (gpsError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gpsError ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // City Filter Chips
                    val cities = listOf("All", "Bengaluru", "Mumbai", "Delhi NCR")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(cities) { c ->
                            val isSelected = selectedCity == c
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) themeColor else Color(0xFF0F172A))
                                    .clickable { selectedCity = c }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = c,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Autocomplete Suggestions Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Autocomplete Suggestions" else "Matching Locations",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (isPickup) "Pickup Hubs" else "Drop Points",
                            fontSize = 11.sp,
                            color = themeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Autocomplete Suggestions List
                    val filteredList = LocationPoint.PRESET_LOCATIONS.filter { loc ->
                        val matchCity = selectedCity == "All" || loc.city.equals(selectedCity, ignoreCase = true)
                        val matchQuery = searchQuery.isBlank() ||
                                loc.title.contains(searchQuery, ignoreCase = true) ||
                                loc.area.contains(searchQuery, ignoreCase = true) ||
                                loc.landmark.contains(searchQuery, ignoreCase = true) ||
                                loc.pincode.contains(searchQuery, ignoreCase = true) ||
                                loc.address.contains(searchQuery, ignoreCase = true) ||
                                loc.category.contains(searchQuery, ignoreCase = true)
                        matchCity && matchQuery
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .testTag("autocomplete_suggestions_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList) { loc ->
                            val distKm = calculateApproxDistanceKm(
                                activeLocation.latitude, activeLocation.longitude,
                                loc.latitude, loc.longitude
                            )

                            Card(
                                onClick = {
                                    activeLocation = loc
                                    flatPlotNo = loc.flatPlotNo
                                    buildingName = loc.buildingName
                                    street = loc.street
                                    landmark = loc.landmark
                                    area = loc.area
                                    city = loc.city
                                    pincode = loc.pincode
                                    contactName = loc.contactPerson
                                    contactPhone = loc.contactPhone
                                    mode = PlaceDialogMode.ADDRESS_DETAILS_FORM
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("autocomplete_item_${loc.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category / Location Icon
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(themeBgLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                loc.category.contains("Mandi") -> Icons.Default.Store
                                                loc.category.contains("Industrial") -> Icons.Default.Factory
                                                loc.category.contains("Tech") -> Icons.Default.Business
                                                loc.category.contains("Residential") -> Icons.Default.Home
                                                else -> Icons.Default.Warehouse
                                            },
                                            contentDescription = loc.category,
                                            tint = themeColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = loc.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${String.format(java.util.Locale.US, "%.1f", distKm)} km",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeColor
                                            )
                                        }

                                        Text(
                                            text = loc.address,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF334155))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = loc.category,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                            }
                                            if (loc.landmark.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Near: ${loc.landmark}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF38BDF8),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Quick Confirm Action
                                    IconButton(
                                        onClick = {
                                            onLocationConfirmed(loc)
                                        },
                                        modifier = Modifier.size(32.dp).testTag("quick_select_${loc.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Select",
                                            tint = themeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PlaceDialogMode.MAP_PICKER -> {
                    // Map Picker Mode: interactive vector canvas with Fixed Center Pin
                    var mapLat by remember { mutableDoubleStateOf(activeLocation.latitude) }
                    var mapLng by remember { mutableDoubleStateOf(activeLocation.longitude) }

                    Text(
                        text = if (isPickup) "Fixed Center Pin (Pickup)" else "Fixed Map Pin (Drop)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text(
                        text = "Pan map to position pin directly at your freight gate or doorstep",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Adjust coordinates slightly on drag to simulate precise map panning
                                    mapLat -= (dragAmount.y * 0.0003)
                                    mapLng += (dragAmount.x * 0.0003)
                                }
                            }
                    ) {
                        InteractiveMapView(
                            modifier = Modifier.fillMaxSize()
                        )

                        // FIXED CENTER PIN with Callout Pill
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag(if (isPickup) "fixed_center_pin_pickup" else "fixed_map_pin_drop"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, themeColor),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LocationIndicatorDot(isPickup = isPickup, size = 8)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPickup) "Set Pickup Point" else "Set Drop Location",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Pin Pointer
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = if (isPickup) "Fixed Center Pickup Pin" else "Fixed Drop Pin",
                                tint = themeColor,
                                modifier = Modifier.size(42.dp)
                            )

                            // Pin ground shadow ellipse
                            Box(
                                modifier = Modifier
                                    .size(16.dp, 5.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coordinate / Geocode Preview Box
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Center Coordinates",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.5f° N, %.5f° E", mapLat, mapLng),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(themeBgLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isPickup) "Pickup Pin Locked" else "Drop Pin Locked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mode = PlaceDialogMode.SEARCH_LIST },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back to Search", color = Color(0xFF94A3B8))
                        }

                        Button(
                            onClick = {
                                val geocoded = LocationHelper.reverseGeocode(context, mapLat, mapLng)
                                activeLocation = geocoded
                                flatPlotNo = geocoded.flatPlotNo
                                buildingName = geocoded.buildingName
                                street = geocoded.street
                                landmark = geocoded.landmark
                                area = geocoded.area
                                city = geocoded.city
                                pincode = geocoded.pincode
                                onLocationConfirmed(geocoded)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = if (isPickup) Color(0xFF0F172A) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag(if (isPickup) "confirm_pickup_btn" else "confirm_drop_btn")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(confirmBtnText, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                PlaceDialogMode.ADDRESS_DETAILS_FORM -> {
                    // Full Structured Indian Address Input Form with Confirm Pickup / Drop
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LocationIndicatorDot(isPickup = isPickup, size = 10)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Address & Contact Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Flat / Plot & Building
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = flatPlotNo,
                                onValueChange = { flatPlotNo = it },
                                label = { Text("Flat / Plot / Shed No.") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(1f).testTag("flat_plot_input")
                            )
                            OutlinedTextField(
                                value = buildingName,
                                onValueChange = { buildingName = it },
                                label = { Text("Building / Complex") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(1f).testTag("building_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Street & Landmark
                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text("Street / Road / Cross") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = themeColor
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("street_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("Landmark (e.g. Near Metro Pillar, Mandi Gate)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = themeColor
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("landmark_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Area & Pincode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = area,
                                onValueChange = { area = it },
                                label = { Text("Area / Locality") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(1.2f).testTag("area_input")
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(0.8f).testTag("pincode_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Site Contact Person
                        Text(
                            text = if (isPickup) "Pickup Contact Person (Dispatcher)" else "Receiver Contact Person",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = { contactName = it },
                                label = { Text("Contact Name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(1f).testTag("contact_name_input")
                            )
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("Phone Number") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = themeColor
                                ),
                                modifier = Modifier.weight(1f).testTag("contact_phone_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save & Confirm Pickup / Confirm Drop Button
                        Button(
                            onClick = {
                                val resolvedTitle = if (buildingName.isNotBlank()) buildingName else if (area.isNotBlank()) area else activeLocation.title
                                val updatedLocation = activeLocation.copy(
                                    title = resolvedTitle,
                                    flatPlotNo = flatPlotNo,
                                    buildingName = buildingName,
                                    street = street,
                                    landmark = landmark,
                                    area = area,
                                    city = city,
                                    pincode = pincode,
                                    contactPerson = contactName,
                                    contactPhone = contactPhone
                                )
                                onLocationConfirmed(
                                    updatedLocation.copy(
                                        address = updatedLocation.fullFormattedAddress()
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColor,
                                contentColor = if (isPickup) Color(0xFF0F172A) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag(if (isPickup) "confirm_pickup_btn" else "confirm_drop_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(confirmBtnText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Approximate haversine distance helper for autocomplete ranking.
 */
private fun calculateApproxDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
