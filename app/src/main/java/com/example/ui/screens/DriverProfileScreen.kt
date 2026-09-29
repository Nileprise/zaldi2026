package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.BookingOrder
import com.example.ui.components.DriverAvailabilityCard
import com.example.ui.components.DriverAvailabilityStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    driverName: String = "Ravi Kumar",
    driverPhone: String = "+91 98452 11094",
    vehicleModel: String = "Tata Ace Gold Plus (Mini Truck)",
    vehicleNumber: String = "KA 05 MX 2190",
    vehicleCapacity: String = "1.2 Tons • 7.2 ft Bed",
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    completedOrders: List<BookingOrder> = emptyList(), // Real data source
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showKycDialog by remember { mutableStateOf(false) }

    // Real-time calculated metrics
    val totalDeliveriesCount = completedOrders.size
    val totalEarningsLifetime = completedOrders.sumOf { it.fare }
    
    // In a real app, these would be calculated or fetched from a ViewModel
    val ratingScore = 4.88
    val acceptanceRatePercent = 96
    val onTimeDeliveryPercent = 99

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
        modifier = modifier.fillMaxSize().testTag("driver_profile_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Driver Profile & Vehicle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("driver_profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header & Availability Status ---
            val currentStatus = if (isOnline) DriverAvailabilityStatus.AVAILABLE else DriverAvailabilityStatus.OFF_DUTY

            DriverAvailabilityCard(
                driverName = driverName,
                availabilityStatus = currentStatus,
                driverId = "DRV-101",
                phoneNumber = driverPhone,
                vehicleType = vehicleModel,
                vehicleNumber = vehicleNumber,
                rating = ratingScore,
                completedTrips = totalDeliveriesCount,
                currentArea = "Connected to Local Hub",
                showStatusControls = true,
                showContactActions = false,
                isKycApproved = true,
                onStatusChange = { newStatus ->
                    when (newStatus) {
                        DriverAvailabilityStatus.AVAILABLE, DriverAvailabilityStatus.IN_TRANSIT -> if (!isOnline) handleToggleWithPermissions()
                        DriverAvailabilityStatus.OFF_DUTY -> if (isOnline) handleToggleWithPermissions()
                    }
                },
                modifier = Modifier.testTag("profile_driver_availability_card")
            )

            // --- Online Toggle Card ---
            OnlineToggleCard(isOnline = isOnline, onToggle = handleToggleWithPermissions)

            // --- Identity & Reputation Card ---
            IdentityReputationCard(
                driverName = driverName,
                driverPhone = driverPhone,
                ratingScore = ratingScore,
                totalDeliveriesCount = totalDeliveriesCount,
                onKycClick = { showKycDialog = true }
            )

            // --- Vehicle Details Card ---
            VehicleDetailsCard(
                vehicleModel = vehicleModel,
                vehicleNumber = vehicleNumber,
                vehicleCapacity = vehicleCapacity
            )

            // --- Performance Metrics Card ---
            PerformanceMetricsCard(
                acceptanceRatePercent = acceptanceRatePercent,
                onTimeDeliveryPercent = onTimeDeliveryPercent,
                totalEarningsLifetime = totalEarningsLifetime
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- KYC Dialog ---
    if (showKycDialog) {
        KycVerificationDialog(onDismiss = { showKycDialog = false })
    }
}

// ============================================================================
// Extracted Sub-Components (Isolates Recomposition)
// ============================================================================

@Composable
private fun OnlineToggleCard(isOnline: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Switch) { onToggle() }.testTag("profile_online_toggle_card"),
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
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if (isOnline) Color.Green else Color.Gray, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "ONLINE • Receiving Trips" else "OFFLINE • Shift Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isOnline) "GPS active • Eligible for customer booking requests" else "Tap toggle to start receiving delivery orders",
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
                    checkedTrackColor = Color(0xFF059669) // Success Green
                ),
                modifier = Modifier.testTag("profile_status_switch")
            )
        }
    }
}

@Composable
private fun IdentityReputationCard(
    driverName: String,
    driverPhone: String,
    ratingScore: Double,
    totalDeliveriesCount: Int,
    onKycClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().testTag("driver_identity_card"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(driverName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("GOLD PARTNER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(driverPhone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.clickable(role = Role.Button) { onKycClick() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("KYC Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$ratingScore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Overall Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("3+ Years", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Experience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalDeliveriesCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Lifetime Trips", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailsCard(vehicleModel: String, vehicleNumber: String, vehicleCapacity: String) {
    Column {
        Text("Registered Vehicle Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        
        OutlinedCard(modifier = Modifier.fillMaxWidth().testTag("driver_vehicle_details_card"), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(vehicleModel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Commercial Freight LMV", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(vehicleNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VehicleSpecRow("Payload Capacity", vehicleCapacity, Icons.Default.Speed)
                    VehicleSpecRow("RC Status", "Active (Valid till Nov 2028)", Icons.Default.CheckCircle, true)
                    VehicleSpecRow("Commercial Insurance", "Comprehensive Policy", Icons.Default.Lock, true)
                    VehicleSpecRow("Pollution Fitness (PUC)", "Passed", Icons.Default.Build, true)
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricsCard(acceptanceRatePercent: Int, onTimeDeliveryPercent: Int, totalEarningsLifetime: Double) {
    Column {
        Text("Current Performance Metrics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                PerformanceMetricProgressRow("Order Acceptance Rate", acceptanceRatePercent, "Accepted within 30s broadcast window", Color(0xFF059669))
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceMetricProgressRow("On-Time Delivery Rate", onTimeDeliveryPercent, "Delivered within estimated navigation window", MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceMetricProgressRow("Customer Satisfaction Score", 98, "Based on last 50 rated deliveries", Color(0xFFFFB300))

                Spacer(modifier = Modifier.height(24.dp))

                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cumulative Payouts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", totalEarningsLifetime)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("Top Tier", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleSpecRow(label: String, value: String, icon: ImageVector, isVerified: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isVerified) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isVerified) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PerformanceMetricProgressRow(title: String, percentage: Int, description: String, barColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("$percentage%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = barColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================================
// Dialog Composables
// ============================================================================

@Composable
private fun KycVerificationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verified Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Driving License: VERIFIED", color = Color(0xFF059669), style = MaterialTheme.typography.bodyMedium)
                Text("• Vehicle RC: ACTIVE & COMPLIANT", color = Color(0xFF059669), style = MaterialTheme.typography.bodyMedium)
                Text("• Police Verification: PASSED", color = Color(0xFF059669), style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
