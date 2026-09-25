package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import com.example.ui.components.GoogleMapsView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.ui.components.ProofOfDeliveryDialog
import com.example.ui.components.SimulatedMapView
import com.example.util.NavigationUtils
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun ActiveRideScreen(
    order: BookingOrder,
    onBack: () -> Unit,
    onCancelRide: (String) -> Unit,
    onCompleteRide: (String) -> Unit,
    driverLocation: DriverLocationData? = null,
    onOpenLiveMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isGoogleMapsMode by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var showPodDialog by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            "Driver: Hello Akhil! I am 4 minutes away near 100ft road junction.",
            "Customer: Great, goods are packed and ready on ground floor."
        )
    }
    var newChatMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .statusBarsPadding()
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                    Text(
                        text = "Live Ride Telemetry",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // SOS Button
                Button(
                    onClick = { showSosDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .padding(end = 8.dp)
                        .testTag("sos_emergency_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SOS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
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
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isGoogleMapsMode) AmberPrimary else Color.Transparent,
                        modifier = Modifier.clickable { isGoogleMapsMode = false }
                    ) {
                        Text(
                            text = "Vector Map",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isGoogleMapsMode) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onOpenLiveMap() }.testTag("active_ride_fullscreen_map_button")
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

            // Map Widget
            if (isGoogleMapsMode) {
                GoogleMapsView(
                    driverLocation = driverLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    pickupTitle = order.pickupAddress,
                    dropoffTitle = order.dropoffAddress,
                    isInteractive = true
                )
            } else {
                SimulatedMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    isTrackingActiveRide = true,
                    pickupName = order.pickupAddress,
                    dropoffName = order.dropoffAddress,
                    etaMinutes = order.etaMinutes,
                    driverLocation = driverLocation
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Status & OTP Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AmberContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                            Text(
                                text = "DRIVER EN ROUTE TO PICKUP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberPrimary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        Text(
                            text = "Arriving in ${order.etaMinutes} minutes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // OTP Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "START OTP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = order.startOtp,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberPrimary,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assigned Driver Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(LogisticsBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RK",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = order.driverName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${order.driverRating} (156 trips)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDark,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${order.vehicleName} • ${order.driverVehicleNumber}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Call & Chat buttons
                        Row {
                            Surface(
                                shape = CircleShape,
                                color = LogisticsBlueContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(onClick = { showCallDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Driver",
                                        tint = LogisticsBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = AmberContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                IconButton(onClick = { showChatDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Chat with Driver",
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Route Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Trip Logistics",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Text(
                            text = order.pickupAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(AmberPrimary, CircleShape)
                        )
                        Text(
                            text = order.dropoffAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "DISTANCE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = "${order.distanceKm} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                        }
                        Column {
                            Text(text = "GOODS TYPE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = order.goodsType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Column {
                            Text(text = "PAYMENT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = order.paymentMethod.split(" ")[0], fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TOTAL FARE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = "₹${order.fare.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AmberPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = {
                    val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(order.dropoffAddress)
                    NavigationUtils.launchMapsNavigation(
                        context = context,
                        destinationLatitude = destLat,
                        destinationLongitude = destLng,
                        destinationLabel = order.dropoffAddress
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("active_ride_navigate_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navigate to Destination",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate (Launch Default Maps)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showPodDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_delivered_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deliver Goods • Capture Proof (POD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showCancelConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("cancel_ride_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Text("Cancel Ride Booking", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // SOS Safety Dialog
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Safety",
                    tint = ErrorRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Emergency SOS Safety", fontWeight = FontWeight.Bold) },
            text = {
                Text("Instant 24x7 emergency response dispatched to Indiranagar sector. Police (112), Ambulance (108), and Akhil Logistics field support alerted with driver ${order.driverVehicleNumber}.")
            },
            confirmButton = {
                Button(
                    onClick = { showSosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Call Police (112)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Call Dialog
    if (showCallDialog) {
        AlertDialog(
            onDismissRequest = { showCallDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = LogisticsBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Call Driver Partner", fontWeight = FontWeight.Bold) },
            text = {
                Text("Calling Ravi Kumar (${order.driverPhone}) via encrypted platform masking.")
            },
            confirmButton = {
                Button(
                    onClick = { showCallDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Text("Dial Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // In-App Chat Dialog
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                keyboardController?.hide()
                showChatDialog = false
            },
            title = { Text("Chat with Ravi Kumar", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    chatMessages.forEach { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (msg.startsWith("Driver")) SurfaceTertiary else AmberContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = TextDark,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newChatMessage,
                        onValueChange = { newChatMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type message...", fontSize = 12.sp) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChatMessage.isNotBlank()) {
                            chatMessages.add("Customer: $newChatMessage")
                            newChatMessage = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showChatDialog = false
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Cancel Confirm Dialog
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Cancel This Ride?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to cancel? No cancellation fee applies within 5 minutes.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirm = false
                        onCancelRide(order.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Keep Ride")
                }
            }
        )
    }

    // Proof of Delivery Dialog
    if (showPodDialog) {
        ProofOfDeliveryDialog(
            order = order,
            onDismiss = { showPodDialog = false },
            onConfirmDelivery = { _, _ ->
                showPodDialog = false
                onCompleteRide(order.id)
            }
        )
    }
}
