package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TripOrigin
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
import com.example.data.model.BookingOrder
import com.example.data.model.VehicleCatalog
import com.example.data.model.VehicleTier
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
import kotlin.math.roundToInt

@Composable
fun BookingFlowScreen(
    pickupAddress: String,
    dropoffAddress: String,
    selectedVehicleId: String,
    selectedGoodsType: String,
    isHelperRequired: Boolean,
    selectedPaymentMethod: String,
    routeDistanceInfo: RouteDistanceInfo? = null,
    onPickupChange: (String) -> Unit,
    onDropoffChange: (String) -> Unit,
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
    val effectiveRouteInfo = routeDistanceInfo ?: remember(pickupAddress, dropoffAddress) {
        DistanceCalculator.calculateExactDistance(pickupAddress, dropoffAddress)
    }
    val totalFare = calculateFare(selectedVehicleId)
    val selectedTier = VehicleCatalog.tiers.find { it.id == selectedVehicleId } ?: VehicleCatalog.tiers[0]

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
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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
                Text(
                    text = "Confirm Freight Booking",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        }

        // Scrollable Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Route Confirmation Badge (Always Visible at Top of Form)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = AmberContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ACTIVE FREIGHT CORRIDOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberPrimaryDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberPrimary
                            ) {
                                Text(
                                    text = "⚡ ${effectiveRouteInfo.distanceKm} km",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${pickupAddress.substringBefore(",").ifEmpty { "Select Pickup" }} ➔ ${dropoffAddress.substringBefore(",").ifEmpty { "Select Destination" }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = "${effectiveRouteInfo.viaRoad} • ~${effectiveRouteInfo.estimatedDurationMinutes} mins transit",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            // Pickup & Dropoff Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pickup & Drop Locations",
                                fontSize = 14.sp,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pickup Input
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
                                .testTag("pickup_address_input"),
                            label = { Text("Pickup Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            placeholder = { Text("Search pickup address or warehouse...", fontSize = 13.sp, color = TextSubtle) },
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

                    // Dropoff Input
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
                                .testTag("dropoff_address_input"),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fast Location Quick Chips
                    Text(
                        text = "⚡ Instant Quick-Fill Hubs:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val hubs = listOf(
                            "Koramangala 4th Block",
                            "Indiranagar 100ft Rd",
                            "Whitefield EPIP Zone",
                            "Electronic City Ph 1",
                            "Peenya Industrial Area",
                            "HSR Layout Sector 2",
                            "Rajajinagar 2nd Stage",
                            "Kempegowda Airport BLR"
                        )
                        items(hubs) { hub ->
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

            Spacer(modifier = Modifier.height(18.dp))

            // Vehicle Tier Selector
            Text(
                text = "Select Vehicle Capacity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Dynamic pricing based on weight & distance",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(VehicleCatalog.tiers) { tier ->
                    val isSelected = tier.id == selectedVehicleId
                    VehicleSelectorCard(
                        tier = tier,
                        isSelected = isSelected,
                        estimatedFare = calculateFare(tier.id),
                        estimatedDistanceKm = effectiveRouteInfo.distanceKm,
                        onClick = { onVehicleSelect(tier.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Goods Type Chips
            Text(
                text = "Category of Goods",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(VehicleCatalog.goodsCategories) { category ->
                    val isSelected = category == selectedGoodsType
                    GoodsTypeChip(
                        category = category,
                        isSelected = isSelected,
                        onClick = { onGoodsSelect(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Helper Assistance Toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleHelper() }
                    .testTag("helper_toggle_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHelperRequired) AmberContainer else SurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isHelperRequired) AmberPrimary else BorderLight
                )
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
                                .background(
                                    if (isHelperRequired) AmberPrimary else SurfaceTertiary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Helper",
                                tint = if (isHelperRequired) Color.White else TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Need Loading Help? (+₹80)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Trained driver helper for ground-to-ground loading",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Checkbox(
                        checked = isHelperRequired,
                        onCheckedChange = { onToggleHelper() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AmberPrimary,
                            uncheckedColor = TextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Payment Methods
            Text(
                text = "Payment Method",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleCatalog.paymentOptions.forEach { option ->
                    val isSelected = option == selectedPaymentMethod
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AmberPrimary else BorderLight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onPaymentSelect(option) },
                        color = if (isSelected) AmberContainer else SurfaceCard
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = option,
                                tint = if (isSelected) AmberPrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = option.split(" ")[0],
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AmberPrimary else TextDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Fare Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceTertiary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Fare Estimate Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FareRow(label = "Pickup Area", value = pickupAddress.substringBefore(",").ifEmpty { "Not set" })
                    FareRow(label = "Drop-off Area", value = dropoffAddress.substringBefore(",").ifEmpty { "Not set" })
                    FareRow(label = "Exact Route Distance", value = "${effectiveRouteInfo.distanceKm} km")
                    FareRow(label = "Routing Corridor", value = effectiveRouteInfo.viaRoad)
                    FareRow(label = "Estimated Transit Time", value = "~${effectiveRouteInfo.estimatedDurationMinutes} mins")
                    FareRow(label = "Base Fare (${selectedTier.name})", value = "₹${selectedTier.baseFare.toInt()}")
                    FareRow(
                        label = "Distance Fare (₹${selectedTier.perKmRate.toInt()}/km × ${effectiveRouteInfo.distanceKm} km)",
                        value = "₹${(effectiveRouteInfo.distanceKm * selectedTier.perKmRate).roundToInt()}"
                    )
                    if (isHelperRequired) {
                        FareRow(label = "Helper Assistance", value = "₹80")
                    }
                    FareRow(label = "Tolls & GST", value = "Included")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Total",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            text = "₹${totalFare.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Sticky Bottom CTA Bar
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOTAL ESTIMATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = " • ${effectiveRouteInfo.distanceKm} km",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary
                        )
                    }
                    Text(
                        text = "₹${totalFare.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberPrimary
                    )
                    Text(
                        text = "${pickupAddress.substringBefore(",").ifEmpty { "Pickup" }} ➔ ${dropoffAddress.substringBefore(",").ifEmpty { "Drop" }} (~${effectiveRouteInfo.estimatedDurationMinutes}m)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onConfirmBooking()
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
}

@Composable
private fun FareRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
private fun VehicleSelectorCard(
    tier: VehicleTier,
    isSelected: Boolean,
    estimatedFare: Double,
    estimatedDistanceKm: Double = 0.0,
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

    val icon: ImageVector = when (tier.id) {
        "bike" -> Icons.Default.TwoWheeler
        "auto" -> Icons.Default.ElectricRickshaw
        else -> Icons.Default.LocalShipping
    }

    Card(
        modifier = Modifier
            .width(155.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

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
                Column(horizontalAlignment = Alignment.End) {
                    if (estimatedDistanceKm > 0.0) {
                        Text(
                            text = "$estimatedDistanceKm km",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary
                        )
                    }
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
}

@Composable
private fun GoodsTypeChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) AmberPrimary else BorderLight,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("goods_chip_${category.take(4)}"),
        color = if (isSelected) AmberPrimary else SurfaceCard
    ) {
        Text(
            text = category,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextDark,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
