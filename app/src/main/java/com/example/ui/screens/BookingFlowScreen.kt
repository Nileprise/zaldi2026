package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VehicleCatalog
import com.example.data.model.VehicleTier
import com.example.ui.components.GoogleMapsView
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberPrimaryDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.util.DirectionsResult
import com.example.util.DistanceCalculator
import com.example.util.GoogleMapsRoutingService
import com.example.util.PlaceModel
import com.example.util.RouteDistanceInfo
import com.example.util.RoutingProfile

@Composable
fun BookingFlowScreen(
    pickupAddress: String,
    dropoffAddress: String,
    selectedVehicleId: String,
    selectedGoodsType: String,
    isHelperRequired: Boolean,
    selectedPaymentMethod: String,
    routeDistanceInfo: RouteDistanceInfo? = null,
    pickupPlace: PlaceModel? = null,
    dropoffPlace: PlaceModel? = null,
    directionsResult: DirectionsResult? = null,
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
    onSelectPickupPlace: ((PlaceModel) -> Unit)? = null,
    onSelectDropoffPlace: ((PlaceModel) -> Unit)? = null,
    onSetCustomDistance: (Double) -> Unit = {},
    onResetDistance: () -> Unit = {},
    onVehicleSelect: (String) -> Unit,
    onGoodsSelect: (String) -> Unit,
    onToggleHelper: () -> Unit,
    onPaymentSelect: (String) -> Unit,
    calculateFare: (String) -> Double,
    onConfirmBooking: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectivePickupPlace = pickupPlace ?: remember(pickupAddress) {
        GoogleMapsRoutingService.geocode(pickupAddress)
    }
    val effectiveDropoffPlace = dropoffPlace ?: remember(dropoffAddress) {
        GoogleMapsRoutingService.geocode(dropoffAddress)
    }

    val selectedTier = VehicleCatalog.tiers.find { it.id == selectedVehicleId } ?: VehicleCatalog.tiers[2]

    val effectiveDirections = directionsResult ?: remember(effectivePickupPlace, effectiveDropoffPlace, selectedTier) {
        GoogleMapsRoutingService.calculateDirections(
            pickup = effectivePickupPlace,
            dropoff = effectiveDropoffPlace,
            profile = selectedTier.routingProfile
        )
    }

    val effectiveRouteInfo = routeDistanceInfo ?: remember(effectiveDirections) {
        RouteDistanceInfo(
            distanceKm = effectiveDirections.actualRoadKm,
            estimatedDurationMinutes = effectiveDirections.etaMinutes,
            viaRoad = effectiveDirections.viaRoad,
            routeSummary = effectiveDirections.routeSummary
        )
    }

    val totalFare = calculateFare(selectedVehicleId)

    // UI state
    var showPickupAutocomplete by remember { mutableStateOf(false) }
    var showDropoffAutocomplete by remember { mutableStateOf(false) }
    var showExactDistanceDialog by remember { mutableStateOf(false) }
    var customDistanceInputText by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Validation state
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isPickupInvalid by remember { mutableStateOf(false) }
    var isDropoffInvalid by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun validateInputs(): Boolean {
        val p = pickupAddress.trim()
        val d = dropoffAddress.trim()

        if (p.isEmpty() || p.length < 3) {
            validationErrorMessage = "Please select or type a valid pickup address."
            isPickupInvalid = true
            isDropoffInvalid = false
            return false
        }

        if (d.isEmpty() || d.length < 3) {
            validationErrorMessage = "Please select or type a valid drop-off destination."
            isPickupInvalid = false
            isDropoffInvalid = true
            return false
        }

        if (p.equals(d, ignoreCase = true) || effectivePickupPlace.placeId == effectiveDropoffPlace.placeId) {
            validationErrorMessage = "Pickup and Drop-off locations cannot be identical. Please choose distinct points."
            isPickupInvalid = true
            isDropoffInvalid = true
            return false
        }

        validationErrorMessage = null
        isPickupInvalid = false
        isDropoffInvalid = false
        return true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Confirm Freight Booking",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Directions & Distance Matrix • Actual Road KM",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${effectiveDirections.actualRoadKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberPrimaryDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Validation Error Banner (if any)
        AnimatedVisibility(
            visible = validationErrorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            validationErrorMessage?.let { errMsg ->
                Surface(
                    color = Color(0xFFFEE2E2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errMsg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Interactive Route Map Preview with Map Markers & Route Line
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMapsView(
                        driverLocation = null,
                        modifier = Modifier.fillMaxSize(),
                        pickupLatLng = effectivePickupPlace.latLng,
                        dropoffLatLng = effectiveDropoffPlace.latLng,
                        pickupTitle = effectivePickupPlace.name,
                        dropoffTitle = effectiveDropoffPlace.name,
                        pickupPlaceId = effectivePickupPlace.placeId,
                        dropoffPlaceId = effectiveDropoffPlace.placeId,
                        customWaypoints = effectiveDirections.waypoints,
                        vehicleType = selectedTier.id,
                        isInteractive = true
                    )

                    // Overlay pill showing routing mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (selectedTier.routingProfile) {
                                    RoutingProfile.TWO_WHEELER -> Icons.Default.TwoWheeler
                                    RoutingProfile.THREE_WHEELER -> Icons.Default.ElectricRickshaw
                                    RoutingProfile.TATA_ACE_COMMERCIAL -> Icons.Default.LocalShipping
                                },
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedTier.routingProfile.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pickup & Dropoff Address Selection Card with Google Maps Autocomplete
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route Waypoints & Place IDs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        IconButton(
                            onClick = {
                                val tempP = pickupAddress
                                val tempPlace = effectivePickupPlace
                                onPickupChange(dropoffAddress)
                                onDropoffChange(tempP)
                                onSelectPickupPlace?.invoke(effectiveDropoffPlace)
                                onSelectDropoffPlace?.invoke(tempPlace)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Locations",
                                tint = AmberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pickup Field
                    OutlinedTextField(
                        value = pickupAddress,
                        onValueChange = {
                            onPickupChange(it)
                            showPickupAutocomplete = it.isNotBlank()
                            validationErrorMessage = null
                            isPickupInvalid = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pickup_address_input"),
                        label = { Text("Pickup Location (Sender)") },
                        placeholder = { Text("Search location or Place ID...") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen)
                        },
                        trailingIcon = {
                            if (pickupAddress.isNotEmpty()) {
                                IconButton(onClick = { onPickupChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        isError = isPickupInvalid,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            errorBorderColor = Color(0xFFDC2626)
                        )
                    )

                    // Pickup Place ID badge
                    Text(
                        text = "Place ID: ${effectivePickupPlace.placeId} • ${effectivePickupPlace.latitude.toString().take(7)}, ${effectivePickupPlace.longitude.toString().take(7)}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )

                    // Autocomplete Suggestions for Pickup
                    if (showPickupAutocomplete && pickupAddress.length >= 2) {
                        val matches = GoogleMapsRoutingService.searchPlaces(pickupAddress)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        ) {
                            matches.forEach { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (onSelectPickupPlace != null) {
                                                onSelectPickupPlace(place)
                                            } else {
                                                onPickupChange(place.name)
                                            }
                                            showPickupAutocomplete = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = place.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text(text = "${place.category} • Place ID: ${place.placeId.take(18)}...", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dropoff Field
                    OutlinedTextField(
                        value = dropoffAddress,
                        onValueChange = {
                            onDropoffChange(it)
                            showDropoffAutocomplete = it.isNotBlank()
                            validationErrorMessage = null
                            isDropoffInvalid = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dropoff_address_input"),
                        label = { Text("Drop-off Destination (Consignee)") },
                        placeholder = { Text("Search destination or Place ID...") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444))
                        },
                        trailingIcon = {
                            if (dropoffAddress.isNotEmpty()) {
                                IconButton(onClick = { onDropoffChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        isError = isDropoffInvalid,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            errorBorderColor = Color(0xFFDC2626)
                        )
                    )

                    // Dropoff Place ID badge
                    Text(
                        text = "Place ID: ${effectiveDropoffPlace.placeId} • ${effectiveDropoffPlace.latitude.toString().take(7)}, ${effectiveDropoffPlace.longitude.toString().take(7)}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )

                    // Autocomplete Suggestions for Dropoff
                    if (showDropoffAutocomplete && dropoffAddress.length >= 2) {
                        val matches = GoogleMapsRoutingService.searchPlaces(dropoffAddress)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        ) {
                            matches.forEach { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (onSelectDropoffPlace != null) {
                                                onSelectDropoffPlace(place)
                                            } else {
                                                onDropoffChange(place.name)
                                            }
                                            showDropoffAutocomplete = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = place.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text(text = "${place.category} • Place ID: ${place.placeId.take(18)}...", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick-Pick Freight Hubs
                    Text(
                        text = "⚡ Popular Freight Hubs:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GoogleMapsRoutingService.placeCatalog.take(6)) { hub ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceTertiary,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.clickable {
                                    if (onSelectDropoffPlace != null) {
                                        onSelectDropoffPlace(hub)
                                    } else {
                                        onDropoffChange(hub.name)
                                    }
                                    validationErrorMessage = null
                                    isDropoffInvalid = false
                                }
                            ) {
                                Text(
                                    text = hub.name.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Tier Selector Grid (2-Wheeler, 3-Wheeler, Tata Ace, Trucks)
            Text(
                text = "Commercial Vehicle Selection & Dynamic Pricing",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Vehicle-specific routing profiles & calibrated per-KM rates",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(VehicleCatalog.tiers) { tier ->
                    val isSelected = tier.id == selectedVehicleId
                    val tierFare = calculateFare(tier.id)
                    VehicleSelectionCard(
                        tier = tier,
                        isSelected = isSelected,
                        estimatedFare = tierFare,
                        distanceKm = effectiveDirections.actualRoadKm,
                        onClick = { onVehicleSelect(tier.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Distance Matrix & Actual Road KM Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distance Matrix & Routing Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberContainer
                        ) {
                            Text(
                                text = "ETA: ~${effectiveDirections.etaMinutes} mins",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberPrimaryDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Actual Road Distance", fontSize = 11.sp, color = TextMuted)
                            Text(text = "${effectiveDirections.actualRoadKm} km", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                        Column {
                            Text(text = "Routing Corridor", fontSize = 11.sp, color = TextMuted)
                            Text(text = effectiveDirections.viaRoad, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Base Vehicle", fontSize = 11.sp, color = TextMuted)
                            Text(text = selectedTier.name.substringBefore(" "), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Manual KM adjuster chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Adjust KM:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceTertiary,
                            modifier = Modifier.clickable {
                                onSetCustomDistance(maxOf(1.0, effectiveDirections.actualRoadKm - 1.0))
                            }
                        ) {
                            Text(text = "-1 km", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceTertiary,
                            modifier = Modifier.clickable {
                                onSetCustomDistance(effectiveDirections.actualRoadKm + 1.0)
                            }
                        ) {
                            Text(text = "+1 km", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceTertiary,
                            modifier = Modifier.clickable { onResetDistance() }
                        ) {
                            Text(text = "Auto GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cargo & Helper Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Cargo Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(VehicleCatalog.goodsCategories) { cat ->
                            val isSel = cat == selectedGoodsType
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) AmberPrimary else SurfaceTertiary,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AmberPrimary else BorderLight),
                                modifier = Modifier.clickable { onGoodsSelect(cat) }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleHelper() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Dedicated Driver Loading Helper", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text(text = "Driver assists with ground-floor loading & unloading (+₹80)", fontSize = 11.sp, color = TextMuted)
                        }
                        Checkbox(
                            checked = isHelperRequired,
                            onCheckedChange = { onToggleHelper() },
                            colors = CheckboxDefaults.colors(checkedColor = AmberPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Sticky Bottom CTA Bar with Transparent Fare & Confirmation Trigger
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SurfaceCard,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "TOTAL ESTIMATED FARE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "₹${totalFare.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberPrimary
                    )
                    Text(
                        text = "${effectiveDirections.actualRoadKm} km • ~${effectiveDirections.etaMinutes}m ETA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (validateInputs()) {
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("confirm_booking_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text(
                        text = "Confirm Booking",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Comprehensive Booking Confirmation Modal Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirm Freight Dispatch",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Please verify your route and consignee details:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Pickup info
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Pickup Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = effectivePickupPlace.name, fontSize = 12.sp, color = TextDark)
                            Text(text = "Place ID: ${effectivePickupPlace.placeId}", fontSize = 9.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropoff info
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Drop-off Destination", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = effectiveDropoffPlace.name, fontSize = 12.sp, color = TextDark)
                            Text(text = "Place ID: ${effectiveDropoffPlace.placeId}", fontSize = 9.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderLight)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Vehicle Tier:", fontSize = 12.sp, color = TextMuted)
                        Text(text = selectedTier.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Actual Road Distance:", fontSize = 12.sp, color = TextMuted)
                        Text(text = "${effectiveDirections.actualRoadKm} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Estimated Arrival (ETA):", fontSize = 12.sp, color = TextMuted)
                        Text(text = "~${effectiveDirections.etaMinutes} mins", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Fare (COD / UPI):", fontSize = 12.sp, color = TextMuted)
                        Text(text = "₹${totalFare.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AmberPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        onConfirmBooking()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Confirm & Dispatch Driver", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Edit Details")
                }
            }
        )
    }
}

@Composable
private fun VehicleSelectionCard(
    tier: VehicleTier,
    isSelected: Boolean,
    estimatedFare: Double,
    distanceKm: Double,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AmberPrimary else BorderLight,
        label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) AmberContainer else SurfaceCard,
        label = "container"
    )

    val icon: ImageVector = when (tier.routingProfile) {
        RoutingProfile.TWO_WHEELER -> Icons.Default.TwoWheeler
        RoutingProfile.THREE_WHEELER -> Icons.Default.ElectricRickshaw
        RoutingProfile.TATA_ACE_COMMERCIAL -> Icons.Default.LocalShipping
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("vehicle_tier_${tier.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(width = 1.5.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSelected) AmberPrimary else SurfaceTertiary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tier.name,
                        tint = if (isSelected) Color.White else AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(AmberPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tier.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )
            Text(
                text = tier.capacity,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${estimatedFare.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) AmberPrimary else TextDark
                )
                Text(
                    text = "${tier.etaMinutes}m ETA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }
}
