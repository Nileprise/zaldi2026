package com.example.ui.customer

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LocationPoint
import com.example.data.model.VehicleType
import com.example.ui.components.LocationIndicatorDot
import com.example.ui.components.RapidoRoutePlannerDialog
import com.example.ui.components.ZaldiPermissionBanner
import com.example.ui.components.rememberZaldiPermissionsState
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.ZaldiViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CustomerHomeScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPickup by viewModel.selectedPickup.collectAsState()
    val selectedDrop by viewModel.selectedDrop.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedGoodsType by viewModel.selectedGoodsType.collectAsState()
    val goodsWeightKg by viewModel.goodsWeightKg.collectAsState()
    val helperCount by viewModel.helperCount.collectAsState()
    val promoInput by viewModel.promoCodeInput.collectAsState()
    val fareBreakdown by viewModel.fareBreakdown.collectAsState()
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()

    val permissionsState = rememberZaldiPermissionsState()

    var showLocationSheetForPickup by remember { mutableStateOf(false) }
    var showLocationSheetForDrop by remember { mutableStateOf(false) }
    var showRouteDirectionsSheet by remember { mutableStateOf(false) }
    var showRapidoPlanner by remember { mutableStateOf(false) }
    var promoText by remember { mutableStateOf(promoInput) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp)
            .testTag("customer_home_screen")
    ) {
        // Runtime Location & Notification Permission Banner (Accompanist)
        item {
            ZaldiPermissionBanner(
                permissionsState = permissionsState,
                role = AppRole.CUSTOMER
            )
        }

        // Hero Banner Art
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card")
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_delivery_hero),
                        contentDescription = "Zaldi Delivery Fleet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Color(0xFF0F172A).copy(alpha = 0.55f)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "Lightning Fast Freight Delivery",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Bikes, Autos & Trucks • Live GPS tracking • Verified Drivers",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Active Booking Quick Resume Bar if exists
        if (activeBooking != null && !activeBooking!!.isTerminalState) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    onClick = { viewModel.setCustomerSubScreen(CustomerSubScreen.TRACKING) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth().testTag("resume_trip_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TRIP IN PROGRESS: ${activeBooking!!.bookingCode}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Driver: ${activeBooking!!.driverName ?: "Searching nearest partner..."}",
                                color = Color(0xFFE0F2FE),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View Trip",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 1. Pickup & Drop Locations Selector Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("locations_selector_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route Locations",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { showRapidoPlanner = true },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Rapido Map Pin",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map Pin Mode", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = {
                                    val temp = selectedPickup
                                    viewModel.setSelectedPickup(selectedDrop)
                                    viewModel.setSelectedDrop(temp)
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap Locations",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Swap", color = Color(0xFFF59E0B), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pickup Row with Green Location Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { showLocationSheetForPickup = true }
                            .padding(12.dp)
                            .testTag("pickup_location_selector")
                    ) {
                        LocationIndicatorDot(isPickup = true, size = 12)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PICKUP LOCATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                            Text(
                                text = selectedPickup.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = selectedPickup.address,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Change Pickup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Drop Row with Red/Orange Destination Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { showLocationSheetForDrop = true }
                            .padding(12.dp)
                            .testTag("drop_location_selector")
                    ) {
                        LocationIndicatorDot(isPickup = false, size = 12)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DROP DESTINATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171)
                                )
                            }
                            Text(
                                text = selectedDrop.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = selectedDrop.address,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Change Drop",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val dist = LocationPoint.calculateDistanceKm(
                        selectedPickup.latitude, selectedPickup.longitude,
                        selectedDrop.latitude, selectedDrop.longitude
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exact Road Distance: $dist km",
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Est. transit duration: ${(dist * 3.2 + 8).toInt()} mins in live traffic",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        OutlinedButton(
                            onClick = { showRouteDirectionsSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("view_route_directions_btn")
                        ) {
                            Text("Route Steps", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Select Vehicle Type
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select Vehicle",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(VehicleType.values().toList()) { vehicle ->
            val isSelected = selectedVehicle == vehicle
            val dist = LocationPoint.calculateDistanceKm(
                selectedPickup.latitude, selectedPickup.longitude,
                selectedDrop.latitude, selectedDrop.longitude
            )
            val approxPrice = (vehicle.defaultBaseFare + dist * vehicle.defaultPerKmFare).toInt()

            Card(
                onClick = { viewModel.setSelectedVehicle(vehicle) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31)
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("vehicle_card_${vehicle.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vehicle.getIcon(),
                            contentDescription = vehicle.title,
                            tint = if (isSelected) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = vehicle.sizeDescription,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = vehicle.idealFor,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹$approxPrice",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isSelected) Color(0xFFF59E0B) else Color.White
                        )
                        Text(
                            text = "Base ₹${vehicle.defaultBaseFare.toInt()}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 3. Goods Details & Helpers
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("goods_details_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Goods Type & Helper Options",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Goods categories pills
                    val goodsTypes = listOf(
                        "Commercial Cargo & Boxes",
                        "Furniture & Shifting",
                        "Electronics & Monitors",
                        "Food & Perishables",
                        "Hardware & Machinery"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(goodsTypes) { type ->
                            val isTypeSelected = selectedGoodsType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTypeSelected) Color(0xFFF59E0B) else Color(0xFF0F172A))
                                    .clickable { viewModel.setSelectedGoodsType(type, goodsWeightKg) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isTypeSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Helper selection
                    Text(
                        text = "Do you need loading / unloading helpers?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "Driver only (₹0)",
                            1 to "+1 Helper (+₹120)",
                            2 to "+2 Helpers (+₹240)"
                        ).forEach { (count, label) ->
                            val isCountSelected = helperCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCountSelected) Color(0xFF0284C7) else Color(0xFF0F172A))
                                    .clickable { viewModel.setHelperCount(count) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCountSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCountSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Promo Code & Fare Breakdown
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("fare_breakdown_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fare Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Promocode input row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoText,
                            onValueChange = { promoText = it.uppercase() },
                            placeholder = { Text("Enter Promo Code", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("promo_code_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.setPromoCode(promoText)
                                viewModel.showToast("Promo code applied!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("apply_promo_button")
                        ) {
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FareRow("Base Fare", "₹${fareBreakdown.baseFare.toInt()}")
                    FareRow("Distance Charge (${fareBreakdown.distanceFare.toInt()} km eq)", "₹${fareBreakdown.distanceFare.toInt()}")
                    if (fareBreakdown.helperFare > 0) {
                        FareRow("Helper Charge ($helperCount helper)", "₹${fareBreakdown.helperFare.toInt()}")
                    }
                    if (fareBreakdown.discount > 0) {
                        FareRow("Promo Discount (${fareBreakdown.appliedPromo})", "-₹${fareBreakdown.discount.toInt()}", isDiscount = true)
                    }

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Estimated Fare",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "₹${fareBreakdown.finalFare.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // 5. Booking Actions
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { viewModel.createBooking() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("book_freight_button")
            ) {
                Text(
                    text = "Book ${selectedVehicle.title} • ₹${fareBreakdown.finalFare.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Link to Booking History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setCustomerSubScreen(CustomerSubScreen.HISTORY) }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "View Past Bookings & Invoices",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog for selecting pickup location with Green indicator, Autocomplete & Fixed Pin
    if (showLocationSheetForPickup) {
        com.example.ui.components.PlaceSelectionDialog(
            title = "Enter Pickup Location",
            isPickup = true,
            initialLocation = selectedPickup,
            onDismiss = { showLocationSheetForPickup = false },
            onLocationConfirmed = {
                viewModel.setSelectedPickup(it)
                showLocationSheetForPickup = false
                viewModel.showToast("Pickup confirmed: ${it.title}")
            }
        )
    }

    // Dialog for selecting drop location with Red/Orange indicator, Autocomplete & Fixed Pin
    if (showLocationSheetForDrop) {
        com.example.ui.components.PlaceSelectionDialog(
            title = "Enter Drop Location",
            isPickup = false,
            initialLocation = selectedDrop,
            onDismiss = { showLocationSheetForDrop = false },
            onLocationConfirmed = {
                viewModel.setSelectedDrop(it)
                showLocationSheetForDrop = false
                viewModel.showToast("Drop confirmed: ${it.title}")
            }
        )
    }

    // Turn-by-Turn Route Guidance Modal
    if (showRouteDirectionsSheet) {
        com.example.ui.components.RouteDirectionsSheet(
            origin = selectedPickup,
            destination = selectedDrop,
            onDismiss = { showRouteDirectionsSheet = false }
        )
    }

    // Rapido-Style Unified Interactive Route Planner with Fixed Pin, Autocomplete & Seamless Zoom
    if (showRapidoPlanner) {
        com.example.ui.components.RapidoRoutePlannerDialog(
            initialPickup = selectedPickup,
            initialDrop = selectedDrop,
            onDismiss = { showRapidoPlanner = false },
            onRouteConfirmed = { pickup, drop ->
                viewModel.setSelectedPickup(pickup)
                viewModel.setSelectedDrop(drop)
                showRapidoPlanner = false
                viewModel.showToast("Route updated: ${pickup.title} ➔ ${drop.title}")
            }
        )
    }
}

@Composable
private fun FareRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDiscount) Color(0xFF34D399) else Color(0xFFCBD5E1)
        )
    }
}
