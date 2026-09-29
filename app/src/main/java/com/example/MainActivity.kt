package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.BookingOrder
import com.example.data.model.UserRole
import com.example.service.LocationManager
import com.example.ui.screens.*
import com.example.ui.theme.AkhilLogisticsTheme
import com.example.ui.viewmodel.LogisticsViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.DeliveryNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

// Route Definitions for Jetpack Navigation
object Routes {
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val CUSTOMER_HOME = "customer_home"
    const val CUSTOMER_BOOKING = "customer_booking"
    const val CUSTOMER_ACTIVE_RIDE = "customer_active_ride"
    const val LIVE_MAP = "live_map"
    const val DRIVER_DASHBOARD = "driver_dashboard"
    const val DRIVER_PROFILE = "driver_profile"
    const val ADMIN_PANEL = "admin_panel"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: LogisticsViewModel by viewModels()
    
    @Inject
    lateinit var notificationManager: DeliveryNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        
        setContent {
            AkhilLogisticsTheme {
                LogisticsAppRoot(viewModel, notificationManager)
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
            // Note: In production, ensure the user is authenticated as a driver first
            viewModel.setAuthSession(
                phone = viewModel.userPhone.value, 
                name = viewModel.userName.value, 
                role = UserRole.DRIVER
            )
        }
    }
}

