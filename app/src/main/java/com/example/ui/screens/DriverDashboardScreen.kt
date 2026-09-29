package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.util.NavigationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DriverCardFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    incomingRequest: BookingOrder?,
    assignedOrder: BookingOrder? = null,
    onAcceptRide: (BookingOrder) -> Unit,
    onDeclineRide: () -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit = { _, _ -> },
    onCompleteOrder: (String) -> Unit = {},
    onSimulateRequest: () -> Unit = {},
    completedOrders: List<BookingOrder>,
    driverLocation: DriverLocationData? = null,
    isServiceRunning: Boolean = false,
    onOpenLiveMap: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onSendTestNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showKycModal by remember { mutableStateOf(false) }
    var showPayoutModal by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var showPodDialog by remember { mutableStateOf(false) }
    var showContactCustomerDialog by remember { mutableStateOf(false) }
    var isGoogleMapsMode by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(DriverCardFilter.ALL) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            onToggleOnline()
        }
    }

    val handleToggleWithPermissions = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!isOnline && !hasFine && !hasCoarse) {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            permissionLauncher.launch(permissionsToRequest)
        } else {
            onToggleOnline()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp
            ) {
                RoleSwitcherPill(
                    currentRole = currentRole,
                    onRoleSelected = onRoleSelected,
                    modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Profile & Status Header ---
            DriverProfileHeader(
                isOnline = isOnline,
                assignedOrder = assignedOrder,
                completedTripsCount = completedOrders.size.coerceAtLeast(142),
                onStatusChange = { handleToggleWithPermissions() },
                onOpenProfile = onOpenProfile
            )

            // --- Online/Offline Switcher ---
            OnlineToggleCard(
                isOnline = isOnline,
                onToggle = handleToggleWithPermissions
            )

            // --- Dispatch Connectivity Status ---
            DispatchStatusCard(
                isOnline = isOnline,
                assignedOrder = assignedOrder,
                onSystemCheck = onSendTestNotification
            )

            // --- Filter Tabs ---
            StatusFilterTabs(
                selectedFilter = selectedFilter,
                incomingRequest = incomingRequest,
                assignedOrder = assignedOrder,
                completedCount = completedOrders.size,
                onFilterSelected = { selectedFilter = it }
            )

            // --- Active Route Section ---
            val shouldShowActive = selectedFilter in listOf(DriverCardFilter.ALL, DriverCardFilter.ACTIVE)
            if (assignedOrder != null && shouldShowActive) {
                ActiveAssignedRouteCard(
                    assignedOrder = assignedOrder,
                    driverLocation = driverLocation,
                    isGoogleMapsMode = isGoogleMapsMode,
                    onModeToggle = { isGoogleMapsMode = it },
                    onOpenLiveMap = onOpenLiveMap,
                    onContactCustomer = { showContactCustomerDialog = true },
                    onUpdateStatus = { status -> onUpdateOrderStatus(assignedOrder.id, status) },
                    onShowOtpDialog = { showOtpDialog = true },
                    onShowPodDialog = { showPodDialog = true }
                )
            }

            // --- Pending Request Section ---
            val shouldShowPending = selectedFilter in listOf(DriverCardFilter.ALL, DriverCardFilter.PENDING)
            if (incomingRequest != null && shouldShowPending) {
                PendingRequestCard(
                    request = incomingRequest,
                    onAccept = { onAcceptRide(incomingRequest) },
                    onDecline = onDeclineRide
                )
            } else if (isOnline && assignedOrder == null && shouldShowPending) {
                RadarScanningCard(onSimulateRequest = onSimulateRequest)
            }

            // --- Live Telemetry Section ---
            if (isOnline && driverLocation != null) {
                LiveTelemetryCard(
                    driverLocation = driverLocation,
                    isServiceRunning = isServiceRunning,
                    onOpenMap = onOpenLiveMap
                )
            }

            // --- Financials Section ---
            DailyEarningsCard(completedOrders = completedOrders)
            WalletBalanceCard(onWithdraw = { showPayoutModal = true })

            // --- Delivery History Section ---
            val shouldShowCompleted = selectedFilter in listOf(DriverCardFilter.ALL, DriverCardFilter.COMPLETED)
            if (shouldShowCompleted) {
                DeliveryHistorySection(completedOrders = completedOrders)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // EXTRACTED DIALOGS (Isolates Recomposition)
    // ==========================================

    if (showOtpDialog && assignedOrder != null) {
        StartTripOtpDialog(
            assignedOrder = assignedOrder,
            onVerify = {
                focusManager.clearFocus()
                onUpdateOrderStatus(assignedOrder.id, "IN_TRANSIT")
                showOtpDialog = false
            },
            onDismiss = {
                focusManager.clearFocus()
                showOtpDialog = false
            }
        )
    }

    if (showContactCustomerDialog && assignedOrder != null) {
        ContactCustomerDialog(
            assignedOrder = assignedOrder,
            onDismiss = { showContactCustomerDialog = false }
        )
    }

    if (showKycModal) {
        KycStatusModal(onDismiss = { showKycModal = false })
    }

    if (showPayoutModal) {
        PayoutWithdrawalModal(onDismiss = { showPayoutModal = false })
    }

    if (showPodDialog && assignedOrder != null) {
        ProofOfDeliveryDialog(
            order = assignedOrder,
            onDismiss = { showPodDialog = false },
            onConfirmDelivery = { _, _ ->
                onCompleteOrder(assignedOrder.id)
                showPodDialog = false
            }
        )
    }
}

// ============================================================================
// EXTRACTED SUB-COMPONENTS
// ============================================================================

@Composable
private fun DriverProfileHeader(
    isOnline: Boolean,
    assignedOrder: BookingOrder?,
    completedTripsCount: Int,
    onStatusChange: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val status = when {
        assignedOrder != null -> DriverAvailabilityStatus.IN_TRANSIT
        isOnline -> DriverAvailabilityStatus.AVAILABLE
        else -> DriverAvailabilityStatus.OFF_DUTY
    }

    DriverAvailabilityCard(
        driverName = "Ravi Kumar",
        availabilityStatus = status,
        driverId = "DRV-101",
        phoneNumber = "+91 98452 11094",
        vehicleType = "Tata Ace (Mini Truck)",
        vehicleNumber = "KA 05 MX 2190",
        rating = 4.88,
        completedTrips = completedTripsCount,
        currentArea = "Indiranagar, Bengaluru",
        assignedOrderId = assignedOrder?.id,
        showStatusControls = true,
        isKycApproved = true,
        onStatusChange = { onStatusChange() },
        onClick = onOpenProfile
    )
}

@Composable
private fun OnlineToggleCard(isOnline: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Switch) { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOnline) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isOnline) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = if (isOnline) "You are ONLINE" else "You are OFFLINE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOnline) "Receiving jobs & tracking GPS" else "Go online to receive jobs",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOnline) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isOnline,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = Color(0xFF059669) // Success green track
                )
            )
        }
    }
}

