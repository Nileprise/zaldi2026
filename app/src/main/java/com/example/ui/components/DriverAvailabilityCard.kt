package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceTertiary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber

/**
 * Driver availability statuses supported across customer booking, driver profile,
 * and dispatch administration.
 */
enum class DriverAvailabilityStatus(
    val displayName: String,
    val subtitle: String,
    val testTagId: String
) {
    AVAILABLE(
        displayName = "Available",
        subtitle = "Ready for new dispatch",
        testTagId = "status_available"
    ),
    IN_TRANSIT(
        displayName = "In Transit",
        subtitle = "Active on delivery route",
        testTagId = "status_in_transit"
    ),
    OFF_DUTY(
        displayName = "Off Duty",
        subtitle = "Offline / Taking a break",
        testTagId = "status_off_duty"
    );

    companion object {
        fun fromString(status: String?): DriverAvailabilityStatus {
            if (status.isNullOrBlank()) return OFF_DUTY
            val normalized = status.uppercase().trim().replace(" ", "_")
            return when {
                normalized in listOf("AVAILABLE", "ONLINE", "ACTIVE", "READY") -> AVAILABLE
                normalized in listOf("IN_TRANSIT", "INTRANSIT", "ON_TRIP", "BUSY", "ASSIGNED", "EN_ROUTE") -> IN_TRANSIT
                normalized in listOf("OFF_DUTY", "OFFDUTY", "OFFLINE", "BREAK", "INACTIVE") -> OFF_DUTY
                else -> OFF_DUTY
            }
        }
    }
}

/**
 * Visual styling definition for each driver availability state.
 */
private data class AvailabilityVisuals(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val shouldPulse: Boolean
)

private fun getAvailabilityVisuals(status: DriverAvailabilityStatus): AvailabilityVisuals {
    return when (status) {
        DriverAvailabilityStatus.AVAILABLE -> AvailabilityVisuals(
            containerColor = Color(0xFFE8F9F1),
            contentColor = Color(0xFF059669),
            borderColor = Color(0xFFA7F3D0),
            icon = Icons.Default.CheckCircle,
            shouldPulse = true
        )
        DriverAvailabilityStatus.IN_TRANSIT -> AvailabilityVisuals(
            containerColor = Color(0xFFEBF2FF),
            contentColor = Color(0xFF1D4ED8),
            borderColor = Color(0xFFBFDBFE),
            icon = Icons.Default.Navigation,
            shouldPulse = true
        )
        DriverAvailabilityStatus.OFF_DUTY -> AvailabilityVisuals(
            containerColor = Color(0xFFF3F4F6),
            contentColor = Color(0xFF6B7280),
            borderColor = Color(0xFFE5E7EB),
            icon = Icons.Default.Schedule,
            shouldPulse = false
        )
    }
}

