package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.data.model.UserRole
import com.example.data.model.VehicleCatalog
import com.example.data.model.VehicleTier
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.RoleSwitcherPill
import com.example.ui.components.SimulatedMapView
import com.example.util.LocationSearchHelper

// Represents a pre-defined popular corridor for production use
data class FastCorridor(val from: String, val to: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    activeOrder: BookingOrder?,
    orderHistory: List<BookingOrder>,
    currentRole: UserRole,
    pickupAddress: String = "",
    dropoffAddress: String = "",
    onPickupChange: (String) -> Unit = {},
    onDropoffChange: (String) -> Unit = {},
    onRoleSelected: (UserRole) -> Unit,
    onStartBooking: () -> Unit,
    onViewActiveRide: () -> Unit,
    driverLocation: DriverLocationData? = null,
    onOpenLiveMap: () -> Unit = {},
    onQuickBook: (pickup: String, dropoff: String) -> Unit = { _, _ -> },
    popularCorridors: List<FastCorridor> = emptyList(), // Passed from ViewModel in production
    modifier: Modifier = Modifier
) {
    var isGoogleMapsMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                RoleSwitcherPill(
                    currentRole = currentRole,
                    onRoleSelected = onRoleSelected
                )
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Your Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text("Book Intra-City Freight", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    },
                    actions = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp).padding(end = 8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("AS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- Active Ride Banner ---
            if (activeOrder != null && activeOrder.status != "DELIVERED" && activeOrder.status != "CANCELLED") {
                ActiveRideBanner(order = activeOrder, onClick = onViewActiveRide)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Map Section ---
            MapSection(
                isGoogleMapsMode = isGoogleMapsMode,
                onModeToggle = { isGoogleMapsMode = it },
                onOpenLiveMap = onOpenLiveMap,
                driverLocation = driverLocation,
                activeOrder = activeOrder
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Primary Search Card ---
            BookingSearchCard(
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                onPickupChange = onPickupChange,
                onDropoffChange = onDropoffChange,
                onStartBooking = onStartBooking
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Fast Corridors (1-Tap Booking) ---
            if (popularCorridors.isNotEmpty()) {
                Text("⚡ 1-Tap Express Corridors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tap any frequent route to book instantly", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(popularCorridors) { corridor ->
                        FastCorridorCard(
                            corridor = corridor,
                            onClick = { onQuickBook(corridor.from, corridor.to) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Fleet Showcase ---
            Text("Vehicle Fleet Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(VehicleCatalog.tiers) { tier ->
                    VehicleShowcaseCard(tier = tier, onClick = onStartBooking)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Recent Trips ---
            if (orderHistory.isNotEmpty()) {
                Text("Recent Shipments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                orderHistory.take(3).forEach { order ->
                    RecentTripCard(order = order)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Trust Banner ---
            TrustBanner()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================================
// Extracted Sub-Components
// ============================================================================

@Composable
private fun ActiveRideBanner(order: BookingOrder, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() }
            .testTag("active_ride_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Navigation, contentDescription = null)
                    }
                }
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text("RIDE IN PROGRESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("${order.driverName} • ${order.vehicleName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("OTP: ${order.startOtp} • Tap to view live tracking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View Ride")
        }
    }
}

@Composable
private fun MapSection(
    isGoogleMapsMode: Boolean,
    onModeToggle: (Boolean) -> Unit,
    onOpenLiveMap: () -> Unit,
    driverLocation: DriverLocationData?,
    activeOrder: BookingOrder?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isGoogleMapsMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.clickable(role = Role.Tab) { onModeToggle(false) }
                    ) {
                        Text("Vector Map", style = MaterialTheme.typography.labelMedium, color = if (!isGoogleMapsMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isGoogleMapsMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.clickable(role = Role.Tab) { onModeToggle(true) }
                    ) {
                        Text("SDK Map", style = MaterialTheme.typography.labelMedium, color = if (isGoogleMapsMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.clickable(role = Role.Button) { onOpenLiveMap() }
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expand", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (isGoogleMapsMode) {
                GoogleMapsView(
                    driverLocation = driverLocation,
                    modifier = Modifier.fillMaxSize(),
                    isInteractive = true
                )
            } else {
                SimulatedMapView(
                    modifier = Modifier.fillMaxSize(),
                    isTrackingActiveRide = activeOrder != null,
                    pickupName = activeOrder?.pickupAddress ?: "Pickup",
                    dropoffName = activeOrder?.dropoffAddress ?: "Drop-off",
                    etaMinutes = activeOrder?.etaMinutes ?: 0,
                    driverLocation = driverLocation
                )
            }
        }
    }
}

@Composable
private fun BookingSearchCard(
    pickupAddress: String,
    dropoffAddress: String,
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
    onStartBooking: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showPickupSuggestions by remember { mutableStateOf(false) }
    var showDropoffSuggestions by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Booking Search", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        val temp = pickupAddress
                        onPickupChange(dropoffAddress)
                        onDropoffChange(temp)
                    }
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Swap Locations", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pickup Input
            LocationSearchInput(
                value = pickupAddress,
                label = "Pickup Location",
                iconColor = Color(0xFF10B981), // Emerald Green
                onValueChange = {
                    onPickupChange(it)
                    showPickupSuggestions = it.isNotBlank()
                },
                onClear = {
                    onPickupChange("")
                    focusManager.clearFocus()
                }
            )

            // Pickup Autocomplete
            AnimatedVisibility(visible = showPickupSuggestions && pickupAddress.length >= 2) {
                AutocompleteDropdown(
                    query = pickupAddress,
                    onSelect = {
                        onPickupChange(it)
                        showPickupSuggestions = false
                        focusManager.clearFocus()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dropoff Input
            LocationSearchInput(
                value = dropoffAddress,
                label = "Drop-off Destination",
                iconColor = MaterialTheme.colorScheme.primary,
                onValueChange = {
                    onDropoffChange(it)
                    showDropoffSuggestions = it.isNotBlank()
                },
                onClear = {
                    onDropoffChange("")
                    focusManager.clearFocus()
                }
            )

            // Dropoff Autocomplete
            AnimatedVisibility(visible = showDropoffSuggestions && dropoffAddress.length >= 2) {
                AutocompleteDropdown(
                    query = dropoffAddress,
                    onSelect = {
                        onDropoffChange(it)
                        showDropoffSuggestions = false
                        focusManager.clearFocus()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onStartBooking()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = pickupAddress.isNotBlank() && dropoffAddress.isNotBlank()
            ) {
                Text("Select Vehicle & View Fares", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LocationSearchInput(
    value: String,
    label: String,
    iconColor: Color,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Box(
                modifier = Modifier.size(24.dp).background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(10.dp).background(iconColor, CircleShape))
            }
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                }
            }
        }
    )
}

@Composable
private fun AutocompleteDropdown(query: String, onSelect: (String) -> Unit) {
    val matches = LocationSearchHelper.search(query).take(3)
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            matches.forEach { match ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onSelect(match) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(match, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun FastCorridorCard(corridor: FastCorridor, onClick: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable(role = Role.Button) { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text("⚡ Instant", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(corridor.from, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("➔ ${corridor.to}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))
            Text(corridor.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VehicleShowcaseCard(tier: VehicleTier, onClick: () -> Unit) {
    val icon = when (tier.id) {
        "bike" -> Icons.Default.TwoWheeler
        "auto" -> Icons.Default.ElectricRickshaw
        else -> Icons.Default.LocalShipping
    }

    OutlinedCard(
        modifier = Modifier
            .width(150.dp)
            .clickable(role = Role.Button) { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(tier.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(tier.capacity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text("From ₹${tier.baseFare.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RecentTripCard(order: BookingOrder) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.id, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (order.status == "DELIVERED") Color(0xFF059669) else MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${order.vehicleName} • ${order.goodsType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("₹${order.fare.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TrustBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("Transparent Toll & Distance Pricing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Live GPS tracking, verified driver KYC, and trained helper options.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
            }
        }
    }
}
