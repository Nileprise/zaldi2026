package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverKyc
import com.example.data.model.UserRole
import android.content.Context
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.DriverManagementHub
import com.example.ui.components.RoleSwitcherPill
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
import com.example.ui.theme.WarningAmber

@Composable
fun AdminPanelScreen(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    orders: List<BookingOrder>,
    drivers: List<DriverKyc>,
    pricingMultiplier: Float,
    onSetPricingMultiplier: (Float) -> Unit,
    onApproveKyc: (String) -> Unit,
    onRejectKyc: (String) -> Unit,
    pendingOrders: List<BookingOrder> = emptyList(),
    onAssignDriver: (BookingOrder, DriverKyc, Context) -> Unit = { _, _, _ -> },
    onUnassignDriver: (String, String) -> Unit = { _, _ -> },
    onSetDriverAvailability: (String, String) -> Unit = { _, _ -> },
    onAutoDispatch: (Context) -> Unit = {},
    onSendTestNotification: (Context) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pendingKycCount = drivers.count { it.status == "PENDING" }
    val activeRidesCount = orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Role Switcher Pill
        RoleSwitcherPill(
            currentRole = currentRole,
            onRoleSelected = onRoleSelected
        )

        // Sub Navigation Tabs for Driver Dispatch vs Operations
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = AmberPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AmberPrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Driver & Dispatch",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                        if (pendingOrders.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = AmberPrimary
                            ) {
                                Text(
                                    text = pendingOrders.size.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("admin_tab_dispatch")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Operations & Surge Hub",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.testTag("admin_tab_operations")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (selectedTab == 0) {
                // Driver Management Hub (Maps active drivers to pending delivery requests & availability)
                DriverManagementHub(
                    drivers = drivers,
                    pendingOrders = pendingOrders,
                    allOrders = orders,
                    onAssignDriver = onAssignDriver,
                    onUnassignDriver = onUnassignDriver,
                    onSetDriverAvailability = onSetDriverAvailability,
                    onAutoDispatch = onAutoDispatch,
                    onSendTestNotification = onSendTestNotification
                )
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // Operations & Surge Hub
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONTROL CENTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = AmberPrimary
                        )
                        Text(
                            text = "Fleet Operations Hub",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            text = "Bengaluru City Sector • Live Monitoring",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                            Text(
                                text = " LIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 KPI Stat Cards Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Active Rides",
                    value = activeRidesCount.coerceAtLeast(3).toString(),
                    subtitle = "Live on map",
                    icon = Icons.Default.Navigation,
                    tint = AmberPrimary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Fleet Online",
                    value = "28",
                    subtitle = "Indiranagar / HSR",
                    icon = Icons.Default.People,
                    tint = LogisticsBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Revenue Today",
                    value = "₹48.6k",
                    subtitle = "+24% week-on-week",
                    icon = Icons.Default.TrendingUp,
                    tint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pending KYC",
                    value = pendingKycCount.toString(),
                    subtitle = "Requires review",
                    icon = Icons.Default.VerifiedUser,
                    tint = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Pricing & Surge Engine Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pricing_engine_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Surge",
                                tint = AmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Dynamic Pricing & Surge Engine",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberContainer
                        ) {
                            Text(
                                text = "${String.format("%.1f", pricingMultiplier)}x Surge",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adjust platform multiplier to balance high freight demand during peak hours or rain. Affects real-time customer fare quotes.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = pricingMultiplier,
                        onValueChange = onSetPricingMultiplier,
                        valueRange = 1.0f..2.5f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberPrimary,
                            activeTrackColor = AmberPrimary,
                            inactiveTrackColor = SurfaceTertiary
                        ),
                        modifier = Modifier.testTag("surge_multiplier_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1.0x (Normal)", fontSize = 10.sp, color = TextMuted)
                        Text("1.3x (Peak)", fontSize = 10.sp, color = TextMuted)
                        Text("1.8x (Rain)", fontSize = 10.sp, color = TextMuted)
                        Text("2.5x (Peak Rush)", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Area Density Breakdown
            Text(
                text = "Fleet Density by Sector",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectorRow("Indiranagar", 12, 12)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)
                    SectorRow("Koramangala", 9, 12)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)
                    SectorRow("HSR Layout", 7, 12)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)
                    SectorRow("Whitefield", 5, 12)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Driver KYC Verification Queue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Driver Partner KYC Approvals",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (pendingKycCount > 0) AmberContainer else SuccessContainer
                ) {
                    Text(
                        text = "$pendingKycCount Pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingKycCount > 0) AmberPrimary else SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            drivers.forEach { driver ->
                DriverKycApprovalCard(
                    driver = driver,
                    onApprove = { onApproveKyc(driver.driverId) },
                    onReject = { onRejectKyc(driver.driverId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Latest Orders Feed
            Text(
                text = "Live Orders Feed",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            orders.take(4).forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(order.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                            Text(order.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (order.status == "DELIVERED") SuccessGreen else LogisticsBlue)
                        }
                        Text(
                            text = "${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${order.vehicleName} • ${order.goodsType}", fontSize = 11.sp, color = TextMuted)
                            Text("₹${order.fare.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextDark,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun SectorRow(name: String, active: Int, max: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = "$active drivers online", fontSize = 11.sp, color = TextMuted)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { active.toFloat() / max },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = AmberPrimary,
            trackColor = SurfaceTertiary
        )
    }
}

@Composable
private fun DriverKycApprovalCard(
    driver: DriverKyc,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_kyc_item_${driver.driverId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = driver.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "${driver.vehicleTier} • ${driver.vehicleNumber}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (driver.status) {
                        "APPROVED" -> SuccessContainer
                        "PENDING" -> AmberContainer
                        else -> ErrorContainer
                    }
                ) {
                    Text(
                        text = driver.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (driver.status) {
                            "APPROVED" -> SuccessGreen
                            "PENDING" -> AmberPrimary
                            else -> ErrorRed
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "DL: ${driver.licenseNumber} • Aadhaar: ${driver.aadhaarNumber}",
                fontSize = 11.sp,
                color = TextDark.copy(alpha = 0.7f)
            )

            if (driver.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("reject_kyc_${driver.driverId}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(38.dp)
                            .testTag("approve_kyc_${driver.driverId}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