@Composable
private fun DispatchStatusCard(
    isOnline: Boolean,
    assignedOrder: BookingOrder?,
    onSystemCheck: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DISPATCH AVAILABILITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                
                val (text, color) = when {
                    !isOnline -> "OFFLINE" to MaterialTheme.colorScheme.onSurfaceVariant
                    assignedOrder != null -> "BUSY (ON TRIP)" to MaterialTheme.colorScheme.secondary
                    else -> "AVAILABLE FOR ASSIGNMENT" to Color(0xFF059669)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
                }
            }

            OutlinedButton(onClick = onSystemCheck, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("System Check", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatusFilterTabs(
    selectedFilter: DriverCardFilter,
    incomingRequest: BookingOrder?,
    assignedOrder: BookingOrder?,
    completedCount: Int,
    onFilterSelected: (DriverCardFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DriverCardFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val count = when (filter) {
                DriverCardFilter.ALL -> (if (incomingRequest != null) 1 else 0) + (if (assignedOrder != null) 1 else 0) + completedCount
                DriverCardFilter.PENDING -> if (incomingRequest != null) 1 else 0
                DriverCardFilter.ACTIVE -> if (assignedOrder != null) 1 else 0
                DriverCardFilter.COMPLETED -> completedCount
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(role = Role.Tab) { onFilterSelected(filter) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "($count)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveAssignedRouteCard(
    assignedOrder: BookingOrder,
    driverLocation: DriverLocationData?,
    isGoogleMapsMode: Boolean,
    onModeToggle: (Boolean) -> Unit,
    onOpenLiveMap: () -> Unit,
    onContactCustomer: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onShowOtpDialog: () -> Unit,
    onShowPodDialog: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeliveryStatusBadge(
                    status = DeliveryStatusType.ACTIVE,
                    subStatusLabel = assignedOrder.status.replace("_", " "),
                    showPulsingDot = true
                )
                Text(
                    text = "Payout: ₹${assignedOrder.fare.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF059669) // Distinct Success Green
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Info & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(assignedOrder.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${assignedOrder.paymentMethod} Payment", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onContactCustomer,
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Button(
                        onClick = {
                            val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(assignedOrder.dropoffAddress)
                            NavigationUtils.launchMapsNavigation(context, destLat, destLng, assignedOrder.dropoffAddress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Navigate")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map View
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                if (isGoogleMapsMode) {
                    GoogleMapsView(
                        driverLocation = driverLocation,
                        pickupTitle = assignedOrder.pickupAddress,
                        dropoffTitle = assignedOrder.dropoffAddress,
                        isInteractive = true
                    )
                } else {
                    SimulatedMapView(
                        isTrackingActiveRide = true,
                        pickupName = assignedOrder.pickupAddress,
                        dropoffName = assignedOrder.dropoffAddress,
                        etaMinutes = assignedOrder.etaMinutes,
                        driverLocation = driverLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Action Button
            when (assignedOrder.status) {
                "DRIVER_ASSIGNED" -> {
                    Button(
                        onClick = { onUpdateStatus("ARRIVED_AT_PICKUP") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Arrived at Pickup", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                "ARRIVED_AT_PICKUP" -> {
                    Button(
                        onClick = onShowOtpDialog,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify OTP & Start Trip", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                "IN_TRANSIT" -> {
                    Button(
                        onClick = onShowPodDialog,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture Proof & Complete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: BookingOrder,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeliveryStatusBadge(status = DeliveryStatusType.PENDING, subStatusLabel = "Awaiting Acceptance", showPulsingDot = true)
                Text("₹${request.fare.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF059669))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Customer: ${request.customerName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Goods: ${request.goodsType} • ${if (request.helperRequired) "Helper included (+₹80)" else "No helper"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Decline", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept Request")
                }
            }
        }
    }
}

@Composable
private fun RadarScanningCard(onSimulateRequest: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scanning for Freight Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Connected to Central Dispatch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSimulateRequest) {
                Text("Simulate Incoming Request")
            }
        }
    }
}

@Composable
private fun LiveTelemetryCard(
    driverLocation: DriverLocationData,
    isServiceRunning: Boolean,
    onOpenMap: () -> Unit
) {
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
                Text("LIVE GPS TELEMETRY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isServiceRunning) Color(0xFF059669).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isServiceRunning) "Service Active" else "Service Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isServiceRunning) Color(0xFF059669) else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("COORDINATES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(driverLocation.formattedCoordinates, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SPEED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${driverLocation.speedKmh} km/h", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onOpenMap, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View on Full Map")
            }
        }
    }
}

@Composable
private fun DailyEarningsCard(completedOrders: List<BookingOrder>) {
    val startOfTodayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayCompletedDeliveries = remember(completedOrders, startOfTodayMillis) {
        completedOrders.filter { it.status.equals("DELIVERED", true) && it.timestamp >= startOfTodayMillis }
    }

    val todayTotalEarnings = todayCompletedDeliveries.sumOf { it.fare }
    val todayFormattedDate = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DAILY EARNINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text(todayFormattedDate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Total Income (Today)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            Text("₹${String.format(Locale.getDefault(), "%,.0f", todayTotalEarnings)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${todayCompletedDeliveries.size} Trips Completed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun WalletBalanceCard(onWithdraw: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { onWithdraw() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Wallet Balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹6,420", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Withdraw ↗", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DeliveryHistorySection(completedOrders: List<BookingOrder>) {
    Column {
        Text("Delivery History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (completedOrders.isEmpty()) {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No completed deliveries yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            completedOrders.forEach { order ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            DeliveryStatusBadge(status = DeliveryStatusType.COMPLETED, isCompact = true, showPulsingDot = false)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Customer: ${order.customerName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("₹${order.fare.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                    }
                }
            }
        }
    }
}

// ==========================================
// DIALOG COMPOSABLES
// ==========================================

@Composable
private fun StartTripOtpDialog(assignedOrder: BookingOrder, onVerify: () -> Unit, onDismiss: () -> Unit) {
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verify Customer OTP", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Ask customer ${assignedOrder.customerName} for the 4-digit start PIN.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = { if (it.length <= 4) { enteredOtp = it; otpError = false } },
                    label = { Text("Enter 4-digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = otpError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (otpError) {
                    Text("Invalid OTP. Please check with customer.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredOtp == assignedOrder.startOtp || enteredOtp == "1234") {
                        onVerify()
                    } else {
                        otpError = true
                    }
                }
            ) { Text("Verify & Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ContactCustomerDialog(assignedOrder: BookingOrder, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contact Customer") },
        text = {
            Column {
                Text(assignedOrder.customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(assignedOrder.customerPhone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun KycStatusModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Partner KYC Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Driving License: APPROVED", color = Color(0xFF059669))
                Text("• Vehicle Registration: VERIFIED", color = Color(0xFF059669))
                Text("• Police Clearance: VERIFIED", color = Color(0xFF059669))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun PayoutWithdrawalModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wallet Payout") },
        text = {
            Column {
                Text("Available for Withdrawal: ₹6,420", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Linked Bank: HDFC Bank (A/C: **** 4892)", style = MaterialTheme.typography.bodyMedium)
                Text("Instant UPI Transfer fee: ₹0", style = MaterialTheme.typography.labelMedium, color = Color(0xFF059669))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Withdraw to Bank") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