@Composable
fun LogisticsAppRoot(
    viewModel: LogisticsViewModel,
    notificationManager: DeliveryNotificationManager
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // Core App State
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    var activeInAppAlert by remember { mutableStateOf<Pair<BookingOrder, String>?>(null) }

    // ============================================================================
    // MVI UI Event Observer
    // Handles side-effects without leaking Context into the ViewModel
    // ============================================================================
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.StartForegroundService -> {
                    if (event.start) LocationManager.startLocationService(context)
                    else LocationManager.stopLocationService(context)
                }
                is UiEvent.ShowNotification -> {
                    // Trigger Android System Notification
                    notificationManager.notifyDriverAssignment(event.order, event.driverName)
                    // Trigger In-App Heads Up Banner
                    activeInAppAlert = Pair(event.order, event.driverName)
                }
                is UiEvent.NavigateTo -> {
                    navController.navigate(event.route)
                }
            }
        }
    }

    // ============================================================================
    // Initial Permissions Handling (Android 13+)
    // ============================================================================
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled silently in production */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ============================================================================
    // Navigation Routing logic based on Auth State
    // ============================================================================
    LaunchedEffect(isLoggedIn, currentRole) {
        if (!isLoggedIn) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) // Clear backstack completely
            }
        } else {
            val destination = when (currentRole) {
                UserRole.CUSTOMER -> Routes.CUSTOMER_HOME
                UserRole.DRIVER -> Routes.DRIVER_DASHBOARD
                UserRole.ADMIN -> Routes.ADMIN_PANEL
            }
            navController.navigate(destination) {
                popUpTo(0) 
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // ============================================================================
            // Navigation Graph
            // ============================================================================
            AppNavGraph(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )

            // ============================================================================
            // Floating In-App Alert Banner
            // ============================================================================
            activeInAppAlert?.let { (order, driverName) ->
                InAppDeliveryAlert(
                    order = order,
                    driverName = driverName,
                    onOpenDriverMode = {
                        viewModel.setAuthSession(viewModel.userPhone.value, viewModel.userName.value, UserRole.DRIVER)
                        activeInAppAlert = null
                    },
                    onDismiss = { activeInAppAlert = null },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: LogisticsViewModel,
    modifier: Modifier = Modifier
) {
    // Collect specific states only where needed to minimize recomposition
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
    val driverLocation by viewModel.driverLocation.collectAsStateWithLifecycle()
    val isLocationServiceRunning by viewModel.isLocationServiceRunning.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        
        // --- AUTHENTICATION ---
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { phone, role ->
                    // Real app handles actual name lookups via Auth repo
                    viewModel.setAuthSession(phone = phone, name = "Verified User", role = role)
                },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onResetSuccess = { _, _ -> navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- CUSTOMER FLOW ---
        composable(Routes.CUSTOMER_HOME) {
            val pickupAddress by viewModel.pickupAddress.collectAsStateWithLifecycle()
            val dropoffAddress by viewModel.dropoffAddress.collectAsStateWithLifecycle()
            
            CustomerHomeScreen(
                activeOrder = activeOrder,
                orderHistory = viewModel.allOrders.collectAsStateWithLifecycle().value,
                currentRole = viewModel.currentRole.collectAsStateWithLifecycle().value,
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                onPickupChange = viewModel::setPickup,
                onDropoffChange = viewModel::setDropoff,
                onRoleSelected = { viewModel.setAuthSession(viewModel.userPhone.value, viewModel.userName.value, it) },
                onStartBooking = { navController.navigate(Routes.CUSTOMER_BOOKING) },
                onViewActiveRide = { navController.navigate(Routes.CUSTOMER_ACTIVE_RIDE) },
                driverLocation = driverLocation,
                onOpenLiveMap = { navController.navigate(Routes.LIVE_MAP) },
                onQuickBook = { p, d ->
                    viewModel.setPickup(p)
                    viewModel.setDropoff(d)
                    navController.navigate(Routes.CUSTOMER_BOOKING)
                }
            )
        }

        composable(Routes.CUSTOMER_BOOKING) {
            BookingFlowScreen(
                pickupAddress = viewModel.pickupAddress.collectAsStateWithLifecycle().value,
                dropoffAddress = viewModel.dropoffAddress.collectAsStateWithLifecycle().value,
                pickupPlace = viewModel.pickupPlace.collectAsStateWithLifecycle().value,
                dropoffPlace = viewModel.dropoffPlace.collectAsStateWithLifecycle().value,
                directionsResult = viewModel.directionsResult.collectAsStateWithLifecycle().value,
                routeDistanceInfo = viewModel.routeDistanceInfo.collectAsStateWithLifecycle().value,
                selectedVehicleId = viewModel.selectedVehicleId.collectAsStateWithLifecycle().value,
                selectedGoodsType = viewModel.selectedGoodsType.collectAsStateWithLifecycle().value,
                isHelperRequired = viewModel.isHelperRequired.collectAsStateWithLifecycle().value,
                selectedPaymentMethod = viewModel.selectedPaymentMethod.collectAsStateWithLifecycle().value,
                onPickupChange = viewModel::setPickup,
                onDropoffChange = viewModel::setDropoff,
                onSelectPickupPlace = viewModel::setPickupPlace,
                onSelectDropoffPlace = viewModel::setDropoffPlace,
                onVehicleSelect = viewModel::setVehicle,
                onGoodsSelect = viewModel::setGoodsType,
                onToggleHelper = viewModel::toggleHelper,
                onPaymentSelect = viewModel::setPaymentMethod,
                calculateFare = viewModel::calculateEstimatedFare,
                onConfirmBooking = {
                    viewModel.bookRide { navController.navigate(Routes.CUSTOMER_ACTIVE_RIDE) { popUpTo(Routes.CUSTOMER_HOME) } }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CUSTOMER_ACTIVE_RIDE) {
            if (activeOrder != null) {
                ActiveRideScreen(
                    order = activeOrder!!,
                    driverLocation = driverLocation,
                    onBack = { navController.popBackStack() },
                    onCancelRide = {
                        viewModel.updateOrderStatus(it, com.example.data.model.OrderStatus.CANCELLED)
                        navController.popBackStack()
                    },
                    onCompleteRide = {
                        viewModel.updateOrderStatus(it, com.example.data.model.OrderStatus.COMPLETED)
                        navController.popBackStack()
                    },
                    onOpenLiveMap = { navController.navigate(Routes.LIVE_MAP) }
                )
            } else {
                navController.popBackStack()
            }
        }

        // --- DRIVER FLOW ---
        composable(Routes.DRIVER_DASHBOARD) {
            DriverDashboardScreen(
                currentRole = viewModel.currentRole.collectAsStateWithLifecycle().value,
                onRoleSelected = { viewModel.setAuthSession(viewModel.userPhone.value, viewModel.userName.value, it) },
                isOnline = viewModel.isDriverOnline.collectAsStateWithLifecycle().value,
                onToggleOnline = viewModel::toggleDriverOnline,
                incomingRequest = viewModel.driverIncomingRequest.collectAsStateWithLifecycle().value,
                assignedOrder = activeOrder,
                onAcceptRide = { viewModel.updateOrderStatus(it.id, com.example.data.model.OrderStatus.IN_TRANSIT) }, // Mock logic
                onDeclineRide = { /* Handle decline logic */ },
                onUpdateOrderStatus = { id, statusStr -> 
                    com.example.data.model.OrderStatus.values().find { it.name == statusStr }?.let { status ->
                        viewModel.updateOrderStatus(id, status)
                    }
                },
                onCompleteOrder = { viewModel.updateOrderStatus(it, com.example.data.model.OrderStatus.COMPLETED) },
                completedOrders = viewModel.allOrders.collectAsStateWithLifecycle().value.filter { it.status == "COMPLETED" || it.status == "DELIVERED" },
                driverLocation = driverLocation,
                isServiceRunning = isLocationServiceRunning,
                onOpenLiveMap = { navController.navigate(Routes.LIVE_MAP) },
                onOpenProfile = { navController.navigate(Routes.DRIVER_PROFILE) },
                onSendTestNotification = { /* Handled securely in VM via UI Event now */ }
            )
        }

        composable(Routes.DRIVER_PROFILE) {
            DriverProfileScreen(
                driverName = viewModel.userName.collectAsStateWithLifecycle().value,
                driverPhone = viewModel.userPhone.collectAsStateWithLifecycle().value,
                isOnline = viewModel.isDriverOnline.collectAsStateWithLifecycle().value,
                onToggleOnline = viewModel::toggleDriverOnline,
                completedOrders = viewModel.allOrders.collectAsStateWithLifecycle().value.filter { it.status == "COMPLETED" },
                onBack = { navController.popBackStack() }
            )
        }

        // --- SHARED / COMMON ---
        composable(Routes.LIVE_MAP) {
            LiveMapScreen(
                driverLocation = driverLocation,
                isServiceRunning = isLocationServiceRunning,
                activeOrder = activeOrder,
                onBack = { navController.popBackStack() }
            )
        }
        
        // --- ADMIN FLOW ---
        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(
                currentRole = viewModel.currentRole.collectAsStateWithLifecycle().value,
                onRoleSelected = { viewModel.setAuthSession(viewModel.userPhone.value, viewModel.userName.value, it) },
                orders = viewModel.allOrders.collectAsStateWithLifecycle().value,
                drivers = viewModel.allDrivers.collectAsStateWithLifecycle().value,
                pricingMultiplier = 1.0f,
                onSetPricingMultiplier = { },
                onApproveKyc = { },
                onRejectKyc = { },
                pendingOrders = viewModel.pendingOrders.collectAsStateWithLifecycle().value,
                onAssignDriver = { order, driver, _ -> viewModel.assignDriverToDelivery(order, driver) },
                onUnassignDriver = { _, _ -> },
                onSetDriverAvailability = { _, _ -> },
                onAutoDispatch = { },
                onSendTestNotification = { }
            )
        }
    }
}

@Composable
fun InAppDeliveryAlert(
    order: BookingOrder,
    driverName: String,
    onOpenDriverMode: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 8 seconds
    LaunchedEffect(order.id) {
        delay(8000)
        onDismiss()
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            modifier = Modifier.testTag("in_app_delivery_notification_banner")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW DELIVERY ASSIGNED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Order #${order.id} • ${order.goodsType}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Text(
                        text = "Pickup: ${order.pickupAddress.split(",")[0]} • ₹${order.fare.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenDriverMode,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Open Driver Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Dismiss", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.inverseOnSurface)
                        }
                    }
                }
            }
        }
    }
}