/**
 * DriverAvailabilityCard
 *
 * A modern, accessible Material Design 3 composable card component displaying
 * comprehensive driver information, operational metrics, contact options,
 * and prominent availability status ('Available', 'In Transit', 'Off Duty')
 * with real-time animated indicator and interactive status switcher controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverAvailabilityCard(
    driverName: String,
    availabilityStatus: DriverAvailabilityStatus,
    modifier: Modifier = Modifier,
    driverId: String? = null,
    phoneNumber: String? = null,
    vehicleType: String = "Mini Truck",
    vehicleNumber: String = "KA 05 MX 2190",
    rating: Double = 4.85,
    completedTrips: Int = 142,
    currentArea: String? = "Indiranagar, Bengaluru",
    assignedOrderId: String? = null,
    assignedOrderRoute: String? = null,
    assignedOrderGoodsType: String? = null,
    assignedOrderFare: Double? = null,
    showStatusControls: Boolean = true,
    showContactActions: Boolean = true,
    isKycApproved: Boolean = true,
    onStatusChange: ((DriverAvailabilityStatus) -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    onUnassignClick: (() -> Unit)? = null,
    onAssignClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val visuals = getAvailabilityVisuals(availabilityStatus)

    // Pulsing animation for live Available and In Transit states
    val infiniteTransition = rememberInfiniteTransition(label = "driver_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_dot_alpha"
    )

    // Initials for avatar
    val initials = remember(driverName) {
        val parts = driverName.trim().split(" ")
        when {
            parts.size >= 2 -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "DR"
        }
    }

    // Vehicle icon based on vehicle tier
    val vehicleIcon = remember(vehicleType) {
        when {
            vehicleType.contains("2-Wheeler", true) || vehicleType.contains("Bike", true) -> Icons.Default.TwoWheeler
            vehicleType.contains("Auto", true) || vehicleType.contains("3-Wheeler", true) -> Icons.Default.ElectricRickshaw
            vehicleType.contains("Car", true) -> Icons.Default.DirectionsCar
            else -> Icons.Default.LocalShipping
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .testTag(if (!driverId.isNullOrBlank()) "driver_card_$driverId" else "driver_availability_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ==========================================
            // HEADER ROW: Avatar + Driver Info + Status Badge
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Avatar with Status Dot Overlay and Driver Name/Vehicle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Box with corner Status Dot
                    Box(modifier = Modifier.size(50.dp)) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = when (availabilityStatus) {
                                DriverAvailabilityStatus.AVAILABLE -> AmberContainer
                                DriverAvailabilityStatus.IN_TRANSIT -> LogisticsBlueContainer
                                DriverAvailabilityStatus.OFF_DUTY -> SurfaceTertiary
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (availabilityStatus) {
                                        DriverAvailabilityStatus.AVAILABLE -> AmberPrimary
                                        DriverAvailabilityStatus.IN_TRANSIT -> LogisticsBlue
                                        DriverAvailabilityStatus.OFF_DUTY -> TextMuted
                                    }
                                )
                            }
                        }

                        // Status dot overlay on avatar edge
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(10.dp)
                                    .alpha(if (visuals.shouldPulse) pulseAlpha else 1f)
                                    .background(visuals.contentColor, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // Driver Name + Verified Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = driverName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.testTag("driver_name_text")
                            )

                            if (isKycApproved) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Driver",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (!driverId.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SurfaceTertiary
                                ) {
                                    Text(
                                        text = driverId,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Vehicle Tier & License Plate
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vehicleIcon,
                                contentDescription = vehicleType,
                                tint = AmberPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = vehicleType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            Text(
                                text = " • ",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            // License Plate Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(0.5.dp, Color(0xFFD97706))
                            ) {
                                Text(
                                    text = vehicleNumber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                        .testTag("driver_vehicle_plate")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Prominent Availability Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = visuals.containerColor,
                    border = BorderStroke(1.dp, visuals.borderColor),
                    modifier = Modifier.testTag("driver_availability_badge_${availabilityStatus.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulsing / solid dot indicator
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .alpha(if (visuals.shouldPulse) pulseAlpha else 1f)
                                .background(visuals.contentColor, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = availabilityStatus.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = visuals.contentColor,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // LOGISTICS METRICS ROW: Rating, Trips, Current Area
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceTertiary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating & Trips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = AmberPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", rating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = " ($completedTrips trips)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Current Area / Hub
                if (!currentArea.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Operating Area",
                            tint = LogisticsBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = currentArea,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            maxLines = 1
                        )
                    }
                }
            }

            // ==========================================
            // ACTIVE ASSIGNMENT / IN-TRANSIT BANNER
            // ==========================================
            if (availabilityStatus == DriverAvailabilityStatus.IN_TRANSIT && !assignedOrderId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LogisticsBlueContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Active Ride",
                                    tint = LogisticsBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active Trip #$assignedOrderId",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LogisticsBlue
                                )
                                if (assignedOrderFare != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ₹${assignedOrderFare.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextDark
                                    )
                                }
                            }

                            if (!assignedOrderGoodsType.isNullOrBlank()) {
                                Text(
                                    text = "Cargo: $assignedOrderGoodsType",
                                    fontSize = 11.sp,
                                    color = TextDark
                                )
                            }

                            if (!assignedOrderRoute.isNullOrBlank()) {
                                Text(
                                    text = assignedOrderRoute,
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        if (onUnassignClick != null) {
                            OutlinedButton(
                                onClick = onUnassignClick,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .testTag("unassign_driver_btn")
                            ) {
                                Text("Unassign", fontSize = 11.sp, color = ErrorRed)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // STATUS CONTROL BUTTONS (Available | In Transit | Off Duty)
            // ==========================================
            if (showStatusControls && onStatusChange != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Availability Status:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Text(
                        text = visuals.icon.let { availabilityStatus.subtitle },
                        fontSize = 10.sp,
                        color = visuals.contentColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Segmented availability buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DriverAvailabilityStatus.values().forEach { targetStatus ->
                        val isSelected = targetStatus == availabilityStatus
                        val targetVisuals = getAvailabilityVisuals(targetStatus)

                        val animatedBg by animateColorAsState(
                            targetValue = if (isSelected) targetVisuals.containerColor else SurfaceLight,
                            label = "status_btn_bg"
                        )
                        val animatedBorder by animateColorAsState(
                            targetValue = if (isSelected) targetVisuals.borderColor else BorderLight,
                            label = "status_btn_border"
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isSelected) {
                                        onStatusChange(targetStatus)
                                    }
                                }
                                .testTag("status_toggle_${targetStatus.name.lowercase()}"),
                            shape = RoundedCornerShape(10.dp),
                            color = animatedBg,
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (isSelected) targetVisuals.contentColor else TextMuted.copy(alpha = 0.4f),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = targetStatus.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) targetVisuals.contentColor else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // ACTION BUTTONS (Call, Chat, Assign)
            // ==========================================
            if (showContactActions && (onCallClick != null || onMessageClick != null || onAssignClick != null)) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onCallClick != null && !phoneNumber.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = onCallClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("driver_call_action_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call $driverName",
                                tint = LogisticsBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogisticsBlue
                            )
                        }
                    }

                    if (onMessageClick != null) {
                        OutlinedButton(
                            onClick = onMessageClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("driver_message_action_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat with $driverName",
                                tint = AmberPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberPrimary
                            )
                        }
                    }

                    if (onAssignClick != null && availabilityStatus == DriverAvailabilityStatus.AVAILABLE) {
                        Button(
                            onClick = onAssignClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            modifier = Modifier.testTag("driver_assign_order_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Assign Order",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * DriverAvailabilityCard overload accepting a [DriverKyc] entity model
 * directly from the application's Room database repository.
 */
