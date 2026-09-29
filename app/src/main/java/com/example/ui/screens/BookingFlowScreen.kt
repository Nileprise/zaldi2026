package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.model.VehicleCatalog
import com.example.data.model.VehicleTier
import com.example.ui.components.GoogleMapsView
import com.example.util.DirectionsResult
import com.example.util.GoogleMapsRoutingService
import com.example.util.PlaceModel
import com.example.util.RouteDistanceInfo
import com.example.util.RoutingProfile

@OptIn(ExperimentalMaterial3Api::class)
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
    popularHubs: List<PlaceModel> = emptyList(), // Production-ready: Passed from ViewModel
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
    onSelectPickupPlace: ((PlaceModel) -> Unit)? = null,
    onSelectDropoffPlace: ((PlaceModel) -> Unit)? = null,
    onVehicleSelect: (String) -> Unit,
    onGoodsSelect: (String) -> Unit,
    onToggleHelper: () -> Unit,
    onPaymentSelect: (String) -> Unit,
    calculateFare: (String) -> Double,
    onConfirmBooking: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Resolution of effective places and routing (In production, this should ideally be hoisted to the ViewModel)
    val effectivePickupPlace = pickupPlace ?: remember(pickupAddress) {
        GoogleMapsRoutingService.geocode(pickupAddress)
    }
    val effectiveDropoffPlace = dropoffPlace ?: remember(dropoffAddress) {
        GoogleMapsRoutingService.geocode(dropoffAddress)
    }

    val selectedTier = VehicleCatalog.tiers.find { it.id == selectedVehicleId } ?: VehicleCatalog.tiers.first()

    val effectiveDirections = directionsResult ?: remember(effectivePickupPlace, effectiveDropoffPlace, selectedTier) {
        GoogleMapsRoutingService.calculateDirections(
            pickup = effectivePickupPlace,
            dropoff = effectiveDropoffPlace,
            profile = selectedTier.routingProfile
        )
    }

    val totalFare = calculateFare(selectedVehicleId)

    // UI state
    var showPickupAutocomplete by remember { mutableStateOf(false) }
    var showDropoffAutocomplete by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Validation state
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isPickupInvalid by remember { mutableStateOf(false) }
    var isDropoffInvalid by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        val p = pickupAddress.trim()
        val d = dropoffAddress.trim()

        if (p.isEmpty() || p.length < 3) {
            validationErrorMessage = "Please enter a valid pickup address."
            isPickupInvalid = true
            isDropoffInvalid = false
            return false
        }
        if (d.isEmpty() || d.length < 3) {
            validationErrorMessage = "Please enter a valid drop-off destination."
            isPickupInvalid = false
            isDropoffInvalid = true
            return false
        }
        if (p.equals(d, ignoreCase = true) || effectivePickupPlace.placeId == effectiveDropoffPlace.placeId) {
            validationErrorMessage = "Pickup and Drop-off locations cannot be identical."
            isPickupInvalid = true
            isDropoffInvalid = true
            return false
        }

        validationErrorMessage = null
        isPickupInvalid = false
        isDropoffInvalid = false
        return true
    }

    // Modern Scaffold Architecture
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Confirm Freight Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Actual Road Distance Matrix", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "${effectiveDirections.actualRoadKm} km",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BookingBottomBar(
                totalFare = totalFare,
                distanceKm = effectiveDirections.actualRoadKm,
                etaMinutes = effectiveDirections.etaMinutes,
                onConfirmClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    if (validateInputs()) showConfirmationDialog = true
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Validation Error Banner
            AnimatedVisibility(
                visible = validationErrorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                validationErrorMessage?.let { errMsg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(errMsg, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Interactive Route Map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
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

                    // Routing Profile Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (selectedTier.routingProfile) {
                                    RoutingProfile.TWO_WHEELER -> Icons.Default.TwoWheeler
                                    RoutingProfile.THREE_WHEELER -> Icons.Default.ElectricRickshaw
                                    RoutingProfile.TATA_ACE_COMMERCIAL -> Icons.Default.LocalShipping
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedTier.routingProfile.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Inputs
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Route Waypoints", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = {
                                    val tempP = pickupAddress
                                    val tempPlace = effectivePickupPlace
                                    onPickupChange(dropoffAddress)
                                    onDropoffChange(tempP)
                                    onSelectPickupPlace?.invoke(effectiveDropoffPlace)
                                    onSelectDropoffPlace?.invoke(tempPlace)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Swap Locations", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pickup Input
                        LocationInputField(
                            value = pickupAddress,
                            label = "Pickup Location (Sender)",
                            isError = isPickupInvalid,
                            iconColor = Color.Green,
                            onValueChange = {
                                onPickupChange(it)
                                showPickupAutocomplete = it.isNotBlank()
                                validationErrorMessage = null
                                isPickupInvalid = false
                            },
                            onClear = { onPickupChange("") }
                        )

                        // Autocomplete Logic (Pickup)
                        if (showPickupAutocomplete && pickupAddress.length >= 2) {
                            AutocompleteDropdown(
                                query = pickupAddress,
                                onSelect = { place ->
                                    if (onSelectPickupPlace != null) onSelectPickupPlace(place) else onPickupChange(place.name)
                                    showPickupAutocomplete = false
                                    focusManager.clearFocus()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dropoff Input
                        LocationInputField(
                            value = dropoffAddress,
                            label = "Drop-off Destination (Consignee)",
                            isError = isDropoffInvalid,
                            iconColor = MaterialTheme.colorScheme.error,
                            onValueChange = {
                                onDropoffChange(it)
                                showDropoffAutocomplete = it.isNotBlank()
                                validationErrorMessage = null
                                isDropoffInvalid = false
                            },
                            onClear = { onDropoffChange("") }
                        )

                        // Autocomplete Logic (Dropoff)
                        if (showDropoffAutocomplete && dropoffAddress.length >= 2) {
                            AutocompleteDropdown(
                                query = dropoffAddress,
                                onSelect = { place ->
                                    if (onSelectDropoffPlace != null) onSelectDropoffPlace(place) else onDropoffChange(place.name)
                                    showDropoffAutocomplete = false
                                    focusManager.clearFocus()
                                }
                            )
                        }

                        // Popular Hubs (Production-ready implementation)
                        if (popularHubs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Popular Freight Hubs:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(popularHubs) { hub ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            if (onSelectDropoffPlace != null) onSelectDropoffPlace(hub) else onDropoffChange(hub.name)
                                            validationErrorMessage = null
                                            isDropoffInvalid = false
                                        }
                                    ) {
                                        Text(
                                            text = hub.name.substringBefore(" "),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Vehicle Selection
                Text("Commercial Vehicle Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Dynamic pricing based on routing profile", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(VehicleCatalog.tiers) { tier ->
                        VehicleSelectionCard(
                            tier = tier,
                            isSelected = tier.id == selectedVehicleId,
                            estimatedFare = calculateFare(tier.id),
                            onClick = { onVehicleSelect(tier.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cargo Options
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cargo Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(VehicleCatalog.goodsCategories) { cat ->
                                val isSel = cat == selectedGoodsType
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onGoodsSelect(cat) }
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(role = Role.Checkbox) { onToggleHelper() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dedicated Loading Helper", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Driver assists with ground-floor loading (+₹80)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Checkbox(
                                checked = isHelperRequired,
                                onCheckedChange = { onToggleHelper() },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }

    // Extracted Confirmation Dialog to prevent full screen recomposition
    if (showConfirmationDialog) {
        BookingConfirmationDialog(
            pickupPlace = effectivePickupPlace,
            dropoffPlace = effectiveDropoffPlace,
            selectedTier = selectedTier,
            actualRoadKm = effectiveDirections.actualRoadKm,
            etaMinutes = effectiveDirections.etaMinutes,
            totalFare = totalFare,
            onConfirm = {
                showConfirmationDialog = false
                onConfirmBooking()
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }
}

// ============================================================================
// Sub-Components
// ============================================================================

@Composable
private fun BookingBottomBar(
    totalFare: Double,
    distanceKm: Double,
    etaMinutes: Int,
    onConfirmClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ESTIMATED FARE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${totalFare.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("$distanceKm km • ~$etaMinutes mins ETA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onConfirmClick,
                modifier = Modifier.height(52.dp).testTag("confirm_booking_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun LocationInputField(
    value: String,
    label: String,
    isError: Boolean,
    iconColor: Color,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = iconColor) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                }
            }
        },
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun AutocompleteDropdown(
    query: String,
    onSelect: (PlaceModel) -> Unit
) {
    val matches = GoogleMapsRoutingService.searchPlaces(query)
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            matches.forEach { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(place) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(place.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("${place.category} • ID: ${place.placeId.take(8)}...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun VehicleSelectionCard(
    tier: VehicleTier,
    isSelected: Boolean,
    estimatedFare: Double,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "container"
    )

    val icon = when (tier.routingProfile) {
        RoutingProfile.TWO_WHEELER -> Icons.Default.TwoWheeler
        RoutingProfile.THREE_WHEELER -> Icons.Default.ElectricRickshaw
        RoutingProfile.TATA_ACE_COMMERCIAL -> Icons.Default.LocalShipping
    }

    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("vehicle_tier_${tier.id}"),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tier.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(tier.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(tier.capacity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "₹${estimatedFare.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BookingConfirmationDialog(
    pickupPlace: PlaceModel,
    dropoffPlace: PlaceModel,
    selectedTier: VehicleTier,
    actualRoadKm: Double,
    etaMinutes: Int,
    totalFare: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Dispatch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Please verify your route and details:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pickup Location", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(pickupPlace.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Drop-off Destination", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(dropoffPlace.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ConfirmationRow("Vehicle Tier:", selectedTier.name)
                ConfirmationRow("Road Distance:", "$actualRoadKm km")
                ConfirmationRow("Estimated Arrival:", "~$etaMinutes mins")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Fare:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${totalFare.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm & Dispatch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Edit Details")
            }
        }
    )
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
