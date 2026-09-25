package com.example

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.screens.ActiveRideScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.DriverDashboardScreen
import com.example.ui.screens.DriverProfileScreen
import com.example.ui.screens.LiveMapScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LogisticsViewModel
import com.example.util.DeliveryNotificationHelper
import kotlinx.coroutines.delay

enum class CustomerScreenState {
    HOME,
    BOOKING,
    ACTIVE_RIDE,
    LIVE_MAP
}

class MainActivity : ComponentActivity() {

    private val viewModel: LogisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LogisticsApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val targetRole = intent?.getStringExtra("EXTRA_TARGET_ROLE")
        if (targetRole == "DRIVER") {
            viewModel.setRole(UserRole.DRIVER)
        }
    }
}

@Composable
fun LogisticsApp(viewModel: LogisticsViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
    val orderHistory by viewModel.allOrders.collectAsStateWithLifecycle()
    val drivers by viewModel.allDrivers.collectAsStateWithLifecycle()
    val isDriverOnline by viewModel.isDriverOnline.collectAsStateWithLifecycle()
    val driverLocation by viewModel.driverLocation.collectAsStateWithLifecycle()
    val isLocationServiceRunning by viewModel.isLocationServiceRunning.collectAsStateWithLifecycle()
    val incomingRequest by viewModel.driverIncomingRequest.collectAsStateWithLifecycle()
    val pricingMultiplier by viewModel.pricingMultiplier.collectAsStateWithLifecycle()
    val pendingOrders by viewModel.pendingOrders.collectAsStateWithLifecycle()
    val activeDeliveryAlert by viewModel.activeDeliveryAlert.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission response handled */ }

    LaunchedEffect(Unit) {
        DeliveryNotificationHelper.initNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val pickupAddress by viewModel.pickupAddress.collectAsStateWithLifecycle()
    val dropoffAddress by viewModel.dropoffAddress.collectAsStateWithLifecycle()
    val routeDistanceInfo by viewModel.routeDistanceInfo.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val selectedGoodsType by viewModel.selectedGoodsType.collectAsStateWithLifecycle()
    val isHelperRequired by viewModel.isHelperRequired.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    var customerScreenState by remember { mutableStateOf(CustomerScreenState.HOME) }
    var isDriverViewingLiveMap by remember { mutableStateOf(false) }
    var isDriverViewingProfile by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDriverViewingLiveMap) {
                LiveMapScreen(
                    driverLocation = driverLocation,
                    isServiceRunning = isLocationServiceRunning,
                    onBack = { isDriverViewingLiveMap = false }
                )
            } else if (isDriverViewingProfile) {
                DriverProfileScreen(
                    isOnline = isDriverOnline,
                    onToggleOnline = { viewModel.toggleDriverOnline(context) },
                    completedOrders = orderHistory,
                    onBack = { isDriverViewingProfile = false }
                )
            } else {
                AnimatedContent(
                    targetState = if (!isLoggedIn) "LOGIN" else currentRole.name,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "role_navigation"
                ) { target ->
                when (target) {
                    "LOGIN" -> {
                        LoginScreen(
                            onLoginSuccess = { phone, role ->
                                viewModel.login(phone, role)
                            }
                        )
                    }

                    UserRole.CUSTOMER.name -> {
                        when (customerScreenState) {
                            CustomerScreenState.HOME -> {
                                CustomerHomeScreen(
                                    activeOrder = activeOrder,
                                    orderHistory = orderHistory,
                                    currentRole = currentRole,
                                    pickupAddress = pickupAddress,
                                    dropoffAddress = dropoffAddress,
                                    routeDistanceInfo = routeDistanceInfo,
                                    onPickupChange = viewModel::setPickup,
                                    onDropoffChange = viewModel::setDropoff,
                                    onSetCustomDistance = viewModel::setCustomDistance,
                                    onResetDistance = viewModel::resetDistanceToAuto,
                                    onRoleSelected = { role ->
                                        viewModel.setRole(role)
                                    },
                                    onStartBooking = {
                                        customerScreenState = CustomerScreenState.BOOKING
                                    },
                                    onViewActiveRide = {
                                        if (activeOrder != null) {
                                            customerScreenState = CustomerScreenState.ACTIVE_RIDE
                                        }
                                    },
                                    driverLocation = driverLocation,
                                    onOpenLiveMap = {
                                        customerScreenState = CustomerScreenState.LIVE_MAP
                                    },
                                    onQuickBook = { p, d ->
                                        viewModel.setPickup(p)
                                        viewModel.setDropoff(d)
                                        customerScreenState = CustomerScreenState.BOOKING
                                    }
                                )
                            }

                            CustomerScreenState.BOOKING -> {
                                BookingFlowScreen(
                                    pickupAddress = pickupAddress,
                                    dropoffAddress = dropoffAddress,
                                    routeDistanceInfo = routeDistanceInfo,
                                    selectedVehicleId = selectedVehicleId,
                                    selectedGoodsType = selectedGoodsType,
                                    isHelperRequired = isHelperRequired,
                                    selectedPaymentMethod = selectedPaymentMethod,
                                    onPickupChange = viewModel::setPickup,
                                    onDropoffChange = viewModel::setDropoff,
                                    onSetCustomDistance = viewModel::setCustomDistance,
                                    onResetDistance = viewModel::resetDistanceToAuto,
                                    onVehicleSelect = viewModel::setVehicle,
                                    onGoodsSelect = viewModel::setGoodsType,
                                    onToggleHelper = viewModel::toggleHelper,
                                    onPaymentSelect = viewModel::setPaymentMethod,
                                    calculateFare = viewModel::calculateEstimatedFare,
                                    onConfirmBooking = {
                                        viewModel.bookRide {
                                            customerScreenState = CustomerScreenState.ACTIVE_RIDE
                                        }
                                    },
                                    onBack = {
                                        customerScreenState = CustomerScreenState.HOME
                                    }
                                )
                            }

                            CustomerScreenState.ACTIVE_RIDE -> {
                                if (activeOrder != null) {
                                    ActiveRideScreen(
                                        order = activeOrder!!,
                                        onBack = {
                                            customerScreenState = CustomerScreenState.HOME
                                        },
                                        onCancelRide = { id ->
                                            viewModel.cancelActiveRide(id)
                                            customerScreenState = CustomerScreenState.HOME
                                        },
                                        onCompleteRide = { id ->
                                            viewModel.completeActiveRide(id)
                                            customerScreenState = CustomerScreenState.HOME
                                        },
                                        driverLocation = driverLocation,
                                        onOpenLiveMap = {
                                            customerScreenState = CustomerScreenState.LIVE_MAP
                                        }
                                    )
                                } else {
                                    customerScreenState = CustomerScreenState.HOME
                                }
                            }

                            CustomerScreenState.LIVE_MAP -> {
                                LiveMapScreen(
                                    driverLocation = driverLocation,
                                    isServiceRunning = isLocationServiceRunning,
                                    onBack = {
                                        customerScreenState = CustomerScreenState.HOME
                                    }
                                )
                            }
                        }
                    }

                    UserRole.DRIVER.name -> {
                        DriverDashboardScreen(
                            currentRole = currentRole,
                            onRoleSelected = { role ->
                                viewModel.setRole(role)
                            },
                            isOnline = isDriverOnline,
                            onToggleOnline = {
                                viewModel.toggleDriverOnline(context)
                            },
                            incomingRequest = incomingRequest,
                            assignedOrder = activeOrder,
                            onAcceptRide = viewModel::acceptDriverRide,
                            onDeclineRide = viewModel::declineDriverRide,
                            onUpdateOrderStatus = viewModel::updateDriverDeliveryStatus,
                            onCompleteOrder = viewModel::completeDriverDelivery,
                            onSimulateRequest = viewModel::simulateIncomingRequest,
                            completedOrders = orderHistory,
                            driverLocation = driverLocation,
                            isServiceRunning = isLocationServiceRunning,
                            onOpenLiveMap = {
                                isDriverViewingLiveMap = true
                            },
                            onOpenProfile = {
                                isDriverViewingProfile = true
                            },
                            onSendTestNotification = {
                                viewModel.sendTestDriverNotification(context)
                            }
                        )
                    }

                    UserRole.ADMIN.name -> {
                        AdminPanelScreen(
                            currentRole = currentRole,
                            onRoleSelected = { role ->
                                viewModel.setRole(role)
                            },
                            orders = orderHistory,
                            drivers = drivers,
                            pricingMultiplier = pricingMultiplier,
                            onSetPricingMultiplier = viewModel::setPricingMultiplier,
                            onApproveKyc = { id ->
                                viewModel.updateKycStatus(id, "APPROVED")
                            },
                            onRejectKyc = { id ->
                                viewModel.updateKycStatus(id, "REJECTED")
                            },
                            pendingOrders = pendingOrders,
                            onAssignDriver = { order, driver, ctx ->
                                viewModel.assignDriverToDelivery(order, driver, ctx)
                            },
                            onUnassignDriver = { orderId, driverId ->
                                viewModel.unassignDriverFromDelivery(orderId, driverId)
                            },
                            onSetDriverAvailability = { driverId, availability ->
                                viewModel.setDriverAvailability(driverId, availability)
                            },
                            onAutoDispatch = { ctx ->
                                viewModel.autoDispatchPendingOrders(ctx)
                            },
                            onSendTestNotification = { ctx ->
                                viewModel.sendTestDriverNotification(ctx)
                            }
                        )
                    }
                }
            }
        }

        // Floating Heads-Up Local Notification Alert Banner
        AnimatedVisibility(
                visible = activeDeliveryAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                activeDeliveryAlert?.let { alert ->
                    LaunchedEffect(alert.timestamp) {
                        delay(6500)
                        viewModel.dismissDeliveryAlert()
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_app_delivery_notification_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(AmberPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Alert",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NEW DELIVERY ASSIGNED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = " • ${alert.driverName}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = "Order #${alert.order.id} • ${alert.order.goodsType}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Pickup: ${alert.order.pickupAddress.split(",")[0]} • ₹${alert.order.fare.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.setRole(UserRole.DRIVER)
                                            viewModel.dismissDeliveryAlert()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Open in Driver Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.dismissDeliveryAlert() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Dismiss", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