@Composable
fun DriverAvailabilityCard(
    driver: DriverKyc,
    modifier: Modifier = Modifier,
    mappedOrder: BookingOrder? = null,
    showStatusControls: Boolean = true,
    showContactActions: Boolean = true,
    onStatusChange: ((DriverAvailabilityStatus) -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    onUnassignClick: (() -> Unit)? = null,
    onAssignClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val status = DriverAvailabilityStatus.fromString(driver.availabilityStatus)

    val routeText = mappedOrder?.let {
        "${it.pickupAddress.split(",")[0]} → ${it.dropoffAddress.split(",")[0]}"
    }

    DriverAvailabilityCard(
        driverName = driver.name,
        availabilityStatus = status,
        modifier = modifier,
        driverId = driver.driverId,
        phoneNumber = driver.phone,
        vehicleType = driver.vehicleTier,
        vehicleNumber = driver.vehicleNumber,
        rating = driver.rating,
        completedTrips = driver.completedTrips,
        currentArea = driver.currentArea,
        assignedOrderId = mappedOrder?.id ?: driver.assignedOrderId,
        assignedOrderRoute = routeText,
        assignedOrderGoodsType = mappedOrder?.goodsType,
        assignedOrderFare = mappedOrder?.fare,
        showStatusControls = showStatusControls,
        showContactActions = showContactActions,
        isKycApproved = driver.status == "APPROVED",
        onStatusChange = onStatusChange,
        onCallClick = onCallClick,
        onMessageClick = onMessageClick,
        onUnassignClick = onUnassignClick,
        onAssignClick = onAssignClick,
        onClick = onClick
    )
}

/**
 * Convenience aliases for caller versatility.
 */
@Composable
fun DriverCard(
    driver: DriverKyc,
    modifier: Modifier = Modifier,
    mappedOrder: BookingOrder? = null,
    showStatusControls: Boolean = true,
    onStatusChange: ((DriverAvailabilityStatus) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    DriverAvailabilityCard(
        driver = driver,
        modifier = modifier,
        mappedOrder = mappedOrder,
        showStatusControls = showStatusControls,
        onStatusChange = onStatusChange,
        onClick = onClick
    )
}

@Composable
fun DriverInfoCard(
    driverName: String,
    availabilityStatus: DriverAvailabilityStatus,
    modifier: Modifier = Modifier,
    driverId: String? = null,
    phoneNumber: String? = null,
    vehicleType: String = "Mini Truck",
    vehicleNumber: String = "KA 05 MX 2190",
    rating: Double = 4.85,
    completedTrips: Int = 142,
    currentArea: String? = "Indiranagar, Bengaluru",
    showStatusControls: Boolean = true,
    onStatusChange: ((DriverAvailabilityStatus) -> Unit)? = null
) {
    DriverAvailabilityCard(
        driverName = driverName,
        availabilityStatus = availabilityStatus,
        modifier = modifier,
        driverId = driverId,
        phoneNumber = phoneNumber,
        vehicleType = vehicleType,
        vehicleNumber = vehicleNumber,
        rating = rating,
        completedTrips = completedTrips,
        currentArea = currentArea,
        showStatusControls = showStatusControls,
        onStatusChange = onStatusChange
    )
}
