package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.BookingOrder
import com.example.data.model.DriverLocationData
import com.example.data.model.UserRole
import com.example.ui.components.DeliveryStatusBadge
import com.example.ui.components.DeliveryStatusType
import com.example.ui.components.DriverAvailabilityCard
import com.example.ui.components.DriverAvailabilityStatus
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.ProofOfDeliveryDialog
import com.example.ui.components.RoleSwitcherPill
import com.example.ui.components.SimulatedMapView
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
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
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }
    var isGoogleMapsMode by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(DriverCardFilter.ALL) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isOnline && !hasFine && !hasCoarse) {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
            permissionLauncher.launch(permissionsToRequest)
        } else {
            onToggleOnline()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Segmented Role Switcher at top
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
            // Driver Information & Availability Status Card
            val currentDriverStatus = when {
                assignedOrder != null -> DriverAvailabilityStatus.IN_TRANSIT
                isOnline -> DriverAvailabilityStatus.AVAILABLE
                else -> DriverAvailabilityStatus.OFF_DUTY
            }

            DriverAvailabilityCard(
                driverName = "Ravi Kumar",
                availabilityStatus = currentDriverStatus,
                driverId = "DRV-101",
                phoneNumber = "+91 98452 11094",
                vehicleType = "Tata Ace (Mini Truck)",
                vehicleNumber = "KA 05 MX 2190",
                rating = 4.88,
                completedTrips = completedOrders.size.coerceAtLeast(142),
                currentArea = "Indiranagar, Bengaluru",
                assignedOrderId = assignedOrder?.id,
                assignedOrderRoute = assignedOrder?.let { "${it.pickupAddress.split(",")[0]} → ${it.dropoffAddress.split(",")[0]}" },
                assignedOrderGoodsType = assignedOrder?.goodsType,
                assignedOrderFare = assignedOrder?.fare,
                showStatusControls = true,
                showContactActions = false,
                isKycApproved = true,
                onStatusChange = { newStatus ->
                    when (newStatus) {
                        DriverAvailabilityStatus.AVAILABLE, DriverAvailabilityStatus.IN_TRANSIT -> {
                            if (!isOnline) handleToggleWithPermissions()
                        }
                        DriverAvailabilityStatus.OFF_DUTY -> {
                            if (isOnline) handleToggleWithPermissions()
                        }
                    }
                },
                onClick = onOpenProfile,
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .testTag("driver_profile_header_card")
            )

            // Online / Offline Switcher Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { handleToggleWithPermissions() }
                    .testTag("driver_online_toggle_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) AmberPrimary else SurfaceCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isOnline) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isOnline) Color.White.copy(alpha = 0.2f) else SurfaceTertiary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Online Status",
                                tint = if (isOnline) Color.White else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = if (isOnline) "You are ONLINE" else "You are OFFLINE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color.White else TextDark
                            )
                            Text(
                                text = if (isOnline) "Live GPS telemetry active & ready for jobs" else "Toggle switch to go online and receive jobs",
                                fontSize = 11.sp,
                                color = if (isOnline) Color.White.copy(alpha = 0.85f) else TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = { handleToggleWithPermissions() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SuccessGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceTertiary
                        ),
                        modifier = Modifier.testTag("driver_status_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dispatch Availability Status & Test Alert Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_availability_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DISPATCH AVAILABILITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = AmberPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            val availabilityText = if (!isOnline) "OFFLINE" else if (assignedOrder != null && assignedOrder.status != "DELIVERED") "BUSY (ON TRIP)" else "AVAILABLE FOR ASSIGNMENT"
                            val availabilityColor = if (!isOnline) TextMuted else if (assignedOrder != null && assignedOrder.status != "DELIVERED") LogisticsBlue else SuccessGreen
                            Box(modifier = Modifier.size(8.dp).background(availabilityColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = availabilityText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = availabilityColor
                            )
                        }
                        if (assignedOrder != null && assignedOrder.status != "DELIVERED") {
                            Text(
                                text = "Mapped Order: #${assignedOrder.id} • ${assignedOrder.goodsType}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSendTestNotification,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("driver_test_alert_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Test Notification",
                            tint = AmberPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // STATUS FILTER TABS & LEGEND
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_status_filter_tabs"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DriverCardFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        DriverCardFilter.ALL -> (if (incomingRequest != null) 1 else 0) + (if (assignedOrder != null) 1 else 0) + completedOrders.size
                        DriverCardFilter.PENDING -> if (incomingRequest != null) 1 else 0
                        DriverCardFilter.ACTIVE -> if (assignedOrder != null) 1 else 0
                        DriverCardFilter.COMPLETED -> completedOrders.size
                    }

                    val badgeColor = when (filter) {
                        DriverCardFilter.ALL -> TextDark
                        DriverCardFilter.PENDING -> Color(0xFFE65100)
                        DriverCardFilter.ACTIVE -> LogisticsBlue
                        DriverCardFilter.COMPLETED -> SuccessGreen
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = filter }
                            .testTag("filter_tab_${filter.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            when (filter) {
                                DriverCardFilter.ALL -> AmberPrimary
                                DriverCardFilter.PENDING -> Color(0xFFFFF3E0)
                                DriverCardFilter.ACTIVE -> Color(0xFFE3F2FD)
                                DriverCardFilter.COMPLETED -> Color(0xFFE8F5E9)
                            }
                        } else {
                            SurfaceCard
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) badgeColor else BorderLight
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected && filter == DriverCardFilter.ALL) Color.White else badgeColor
                            )
                            Text(
                                text = "($count)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected && filter == DriverCardFilter.ALL) Color.White.copy(alpha = 0.85f) else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // SECTION 1: CURRENT ASSIGNED ROUTE (ACTIVE)
            // ==========================================
            val shouldShowActive = selectedFilter == DriverCardFilter.ALL || selectedFilter == DriverCardFilter.ACTIVE

            if (assignedOrder != null && shouldShowActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_assigned_route_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, LogisticsBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header Status Indicator & Trip Payout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // VISUAL STATUS INDICATOR: ACTIVE
                            DeliveryStatusBadge(
                                status = DeliveryStatusType.ACTIVE,
                                subStatusLabel = assignedOrder.status.replace("_", " "),
                                showPulsingDot = true,
                                modifier = Modifier.testTag("active_order_status_badge")
                            )

                            Text(
                                text = "Trip Payout: ₹${assignedOrder.fare.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer Details & Quick Contact
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = assignedOrder.customerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Start OTP: ${assignedOrder.startOtp} • ${assignedOrder.paymentMethod}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmberPrimary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = LogisticsBlueContainer,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { showContactCustomerDialog = true }
                                        .testTag("driver_call_customer_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call Customer",
                                            tint = LogisticsBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = AmberContainer,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { showContactCustomerDialog = true }
                                        .testTag("driver_chat_customer_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Message Customer",
                                            tint = AmberPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = LogisticsBlue,
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clickable {
                                            val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(assignedOrder.dropoffAddress)
                                            NavigationUtils.launchMapsNavigation(
                                                context = context,
                                                destinationLatitude = destLat,
                                                destinationLongitude = destLng,
                                                destinationLabel = assignedOrder.dropoffAddress
                                            )
                                        }
                                        .testTag("driver_navigate_quick_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = "Navigate",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Navigate",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Route Addresses Summary with Coordinates & Navigation Chip
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceTertiary, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                                Text(
                                    text = "Pickup: ${assignedOrder.pickupAddress}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(AmberPrimary, CircleShape))
                                Text(
                                    text = "Drop-off: ${assignedOrder.dropoffAddress}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(assignedOrder.dropoffAddress)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Destination GPS: ${String.format("%.4f", destLat)}° N, ${String.format("%.4f", destLng)}° E",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Launch Maps ↗",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LogisticsBlue,
                                    modifier = Modifier
                                        .clickable {
                                            NavigationUtils.launchMapsNavigation(
                                                context = context,
                                                destinationLatitude = destLat,
                                                destinationLongitude = destLng,
                                                destinationLabel = assignedOrder.dropoffAddress
                                            )
                                        }
                                        .testTag("driver_navigate_coords_link")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Map Controls Header (Switcher + Full Map button)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceTertiary, RoundedCornerShape(16.dp))
                                    .padding(2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isGoogleMapsMode) AmberPrimary else Color.Transparent,
                                    modifier = Modifier.clickable { isGoogleMapsMode = true }
                                ) {
                                    Text(
                                        text = "Google Maps SDK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGoogleMapsMode) Color.White else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (!isGoogleMapsMode) AmberPrimary else Color.Transparent,
                                    modifier = Modifier.clickable { isGoogleMapsMode = false }
                                ) {
                                    Text(
                                        text = "Vector Map",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isGoogleMapsMode) Color.White else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.clickable { onOpenLiveMap() }.testTag("driver_expand_map_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen Map",
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Full Map",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberPrimary,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Embedded Interactive Route Map
                        if (isGoogleMapsMode) {
                            GoogleMapsView(
                                driverLocation = driverLocation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                pickupTitle = assignedOrder.pickupAddress,
                                dropoffTitle = assignedOrder.dropoffAddress,
                                isInteractive = true
                            )
                        } else {
                            SimulatedMapView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                isTrackingActiveRide = true,
                                pickupName = assignedOrder.pickupAddress,
                                dropoffName = assignedOrder.dropoffAddress,
                                etaMinutes = assignedOrder.etaMinutes,
                                driverLocation = driverLocation
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary 'Navigate' Button for Active Delivery Request
                        Button(
                            onClick = {
                                val (destLat, destLng) = NavigationUtils.getDestinationCoordinates(assignedOrder.dropoffAddress)
                                NavigationUtils.launchMapsNavigation(
                                    context = context,
                                    destinationLatitude = destLat,
                                    destinationLongitude = destLng,
                                    destinationLabel = assignedOrder.dropoffAddress
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("driver_navigate_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Navigate to Destination",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Navigate (Launch Default Maps)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic Milestone Progress Action Buttons
                        when (assignedOrder.status) {
                            "DRIVER_ASSIGNED" -> {
                                Button(
                                    onClick = {
                                        onUpdateOrderStatus(assignedOrder.id, "ARRIVED_AT_PICKUP")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("driver_arrived_pickup_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mark Arrived at Pickup Point",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            "ARRIVED_AT_PICKUP" -> {
                                Button(
                                    onClick = { showOtpDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("driver_start_trip_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Verify Customer OTP & Start Trip",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            "IN_TRANSIT" -> {
                                Button(
                                    onClick = { showPodDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("driver_complete_delivery_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Arrived at Destination • Capture Proof & Complete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // SECTION 2: INCOMING DELIVERY REQUESTS (PENDING)
            // ==========================================
            val shouldShowPending = selectedFilter == DriverCardFilter.ALL || selectedFilter == DriverCardFilter.PENDING

            if (incomingRequest != null && shouldShowPending) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incoming_request_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB74D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // VISUAL STATUS INDICATOR: PENDING
                            DeliveryStatusBadge(
                                status = DeliveryStatusType.PENDING,
                                subStatusLabel = "Awaiting Acceptance",
                                showPulsingDot = true,
                                modifier = Modifier.testTag("pending_request_status_badge")
                            )

                            Text(
                                text = "₹${incomingRequest.fare.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer & Cargo Info
                        Text(
                            text = "Customer: ${incomingRequest.customerName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Goods: ${incomingRequest.goodsType} • ${if (incomingRequest.helperRequired) "Helper included (+₹80)" else "No helper"}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Route details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceTertiary, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                                Text(
                                    text = "Pickup: ${incomingRequest.pickupAddress}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(AmberPrimary, CircleShape))
                                Text(
                                    text = "Drop-off: ${incomingRequest.dropoffAddress}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Accept / Decline CTAs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDeclineRide,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("decline_ride_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Decline", fontWeight = FontWeight.Bold, color = TextMuted)
                            }

                            Button(
                                onClick = { onAcceptRide(incomingRequest) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("accept_ride_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept Request", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else if (isOnline && assignedOrder == null && shouldShowPending) {
                // Online radar card with simulate button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_radar_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(SuccessContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Radar",
                                tint = SuccessGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Scanning for Freight Requests",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Connected to Bangalore Central Dispatch • Indiranagar Hub",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onSimulateRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("simulate_incoming_request_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulate Incoming Delivery Request",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // SECTION 3: LIVE GPS TELEMETRY
            // ==========================================
            if (isOnline && driverLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_gps_telemetry_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Text(
                                    text = "LIVE GPS TELEMETRY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SuccessGreen,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessContainer
                            ) {
                                Text(
                                    text = if (isServiceRunning) "Foreground Service Active" else "Location Provider Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT COORDINATES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = driverLocation.formattedCoordinates,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TELEMETRY SPEED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${driverLocation.speedKmh} km/h",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Accuracy: ±${driverLocation.accuracyMeters}m • Alt: ${driverLocation.altitude.toInt()}m",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Updated: ${driverLocation.formattedTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onOpenLiveMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("driver_open_google_maps_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Real-Time on Google Maps SDK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // ==========================================
            // SECTION 4: DAILY EARNINGS & WALLET STATS
            // ==========================================
            // Calculate today's start-of-day epoch millis
            val startOfTodayMillis = remember {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            // Filter deliveries completed today
            val todayCompletedDeliveries = remember(completedOrders, startOfTodayMillis) {
                completedOrders.filter { order ->
                    val isDelivered = order.status.equals("DELIVERED", ignoreCase = true) ||
                                      order.status.equals("COMPLETED", ignoreCase = true)
                    isDelivered && order.timestamp >= startOfTodayMillis
                }
            }

            val todayTotalEarnings = remember(todayCompletedDeliveries) {
                todayCompletedDeliveries.sumOf { it.fare }
            }

            val todayTripsCount = todayCompletedDeliveries.size

            val todayFormattedDate = remember {
                SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
            }

            // Daily Earnings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_daily_earnings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AmberPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top row: Header badge & current date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Daily Earnings",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY EARNINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White.copy(alpha = 0.95f),
                                letterSpacing = 1.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = todayFormattedDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Middle: Total Income Amount & Calculation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Total Income (Today)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,.0f", todayTotalEarnings)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.testTag("driver_daily_total_earnings_amount")
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = if (todayTripsCount == 1) "1 Trip Completed" else "$todayTripsCount Trips Completed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("driver_daily_trips_completed_count")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom info strip: Calculation breakdown & incentive progress
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (todayTripsCount > 0) {
                                    "Avg. ₹${(todayTotalEarnings / todayTripsCount).toInt()} / completed delivery"
                                } else {
                                    "Complete trips today to build daily earnings"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Text(
                                text = "Payout Ready",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wallet Balance Quick Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPayoutModal = true }
                    .testTag("driver_wallet_balance_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = LogisticsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Driver Wallet Balance",
                            fontSize = 12.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹6,420",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Withdraw ↗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LogisticsBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // SECTION 5: DELIVERY HISTORY
            // ==========================================
            val shouldShowCompleted = selectedFilter == DriverCardFilter.ALL || selectedFilter == DriverCardFilter.COMPLETED

            if (shouldShowCompleted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_delivery_history_header"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History Icon",
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delivery History",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceTertiary
                    ) {
                        Text(
                            text = "${completedOrders.size} Completed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (completedOrders.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_delivery_history_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No completed deliveries found",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Your completed freight deliveries will be logged here with date & customer addresses.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

                    completedOrders.forEach { order ->
                        val formattedDate = try {
                            dateFormat.format(Date(order.timestamp))
                        } catch (e: Exception) {
                            "Recent Delivery"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("completed_delivery_card_${order.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top row: Status badge, Date & Time, and Earned Fare
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // VISUAL STATUS INDICATOR: COMPLETED
                                        DeliveryStatusBadge(
                                            status = DeliveryStatusType.COMPLETED,
                                            isCompact = true,
                                            showPulsingDot = false,
                                            modifier = Modifier.testTag("completed_status_badge_${order.id}")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = order.id,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                    }

                                    Text(
                                        text = "₹${order.fare.toInt()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SuccessGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Date & Timestamp Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Delivery Date",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = formattedDate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted,
                                        modifier = Modifier.testTag("delivery_date_${order.id}")
                                    )
                                }

                                // Customer & Cargo addresses box
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceTertiary, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    // Customer Name Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Customer",
                                            tint = LogisticsBlue,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Customer: ${order.customerName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark,
                                            modifier = Modifier.testTag("delivery_customer_${order.id}")
                                        )
                                    }

                                    // Pickup Customer Address
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .size(7.dp)
                                                .background(SuccessGreen, CircleShape)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "Pickup Address",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = order.pickupAddress,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextDark,
                                                modifier = Modifier.testTag("delivery_pickup_${order.id}")
                                            )
                                        }
                                    }

                                    // Dropoff Customer Address
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .size(7.dp)
                                                .background(AmberPrimary, CircleShape)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "Drop-off Address",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = order.dropoffAddress,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextDark,
                                                modifier = Modifier.testTag("delivery_dropoff_${order.id}")
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Footer: Distance, Goods, Payment method
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${order.goodsType} • ${order.distanceKm} km",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Paid via ${order.paymentMethod}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LogisticsBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // ==========================================
    // DIALOGS & MODALS
    // ==========================================

    // Start Trip OTP Dialog
    if (showOtpDialog && assignedOrder != null) {
        AlertDialog(
            onDismissRequest = {
                focusManager.clearFocus()
                keyboardController?.hide()
                showOtpDialog = false
                otpError = false
            },
            title = {
                Text("Verify Customer OTP", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Ask customer ${assignedOrder.customerName} for the 4-digit start PIN.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(Demo Hint: Customer OTP is ${assignedOrder.startOtp})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 4) enteredOtp = it
                            otpError = false
                        },
                        label = { Text("Enter 4-digit OTP") },
                        isError = otpError,
                        modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
                        singleLine = true
                    )
                    if (otpError) {
                        Text(
                            text = "Invalid OTP. Please check with customer.",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredOtp == assignedOrder.startOtp || enteredOtp == "1234") {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onUpdateOrderStatus(assignedOrder.id, "IN_TRANSIT")
                            showOtpDialog = false
                            enteredOtp = ""
                        } else {
                            otpError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    modifier = Modifier.testTag("submit_otp_button")
                ) {
                    Text("Verify & Start", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showOtpDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Customer Contact Dialog
    if (showContactCustomerDialog && assignedOrder != null) {
        AlertDialog(
            onDismissRequest = { showContactCustomerDialog = false },
            title = {
                Text("Contact Consignee / Sender", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Customer: ${assignedOrder.customerName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Phone: ${assignedOrder.customerPhone}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pickup: ${assignedOrder.pickupAddress}",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showContactCustomerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // KYC Verification Modal
    if (showKycModal) {
        AlertDialog(
            onDismissRequest = { showKycModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partner KYC Status", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("• Driving License (Commercial LMV): APPROVED", fontSize = 12.sp, color = SuccessGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Vehicle Registration (KA 05 MX 2190): VERIFIED", fontSize = 12.sp, color = SuccessGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Vehicle Fitness & Pollution: VALID till 2027", fontSize = 12.sp, color = SuccessGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Police Clearance Certificate: VERIFIED", fontSize = 12.sp, color = SuccessGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showKycModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Payout / Withdrawal Modal
    if (showPayoutModal) {
        AlertDialog(
            onDismissRequest = { showPayoutModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = LogisticsBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Driver Wallet Payout", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Available for Withdrawal: ₹6,420", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Linked Bank: HDFC Bank (A/C: **** 4892)", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Instant UPI Transfer fee: ₹0 (Free for Gold Partners)", fontSize = 11.sp, color = SuccessGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPayoutModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Withdraw to Bank", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Proof of Delivery (Photo or Signature) Dialog
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
