package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BookingOrder
import com.example.data.model.DriverAvailability
import com.example.data.model.DriverKyc
import com.example.data.model.KycStatus
import com.example.data.model.OrderStatus
import com.example.ui.theme.*

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
    onSetDriverAvailability: (String, DriverAvailability) -> Unit,
    onAutoDispatch: (Context) -> Unit,
    onSendTestNotification: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(DriverAvailabilityFilter.ALL) }
    var selectedOrderForAssignment by remember { mutableStateOf<BookingOrder?>(null) }
    var notificationFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Enforce strict Type Safety using Domain Enums instead of raw strings
    val availableDrivers = drivers.filter { 
        it.status == KycStatus.APPROVED && it.availabilityStatus == DriverAvailability.AVAILABLE 
    }
    val busyDrivers = drivers.filter { it.availabilityStatus == DriverAvailability.BUSY }
    val offlineDrivers = drivers.filter { it.availabilityStatus == DriverAvailability.OFFLINE }

    val filteredDrivers = when (selectedFilter) {
        DriverAvailabilityFilter.ALL -> drivers
        DriverAvailabilityFilter.AVAILABLE -> availableDrivers
        DriverAvailabilityFilter.IN_TRANSIT -> busyDrivers
        DriverAvailabilityFilter.OFF_DUTY -> offlineDrivers
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Smoothly animated feedback banner
        AnimatedVisibility(
            visible = notificationFeedbackMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            notificationFeedbackMessage?.let { msg ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("notification_feedback_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = SuccessContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Success Alert",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelMedium,
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
                                contentDescription = "Dismiss Alert",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Toolbar & Stats Bar
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .testTag("driver_dispatch_header_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DISPATCH CONTROLLER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Active Fleet Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            onAutoDispatch(context)
                            notificationFeedbackMessage = "Auto-dispatched pending orders & fired driver push notifications!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("auto_dispatch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoMode,
                            contentDescription = "Auto Match Drivers",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Match", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        label = "Pending",
                        count = pendingOrders.size,
                        color = WarningAmber,
                        bgColor = AmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Test Local Notification Alert",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // PENDING DELIVERY REQUESTS SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(WarningAmber, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pending Delivery Requests (${pendingOrders.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Awaiting Mapping",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (pendingOrders.isEmpty()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = SuccessContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All Clear",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All delivery requests currently mapped!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "New bookings will appear here instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            pendingOrders.forEach { order ->
                PendingOrderMappingCard(
                    order = order,
                    availableDrivers = availableDrivers,
                    onAssignClick = { selectedOrderForAssignment = order }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DRIVER AVAILABILITY & FLEET SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Driver Fleet & Availability Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${filteredDrivers.size} drivers",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Modern Kotlin 1.9+ entries property
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DriverAvailabilityFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    notificationFeedbackMessage = "Assigned ${driver.name} to #${order.id} & alerted driver!"
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Modal Dialog to Select Driver
    selectedOrderForAssignment?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrderForAssignment = null },
            title = {
                Text(
                    text = "Map Driver to Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select an available driver partner. They will immediately receive a high-priority push notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (availableDrivers.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ No approved drivers currently in 'AVAILABLE' status.",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarningAmber,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        availableDrivers.forEach { driver ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(role = Role.Button) {
                                        onAssignDriver(order, driver, context)
                                        notificationFeedbackMessage = "Assigned ${driver.name} to #${order.id}"
                                        selectedOrderForAssignment = null
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(driver.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${driver.vehicleTier} • ${driver.vehicleNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessContainer
                                    ) {
                                        Text(
                                            "Assign",
                                            style = MaterialTheme.typography.labelSmall,
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
                    Text("Cancel")
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
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarningAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNASSIGNED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }

                Text(
                    text = "₹${order.fare.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.goodsType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Required: ${order.vehicleName} (${order.distanceKm} km)",
                    style = MaterialTheme.typography.labelMedium,
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
                        contentDescription = "Assign Driver",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Map Driver", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
    onSetAvailability: (DriverAvailability) -> Unit,
    onUnassign: (String) -> Unit,
    onAssignPendingOrder: (BookingOrder) -> Unit
) {
    var showQuickAssignDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        DriverAvailabilityCard(
            driverId = driver.driverId,
            driverName = driver.name,
            statusText = driver.availabilityStatus.name,
            onClick = {
                if (driver.availabilityStatus == DriverAvailability.AVAILABLE && pendingOrders.isNotEmpty()) {
                    showQuickAssignDropdown = !showQuickAssignDropdown
                }
            }
        )

        AnimatedVisibility(
            visible = showQuickAssignDropdown,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = AmberContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Map ${driver.name} to Pending Request:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    pendingOrders.forEach { order ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(role = Role.Button) {
                                    onAssignPendingOrder(order)
                                    showQuickAssignDropdown = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("#${order.id} • ${order.goodsType}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("₹${order.fare.toInt()} • ${order.pickupAddress.split(",")[0]} → ${order.dropoffAddress.split(",")[0]}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Map →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
