package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.components.ZaldiTopBar
import com.example.ui.customer.CustomerHistoryScreen
import com.example.ui.customer.CustomerHomeScreen
import com.example.ui.customer.CustomerPaymentScreen
import com.example.ui.customer.CustomerRatingScreen
import com.example.ui.customer.CustomerTrackingScreen
import com.example.ui.driver.DriverActiveTripScreen
import com.example.ui.driver.DriverDashboardScreen
import com.example.ui.driver.DriverEarningsScreen
import com.example.ui.driver.DriverKYCScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.DriverSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ZaldiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZaldiAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZaldiAppContent(viewModel: ZaldiViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val customerSubScreen by viewModel.customerSubScreen.collectAsState()
    val driverSubScreen by viewModel.driverSubScreen.collectAsState()
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAuthModal by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Hardware / System Back Button Navigation Handling
    BackHandler(enabled = (currentRole == AppRole.CUSTOMER && customerSubScreen != CustomerSubScreen.HOME) ||
            (currentRole == AppRole.DRIVER && driverSubScreen != DriverSubScreen.DASHBOARD)) {
        if (currentRole == AppRole.CUSTOMER && customerSubScreen != CustomerSubScreen.HOME) {
            viewModel.setCustomerSubScreen(CustomerSubScreen.HOME)
        } else if (currentRole == AppRole.DRIVER && driverSubScreen != DriverSubScreen.DASHBOARD) {
            viewModel.setDriverSubScreen(DriverSubScreen.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        topBar = {
            ZaldiTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.setRole(it) },
                activeBooking = activeBooking,
                onQuickDemo = { viewModel.runAutoDemo() },
                onOpenAuth = { showAuthModal = true }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showAuthModal) {
                com.example.ui.auth.AuthScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { showAuthModal = false }
                )
            } else {
                Crossfade(targetState = currentRole, label = "role_crossfade") { role ->
                    when (role) {
                        AppRole.CUSTOMER -> {
                            CustomerNavigationHost(
                                viewModel = viewModel,
                                subScreen = customerSubScreen
                            )
                        }

                        AppRole.DRIVER -> {
                            DriverNavigationHost(
                                viewModel = viewModel,
                                subScreen = driverSubScreen
                            )
                        }

                        AppRole.ADMIN -> {
                            AdminDashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerNavigationHost(
    viewModel: ZaldiViewModel,
    subScreen: CustomerSubScreen
) {
    Crossfade(targetState = subScreen, label = "customer_subscreen_crossfade") { screen ->
        when (screen) {
            CustomerSubScreen.HOME -> CustomerHomeScreen(viewModel = viewModel)
            CustomerSubScreen.TRACKING -> CustomerTrackingScreen(viewModel = viewModel)
            CustomerSubScreen.PAYMENT -> CustomerPaymentScreen(viewModel = viewModel)
            CustomerSubScreen.RATING -> CustomerRatingScreen(viewModel = viewModel)
            CustomerSubScreen.HISTORY -> CustomerHistoryScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun DriverNavigationHost(
    viewModel: ZaldiViewModel,
    subScreen: DriverSubScreen
) {
    Crossfade(targetState = subScreen, label = "driver_subscreen_crossfade") { screen ->
        when (screen) {
            DriverSubScreen.DASHBOARD, DriverSubScreen.HISTORY -> DriverDashboardScreen(viewModel = viewModel)
            DriverSubScreen.ACTIVE_TRIP -> DriverActiveTripScreen(viewModel = viewModel)
            DriverSubScreen.KYC -> DriverKYCScreen(viewModel = viewModel)
            DriverSubScreen.EARNINGS -> DriverEarningsScreen(viewModel = viewModel)
        }
    }
}
