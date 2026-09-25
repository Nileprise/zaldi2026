package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.data.model.UserRole
import com.example.data.model.VehicleCatalog
import com.example.data.model.VehicleTier
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.RoleSwitcherPill
import com.example.ui.components.SimulatedMapView
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
import com.example.util.DistanceCalculator
import com.example.util.LocationSearchHelper
import com.example.util.RouteDistanceInfo

@Composable
fun CustomerHomeScreen(
    activeOrder: BookingOrder?,
    orderHistory: List<BookingOrder>,
    currentRole: UserRole,
    pickupAddress: String = "Indiranagar 100ft Rd, Bengaluru",
    dropoffAddress: String = "Koramangala 4th Block, Bengaluru",
    routeDistanceInfo: RouteDistanceInfo? = null,
    onPickupChange: (String) -> Unit = {},
    onDropoffChange: (String) -> Unit = {},
    onSetCustomDistance: (Double) -> Unit = {},
    onResetDistance: () -> Unit = {},
    onRoleSelected: (UserRole) -> Unit,
    onStartBooking: () -> Unit,
    onViewActiveRide: () -> Unit,
    driverLocation: DriverLocationData? = null,
    onOpenLiveMap: () -> Unit = {},
    onQuickBook: (pickup: String, dropoff: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var isGoogleMapsMode by remember { mutableStateOf(false) }
    val effectiveRouteInfo = routeDistanceInfo ?: remember(pickupAddress, dropoffAddress) {
        DistanceCalculator.calculateExactDistance(pickupAddress, dropoffAddress)
    }

    var showExactDistanceDialog by remember { mutableStateOf(false) }
    var customDistanceInputText by remember { mutableStateOf("") }
    var showPickupSuggestions by remember { mutableStateOf(false) }
    var showDropoffSuggestions by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Segmented Role Switcher at the very top
        RoleSwitcherPill(
            currentRole = currentRole,
            onRoleSelected = onRoleSelected
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = AmberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Indiranagar, Bengaluru",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "Book Intra-City Freight",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = AmberContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AS",
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Ride Floating Alert (if exists)
            if (activeOrder != null && activeOrder.status != "DELIVERED" && activeOrder.status != "CANCELLED") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onViewActiveRide() }
                        .testTag("active_ride_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Active Ride",
                                    tint = Color.White
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "RIDE IN PROGRESS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${activeOrder.driverName} • ${activeOrder.vehicleName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "OTP: ${activeOrder.startOtp} • Tap to view live tracking",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View",
                            tint = Color.White
                        )
                    }
                }
            }

            // Map Controls Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(SurfaceTertiary, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isGoogleMapsMode) AmberPrimary else Color.Transparent,
                        modifier = Modifier.clickable { isGoogleMapsMode = false }
                    ) {
                        Text(
                            text = "Vector Map ⚡ Fast",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isGoogleMapsMode) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isGoogleMapsMode) AmberPrimary else Color.Transparent,
                        modifier = Modifier.clickable { isGoogleMapsMode = true }
                    ) {
                        Text(
                            text = "Google Maps SDK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoogleMapsMode) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onOpenLiveMap() }.testTag("open_fullscreen_map_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Full Map",
                            tint = AmberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Full Map",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Interactive Map Widget
            if (isGoogleMapsMode) {
                GoogleMapsView(
                    driverLocation = driverLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    isInteractive = true
                )
            } else {
                SimulatedMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    isTrackingActiveRide = activeOrder != null,
                    pickupName = activeOrder?.pickupAddress ?: "Indiranagar 100ft Rd",
                    dropoffName = activeOrder?.dropoffAddress ?: "Koramangala 4th Block",
                    etaMinutes = activeOrder?.etaMinutes ?: 11,
                    driverLocation = driverLocation
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Plan Delivery CTA Box (Instant Booking Search)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Instant Booking Search",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = LogisticsBlueContainer,
                            modifier = Modifier.clickable {
                                val temp = pickupAddress
                                onPickupChange(dropoffAddress)
                                onDropoffChange(temp)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Locations",
                                    tint = LogisticsBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Swap",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LogisticsBlue,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pickup Search Input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                        }
                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = {
                                onPickupChange(it)
                                showPickupSuggestions = it.isNotBlank()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                                .testTag("home_pickup_search_input"),
                            label = { Text("Pickup Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            placeholder = { Text("Search pickup area or hub...", fontSize = 13.sp, color = TextSubtle) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (pickupAddress.isNotEmpty()) {
                                    IconButton(onClick = {
                                        focusManager.clearFocus()
                                        onPickupChange("")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Pickup",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedLabelColor = AmberPrimary,
                                unfocusedLabelColor = TextDark,
                                cursorColor = AmberPrimary
                            )
                        )
                    }

                    if (showPickupSuggestions && pickupAddress.length >= 2) {
                        val pickupMatches = LocationSearchHelper.search(pickupAddress)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        ) {
                            pickupMatches.take(3).forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onPickupChange(match)
                                            showPickupSuggestions = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Text(text = match, fontSize = 11.sp, color = TextDark, modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dropoff Search Input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AmberPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(AmberPrimary, CircleShape)
                            )
                        }
                        OutlinedTextField(
                            value = dropoffAddress,
                            onValueChange = {
                                onDropoffChange(it)
                                showDropoffSuggestions = it.isNotBlank()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                                .testTag("home_dropoff_search_input"),
                            label = { Text("Drop-off Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            placeholder = { Text("Search delivery destination...", fontSize = 13.sp, color = TextSubtle) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (dropoffAddress.isNotEmpty()) {
                                    IconButton(onClick = {
                                        focusManager.clearFocus()
                                        onDropoffChange("")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Drop-off",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedLabelColor = AmberPrimary,
                                unfocusedLabelColor = TextDark,
                                cursorColor = AmberPrimary
                            )
                        )
                    }

                    if (showDropoffSuggestions && dropoffAddress.length >= 2) {
                        val dropMatches = LocationSearchHelper.search(dropoffAddress)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        ) {
                            dropMatches.take(3).forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDropoffChange(match)
                                            showDropoffSuggestions = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(14.dp))
                                    Text(text = match, fontSize = 11.sp, color = TextDark, modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant Quick Fill Chips
                    Text(
                        text = "⚡ Instant Quick-Fill Hubs:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val quickHubs = listOf(
                            "Koramangala 4th Block",
                            "Indiranagar 100ft Rd",
                            "Whitefield EPIP Zone",
                            "Electronic City Ph 1",
                            "Peenya Industrial Area",
                            "HSR Layout Sector 2",
                            "Rajajinagar 2nd Stage",
                            "Kempegowda Airport BLR"
                        )
                        items(quickHubs) { hub ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceTertiary,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable {
                                    onDropoffChange("$hub, Bengaluru")
                                }
                            ) {
                                Text(
                                    text = hub,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Estimated Distance Card with Precision Controls
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.55f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AmberPrimary,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "⚡", fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "ESTIMATED DISTANCE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmberPrimaryDark,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${effectiveRouteInfo.viaRoad} • ~${effectiveRouteInfo.estimatedDurationMinutes} mins",
                                            fontSize = 11.sp,
                                            color = TextDark,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberPrimary,
                                    modifier = Modifier.clickable {
                                        customDistanceInputText = effectiveRouteInfo.distanceKm.toString()
                                        showExactDistanceDialog = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${effectiveRouteInfo.distanceKm} km",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Distance",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Distance Adjuster & Presets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                        modifier = Modifier.clickable {
                                            val newDist = kotlin.math.max(1.0, effectiveRouteInfo.distanceKm - 1.0)
                                            onSetCustomDistance(newDist)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease 1 km", modifier = Modifier.size(12.dp), tint = TextDark)
                                            Text(text = "1 km", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                        modifier = Modifier.clickable {
                                            onSetCustomDistance(effectiveRouteInfo.distanceKm + 1.0)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase 1 km", modifier = Modifier.size(12.dp), tint = TextDark)
                                            Text(text = "1 km", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                        modifier = Modifier.clickable { onResetDistance() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Auto", modifier = Modifier.size(12.dp), tint = AmberPrimary)
                                            Text(text = "Auto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                                        }
                                    }
                                }

                                // Quick presets
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val presets = listOf(5.0, 10.0, 15.0, 25.0)
                                    items(presets) { km ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (effectiveRouteInfo.distanceKm == km) AmberPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (effectiveRouteInfo.distanceKm == km) AmberPrimary else BorderLight),
                                            modifier = Modifier.clickable { onSetCustomDistance(km) }
                                        ) {
                                            Text(
                                                text = "${km.toInt()}k",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (effectiveRouteInfo.distanceKm == km) Color.White else TextDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Direct Exact Distance Edit Dialog
                    if (showExactDistanceDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                showExactDistanceDialog = false
                            },
                            title = {
                                Text(text = "Set Exact Estimated Distance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "Enter exact distance in kilometers for this delivery route:",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = customDistanceInputText,
                                        onValueChange = { customDistanceInputText = it },
                                        label = { Text("Exact Distance (km)") },
                                        placeholder = { Text("e.g. 7.4 or 12.5") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        val entered = customDistanceInputText.toDoubleOrNull()
                                        if (entered != null && entered > 0.0) {
                                            onSetCustomDistance(entered)
                                        }
                                        showExactDistanceDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                                ) {
                                    Text("Apply Distance")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showExactDistanceDialog = false
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onStartBooking()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("book_goods_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Text(
                            text = "Select Vehicle & View Fares",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1-Tap Fast Booking Corridors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ 1-Tap Express Corridors",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Instant Pre-fill",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberPrimary
                )
            }
            Text(
                text = "Tap any frequent delivery route to book instantly",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FastCorridorCard(
                        from = "Indiranagar",
                        to = "Koramangala",
                        dist = "11 km • ~₹210",
                        onClick = {
                            onQuickBook("Indiranagar 100ft Rd, Bengaluru", "Koramangala 4th Block, Bengaluru")
                        }
                    )
                }
                item {
                    FastCorridorCard(
                        from = "Peenya Industrial",
                        to = "Rajajinagar",
                        dist = "8 km • ~₹165",
                        onClick = {
                            onQuickBook("Peenya Industrial Area, Phase 1", "Rajajinagar 2nd Stage, Bengaluru")
                        }
                    )
                }
                item {
                    FastCorridorCard(
                        from = "Whitefield",
                        to = "Electronic City",
                        dist = "19 km • ~₹350",
                        onClick = {
                            onQuickBook("Whitefield Export Promotion Park", "Electronic City Phase 1, Hosur Rd")
                        }
                    )
                }
                item {
                    FastCorridorCard(
                        from = "HSR Layout",
                        to = "JP Nagar",
                        dist = "6 km • ~₹130",
                        onClick = {
                            onQuickBook("HSR Layout Sector 2, Bengaluru", "JP Nagar 6th Phase, Bengaluru")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fleet Tier Showcase
            Text(
                text = "Vehicle Fleet Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(VehicleCatalog.tiers) { tier ->
                    VehicleShowcaseCard(
                        tier = tier,
                        onClick = onStartBooking
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Recent Trips / Activity
            if (orderHistory.isNotEmpty()) {
                Text(
                    text = "Recent Shipments",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                orderHistory.take(3).forEach { order ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = order.id,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = order.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (order.status == "DELIVERED") SuccessGreen else LogisticsBlue
                                    )
                                }
                                Text(
                                    text = "${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextDark,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "${order.vehicleName} • ${order.goodsType}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "₹${order.fare.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust & Features Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LogisticsBlueContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = LogisticsBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Transparent Toll & Distance Pricing",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LogisticsBlue
                        )
                        Text(
                            text = "Live GPS tracking, verified driver KYC, and trained helper option on all freight tiers.",
                            fontSize = 11.sp,
                            color = TextDark.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VehicleShowcaseCard(
    tier: VehicleTier,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (tier.id) {
        "bike" -> Icons.Default.TwoWheeler
        "auto" -> Icons.Default.ElectricRickshaw
        else -> Icons.Default.LocalShipping
    }

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AmberContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tier.name,
                    tint = AmberPrimary,
                    modifier = Modifier.size(20.dp)
                )
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

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "From ₹${tier.baseFare.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberPrimary
                )
                Text(
                    text = "${tier.etaMinutes}m",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun FastCorridorCard(
    from: String,
    to: String,
    dist: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        shadowElevation = 1.5.dp,
        modifier = modifier
            .width(170.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LogisticsBlueContainer
                ) {
                    Text(
                        text = "⚡ Instant",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LogisticsBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = from,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )
            Text(
                text = "➔ $to",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AmberPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dist,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
