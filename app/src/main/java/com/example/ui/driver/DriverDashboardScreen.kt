package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Driver
import com.example.ui.components.ZaldiPermissionBanner
import com.example.ui.components.isLocationGranted
import com.example.ui.components.isNotificationGranted
import com.example.ui.components.rememberZaldiPermissionsState
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.DriverSubScreen
import com.example.ui.viewmodel.ZaldiViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DriverDashboardScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val currentDriverId by viewModel.currentDriverId.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val driver = allDrivers.firstOrNull { it.id == currentDriverId } ?: Driver(
        id = "DRV-101",
        name = "Venkatesh Rao",
        phone = "+91 98450 11223"
    )

    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    val permissionsState = rememberZaldiPermissionsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("driver_dashboard_screen")
    ) {
        // Driver Profile Card & Duty Status
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth().testTag("driver_profile_header")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = driver.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = driver.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = "KYC Verified",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${driver.vehicleModel} • ${driver.vehiclePlate}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Online/Offline Switch
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = driver.isOnline,
                            onCheckedChange = { isGoingOnline ->
                                if (isGoingOnline && (!permissionsState.isLocationGranted() || !permissionsState.isNotificationGranted())) {
                                    permissionsState.launchMultiplePermissionRequest()
                                }
                                viewModel.toggleDriverDuty(driver.id, isGoingOnline)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF10B981),
                                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f),
                                uncheckedThumbColor = Color(0xFFEF4444),
                                uncheckedTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.testTag("driver_duty_switch")
                        )
                        Text(
                            text = if (driver.isOnline) "ON DUTY" else "OFF DUTY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (driver.isOnline) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // Driver Runtime Location & Notification Permissions Banner
        ZaldiPermissionBanner(
            permissionsState = permissionsState,
            role = AppRole.DRIVER
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Today's Earnings",
                value = "₹${driver.todayEarnings.toInt()}",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Wallet Balance",
                value = "₹${driver.walletBalance.toInt()}",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Trips",
                value = "${driver.tripsCount}",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Driver Rating",
                value = "${driver.rating} ★",
                color = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Incoming Booking Request HUD (if active booking is searching or assigned)
        if (activeBooking != null && (activeBooking!!.status == Booking.STATUS_SEARCHING || activeBooking!!.driverId == driver.id)) {
            val booking = activeBooking!!
            IncomingBookingHUD(
                booking = booking,
                onAccept = {
                    viewModel.verifyPickupOtp(booking.id, booking.pickupOtp)
                    viewModel.setDriverSubScreen(DriverSubScreen.ACTIVE_TRIP)
                },
                onViewTrip = {
                    viewModel.setDriverSubScreen(DriverSubScreen.ACTIVE_TRIP)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Navigation Tabs
        Text(
            text = "Driver Partner Hub",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                DriverMenuRow(
                    icon = Icons.Default.DirectionsCar,
                    title = "Active Navigation Trip",
                    subtitle = if (activeBooking != null && !activeBooking!!.isTerminalState) "Live order ${activeBooking!!.bookingCode} in progress" else "No trip in progress",
                    onClick = { viewModel.setDriverSubScreen(DriverSubScreen.ACTIVE_TRIP) },
                    testTag = "menu_active_trip"
                )
                Divider(color = Color(0xFF334155))
                DriverMenuRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Earnings & Payout Ledger",
                    subtitle = "Daily breakdown, incentives, payout transfer",
                    onClick = { viewModel.setDriverSubScreen(DriverSubScreen.EARNINGS) },
                    testTag = "menu_earnings"
                )
                Divider(color = Color(0xFF334155))
                DriverMenuRow(
                    icon = Icons.Default.VerifiedUser,
                    title = "KYC & Vehicle Documents",
                    subtitle = "Driving License, RC, Insurance (${driver.kycStatus})",
                    onClick = { viewModel.setDriverSubScreen(DriverSubScreen.KYC) },
                    testTag = "menu_kyc"
                )
                Divider(color = Color(0xFF334155))
                DriverMenuRow(
                    icon = Icons.Default.History,
                    title = "Trip History & Delivery Records",
                    subtitle = "Past completed deliveries & earnings",
                    onClick = { viewModel.setDriverSubScreen(DriverSubScreen.HISTORY) },
                    testTag = "menu_history"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun IncomingBookingHUD(
    booking: Booking,
    onAccept: () -> Unit,
    onViewTrip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF38BDF8)),
        modifier = Modifier.fillMaxWidth().testTag("incoming_booking_hud")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (booking.status == Booking.STATUS_SEARCHING) "INCOMING BOOKING REQUEST" else "ACTIVE TRIP ASSIGNED",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Payout: ₹${(booking.totalFare * 0.85).toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFFF59E0B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Order #${booking.bookingCode} • ${booking.vehicleType}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = "Pickup: ${booking.pickupTitle}",
                fontSize = 12.sp,
                color = Color(0xFFE0F2FE)
            )
            Text(
                text = "Drop: ${booking.dropTitle} (${booking.distanceKm} km)",
                fontSize = 12.sp,
                color = Color(0xFFE0F2FE)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (booking.status == Booking.STATUS_SEARCHING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("accept_booking_btn")
                    ) {
                        Text("Accept Order", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onViewTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("view_active_trip_btn")
                ) {
                    Text("Open Trip Navigation", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DriverMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}
