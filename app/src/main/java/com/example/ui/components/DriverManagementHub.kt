package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverKyc
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

enum class DriverAvailabilityFilter(val label: String) {
    ALL("All Drivers"),
    AVAILABLE("Available"),
    IN_TRANSIT("In Transit"),
    OFF_DUTY("Off Duty")
}

@Composable
fun DriverManagementHub(
    drivers: List<DriverKyc>,
    pendingOrders: List<BookingOrder>,
    allOrders: List<BookingOrder>,
    onAssignDriver: (BookingOrder, DriverKyc, Context) -> Unit,
    onUnassignDriver: (String, String) -> Unit,
    onSetDriverAvailability: (String, String) -> Unit,
    onAutoDispatch: (Context) -> Unit,
    onSendTestNotification: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(DriverAvailabilityFilter.ALL) }
    var selectedOrderForAssignment by remember { mutableStateOf<BookingOrder?>(null) }
    var notificationFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val availableDrivers = drivers.filter { it.status == "APPROVED" && (it.availabilityStatus == "AVAILABLE" || it.availabilityStatus == "Available") }
    val busyDrivers = drivers.filter { it.availabilityStatus in listOf("BUSY", "IN_TRANSIT", "In Transit", "ON_TRIP") }
    val offlineDrivers = drivers.filter { it.availabilityStatus in listOf("OFFLINE", "OFF_DUTY", "Off Duty") }

    val filteredDrivers = when (selectedFilter) {
        DriverAvailabilityFilter.ALL -> drivers
        DriverAvailabilityFilter.AVAILABLE -> availableDrivers
        DriverAvailabilityFilter.IN_TRANSIT -> busyDrivers
        DriverAvailabilityFilter.OFF_DUTY -> offlineDrivers
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Notification Alert Feedback Banner
        notificationFeedbackMessage?.let { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("notification_feedback_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { notificationFeedbackMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Action Toolbar & Stats Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .testTag("driver_dispatch_header_card"),
            shape = RoundedCornerShape(18.dp),
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
                            text = "ROOM DATABASE DISPATCHER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp,
                            color = AmberPrimary
                        )
                        Text(
                            text = "Active Driver Fleet & Requests",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    // Auto dispatch action
                    Button(
                        onClick = {
                            onAutoDispatch(context)
                            notificationFeedbackMessage = "Auto-dispatched pending orders & fired driver push notifications!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("auto_dispatch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoMode,
                            contentDescription = "Auto Match",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Fleet Stat Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusSummaryPill(
                        label = "Available",
                        count = availableDrivers.size,
                        color = SuccessGreen,
                        bgColor = SuccessContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatusSummaryPill(
                        label = "On Trip",
                        count = busyDrivers.size,
                        color = LogisticsBlue,
                        bgColor = LogisticsBlueContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatusSummaryPill(
                        label = "Offline",
                        count = offlineDrivers.size,
                        color = TextMuted,
                        bgColor = SurfaceTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    StatusSummaryPill(
                        label = "Pending Orders",
                        count = pendingOrders.size,
                        color = WarningAmber,
                        bgColor = AmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Local Notification Button
                OutlinedButton(
                    onClick = {
                        onSendTestNotification(context)
                        notificationFeedbackMessage = "Sent test delivery assignment notification with vibration!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_driver_notification_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = "Test Notification",
                        tint = AmberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Test Local Notification Alert (Heads-up & Vibration)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberPrimary
                    )
                }
            }
        }

        // PENDING DELIVERY REQUESTS SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(WarningAmber, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pending Delivery Requests (${pendingOrders.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Text(
                text = "Awaiting Driver Mapping",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        if (pendingOrders.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All Clear",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "All delivery requests currently mapped!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "New customer bookings will appear here instantly via Room Database Flow.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            pendingOrders.forEach { order ->
                PendingOrderMappingCard(
                    order = order,
                    availableDrivers = availableDrivers,
                    onAssignClick = {
                        selectedOrderForAssignment = order
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // DRIVER AVAILABILITY & FLEET SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Driver Fleet & Availability Status",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "${filteredDrivers.size} drivers",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Availability Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DriverAvailabilityFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Driver List Cards
        filteredDrivers.forEach { driver ->
            val mappedOrder = allOrders.find { it.id == driver.assignedOrderId }

            DriverFleetCard(
                driver = driver,
                mappedOrder = mappedOrder,
                pendingOrders = pendingOrders,
                onSetAvailability = { newStatus ->
                    onSetDriverAvailability(driver.driverId, newStatus)
                },
                onUnassign = { orderId ->
                    onUnassignDriver(orderId, driver.driverId)
                    notificationFeedbackMessage = "Unassigned driver ${driver.name} from Order #$orderId"
                },
                onAssignPendingOrder = { order ->
                    onAssignDriver(order, driver, context)
                    notificationFeedbackMessage = "Assigned ${driver.name} to #${order.id} & alerted driver via local notification!"
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Modal Dialog to Select Driver for a Pending Order
    selectedOrderForAssignment?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrderForAssignment = null },
            title = {
                Text(
                    text = "Map Driver to Order #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select an available driver partner from the Room database. The driver will immediately receive a high-priority local notification with sound & vibration.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (availableDrivers.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ No approved drivers currently in 'AVAILABLE' status. Change a driver's status below or set one to Available.",
                                fontSize = 11.sp,
                                color = AmberPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        availableDrivers.forEach { driver ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onAssignDriver(order, driver, context)
                                        notificationFeedbackMessage = "Assigned ${driver.name} to #${order.id} & fired push notification!"
                                        selectedOrderForAssignment = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                        Text("${driver.vehicleTier} • ${driver.vehicleNumber}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessContainer
                                    ) {
                                        Text(
                                            "Assign & Alert",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedOrderForAssignment = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun StatusSummaryPill(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PendingOrderMappingCard(
    order: BookingOrder,
    availableDrivers: List<DriverKyc>,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberContainer
                    ) {
                        Text(
                            text = order.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNASSIGNED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }

                Text(
                    text = "₹${order.fare.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.goodsType,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Text(
                text = "${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Required: ${order.vehicleName} (${order.distanceKm} km)",
                    fontSize = 11.sp,
                    color = LogisticsBlue,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onAssignClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("assign_driver_btn_${order.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Assign",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Map Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DriverFleetCard(
    driver: DriverKyc,
    mappedOrder: BookingOrder?,
    pendingOrders: List<BookingOrder>,
    onSetAvailability: (String) -> Unit,
    onUnassign: (String) -> Unit,
    onAssignPendingOrder: (BookingOrder) -> Unit
) {
    var showQuickAssignDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        DriverAvailabilityCard(
            driver = driver,
            mappedOrder = mappedOrder,
            showStatusControls = true,
            showContactActions = true,
            onStatusChange = { newStatus ->
                val statusStr = when (newStatus) {
                    DriverAvailabilityStatus.AVAILABLE -> "AVAILABLE"
                    DriverAvailabilityStatus.IN_TRANSIT -> "IN_TRANSIT"
                    DriverAvailabilityStatus.OFF_DUTY -> "OFF_DUTY"
                }
                onSetAvailability(statusStr)
            },
            onCallClick = { /* Call action handled */ },
            onUnassignClick = if (mappedOrder != null) {
                { onUnassign(mappedOrder.id) }
            } else null,
            onAssignClick = if (driver.availabilityStatus in listOf("AVAILABLE", "Available") && pendingOrders.isNotEmpty()) {
                { showQuickAssignDropdown = !showQuickAssignDropdown }
            } else null
        )

        AnimatedVisibility(visible = showQuickAssignDropdown) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Map ${driver.name} to Pending Request:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    pendingOrders.forEach { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    onAssignPendingOrder(order)
                                    showQuickAssignDropdown = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("#${order.id} • ${order.goodsType}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${order.fare.toInt()} • ${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}", fontSize = 10.sp, color = TextMuted)
                                }
                                Text("Map →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityToggleButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) selectedColor else SurfaceTertiary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("status_toggle_${label.lowercase()}")
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
