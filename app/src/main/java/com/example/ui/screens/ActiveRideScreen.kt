package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.ProofOfDeliveryDialog
import com.example.ui.components.SimulatedMapView
import com.example.util.NavigationUtils

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // UI State
    var isGoogleMapsMode by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var showPodDialog by remember { mutableStateOf(false) }

    // Scaffold provides native edge-to-edge layout and inset management
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text("Live Ride Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showSosDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SOS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- Map Controls ---
            MapControlsHeader(
                isGoogleMapsMode = isGoogleMapsMode,
                onModeToggle = { isGoogleMapsMode = it },
                onOpenLiveMap = onOpenLiveMap
            )

            // --- Map Widget ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (isGoogleMapsMode) {
                    GoogleMapsView(
                        driverLocation = driverLocation,
                        pickupLatLng = com.google.android.gms.maps.model.LatLng(order.pickupLat, order.pickupLng),
                        dropoffLatLng = com.google.android.gms.maps.model.LatLng(order.dropoffLat, order.dropoffLng),
                        pickupTitle = order.pickupAddress,
                        dropoffTitle = order.dropoffAddress,
                        pickupPlaceId = order.pickupPlaceId,
                        dropoffPlaceId = order.dropoffPlaceId,
                        vehicleType = order.vehicleName, // Or vehicleTierId
                        isInteractive = true
                    )
                } else {
                    SimulatedMapView(
                        isTrackingActiveRide = true,
                        pickupName = order.pickupAddress,
                        dropoffName = order.dropoffAddress,
                        etaMinutes = order.etaMinutes,
                        driverLocation = driverLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Live Status & OTP Banner ---
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                            Text(
                                text = "DRIVER EN ROUTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = "Arriving in ${order.etaMinutes} mins",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // OTP Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("START OTP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.startOtp, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Driver Info Card ---
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = order.driverName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(order.driverName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${order.driverRating} Rating",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Text("${order.vehicleName} • ${order.driverVehicleNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Contact Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showCallDialog = true },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        IconButton(
                            onClick = { showChatDialog = true },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Trip Logistics Details ---
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trip Logistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    LocationRow(address = order.pickupAddress, isPickup = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    LocationRow(address = order.dropoffAddress, isPickup = false)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LogisticsMetric(label = "DISTANCE", value = "${order.distanceKm} km")
                        LogisticsMetric(label = "GOODS TYPE", value = order.goodsType)
                        LogisticsMetric(label = "PAYMENT", value = order.paymentMethod.split(" ")[0])
                        LogisticsMetric(label = "FARE", value = "₹${order.fare.toInt()}", isHighlight = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Primary Actions ---
            Button(
                onClick = {
                    val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(order.dropoffAddress)
                    NavigationUtils.launchMapsNavigation(context, destLat, destLng, order.dropoffAddress)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Maps Navigation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showPodDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF059669) // Specific Success Green
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Delivery (POD)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showCancelConfirm = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel Ride Booking", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }

    // ============================================================================
    // Extracted Dialogs (Isolates recomposition)
    // ============================================================================

    if (showSosDialog) {
        SosEmergencyDialog(
            driverVehicleInfo = order.driverVehicleNumber,
            onDismiss = { showSosDialog = false }
        )
    }

    if (showCallDialog) {
        CallDriverDialog(
            driverName = order.driverName,
            driverPhone = order.driverPhone,
            onDismiss = { showCallDialog = false }
        )
    }

    if (showChatDialog) {
        ChatWithDriverDialog(
            driverName = order.driverName,
            onDismiss = { showChatDialog = false }
        )
    }

    if (showCancelConfirm) {
        CancelRideDialog(
            onConfirm = { 
                showCancelConfirm = false
                onCancelRide(order.id) 
            },
            onDismiss = { showCancelConfirm = false }
        )
    }

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

// ============================================================================
// Sub-Components
// ============================================================================

@Composable
private fun MapControlsHeader(
    isGoogleMapsMode: Boolean,
    onModeToggle: (Boolean) -> Unit,
    onOpenLiveMap: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Toggle
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isGoogleMapsMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.clickable(role = Role.Tab) { onModeToggle(true) }
                ) {
                    Text(
                        "SDK Map", 
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGoogleMapsMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (!isGoogleMapsMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.clickable(role = Role.Tab) { onModeToggle(false) }
                ) {
                    Text(
                        "Vector Map", 
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!isGoogleMapsMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Expand Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.clickable(role = Role.Button) { onOpenLiveMap() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expand", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LocationRow(address: String, isPickup: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (isPickup) Color.Green else MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LogisticsMetric(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = if (isHighlight) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value, 
            style = MaterialTheme.typography.titleSmall, 
            fontWeight = FontWeight.Bold, 
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================================
// Dialog Composables
// ============================================================================

@Composable
private fun SosEmergencyDialog(driverVehicleInfo: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Emergency SOS Safety") },
        text = { Text("Instant 24x7 emergency response dispatched to your location. Police and field support alerted with driver info ($driverVehicleInfo).") },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Call Police (112)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun CallDriverDialog(driverName: String, driverPhone: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Call, contentDescription = null) },
        title = { Text("Call Driver Partner") },
        text = { Text("Calling $driverName ($driverPhone) via encrypted platform masking.") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Dial Now") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CancelRideDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel This Ride?") },
        text = { Text("Are you sure you want to cancel? No cancellation fee applies within 5 minutes.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Yes, Cancel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Ride") }
        }
    )
}

@Composable
private fun ChatWithDriverDialog(driverName: String, onDismiss: () -> Unit) {
    var newChatMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val messages = remember { mutableStateListOf("Driver: Hello! I am 4 minutes away.", "Customer: Great, ready here.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat with $driverName") },
        text = {
            Column {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(messages) { msg ->
                        val isDriver = msg.startsWith("Driver")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = if (isDriver) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDriver) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    color = if (isDriver) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newChatMessage,
                    onValueChange = { newChatMessage = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type message...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (newChatMessage.isNotBlank()) {
                                messages.add("Customer: $newChatMessage")
                                newChatMessage = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newChatMessage.isNotBlank()) {
                        messages.add("Customer: $newChatMessage")
                        newChatMessage = ""
                    }
                }
            ) { Text("Send") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
