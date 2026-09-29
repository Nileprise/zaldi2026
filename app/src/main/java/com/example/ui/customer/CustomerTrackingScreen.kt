package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.components.InteractiveMapView
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

@Composable
fun CustomerTrackingScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    val context = LocalContext.current
    var showChatSheet by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (activeBooking == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = "No Active Trip",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Delivery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    text = "Book a vehicle from the Home screen to start tracking.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.setCustomerSubScreen(CustomerSubScreen.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
                ) {
                    Text("Go to Booking", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val booking = activeBooking!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("customer_tracking_screen")
    ) {
        // 1. Interactive Vector Map view (upper half)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            InteractiveMapView(
                modifier = Modifier.fillMaxSize(),
                activeBooking = booking
            )

            // ETA and Live Speed floating badge on top of map
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (booking.status) {
                            Booking.STATUS_SEARCHING -> "Searching Drivers..."
                            Booking.STATUS_ASSIGNED -> "Driver Arriving in 4 min"
                            Booking.STATUS_ARRIVED_PICKUP -> "Driver at Pickup"
                            Booking.STATUS_IN_TRANSIT -> "In Transit • ETA ${booking.estimatedDurationMin}m"
                            Booking.STATUS_DELIVERED -> "Delivered"
                            else -> "Trip ${booking.status}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Tracking Details & Driver Info (scrollable lower half)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Milestone / Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("status_milestone_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ORDER ${booking.bookingCode}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = when (booking.status) {
                                    Booking.STATUS_SEARCHING -> "Looking for Nearby Drivers"
                                    Booking.STATUS_ASSIGNED -> "Driver Partner Assigned"
                                    Booking.STATUS_ARRIVED_PICKUP -> "Driver Arrived at Pickup Gate"
                                    Booking.STATUS_IN_TRANSIT -> "Package on the Way"
                                    Booking.STATUS_DELIVERED -> "Delivered at Destination"
                                    else -> booking.status
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }

                        // Searching radar animation or status icon
                        if (booking.status == Booking.STATUS_SEARCHING) {
                            val infiniteTransition = rememberInfiniteTransition(label = "radar")
                            val pulse by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .scale(pulse)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Radar,
                                    contentDescription = "Radar Scan",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Stepper (5 points)
                    TrackingStepper(booking.status)

                    // Pickup OTP Display (Crucial for customer security)
                    if (booking.status in listOf(Booking.STATUS_ASSIGNED, Booking.STATUS_ARRIVED_PICKUP)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PICKUP VERIFICATION OTP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Text(
                                        text = "Share with driver at loading gate",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF59E0B))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = booking.pickupOtp,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 3.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Driver Profile Card (if assigned)
            if (booking.driverName != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().testTag("driver_info_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Driver avatar circle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = booking.driverName!!.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = booking.driverName!!,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${booking.driverRating} ★",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " • Verified Partner",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "${booking.vehicleType} • ${booking.driverVehicleNo ?: ""}",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        // Call & Chat buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showChatSheet = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .testTag("customer_chat_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat Driver", tint = Color(0xFF38BDF8))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    booking.driverPhone?.let { phone ->
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .testTag("customer_call_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }

            // Route & Cargo Details
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("route_summary_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shipment Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "From: ${booking.pickupTitle}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = booking.pickupAddress, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "To: ${booking.dropTitle}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(text = booking.dropAddress, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Cargo: ${booking.goodsType}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(text = "Total: ₹${booking.totalFare.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(18.dp))
            if (booking.status == Booking.STATUS_DELIVERED) {
                Button(
                    onClick = { viewModel.setCustomerSubScreen(CustomerSubScreen.PAYMENT) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("proceed_payment_button")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = "Pay")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proceed to Payment (₹${booking.totalFare.toInt()})", fontWeight = FontWeight.Bold)
                }
            } else if (booking.status != Booking.STATUS_CANCELLED) {
                OutlinedButton(
                    onClick = { viewModel.cancelActiveBooking(booking.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_booking_button")
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Booking", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showChatSheet) {
        com.example.ui.components.ChatBottomSheet(
            driverName = booking.driverName ?: "Driver Partner",
            vehicleNo = booking.driverVehicleNo ?: "KA-01-MJ-8921",
            onDismiss = { showChatSheet = false }
        )
    }
}

@Composable
fun TrackingStepper(status: String) {
    val steps = listOf(
        Booking.STATUS_CREATED to "Created",
        Booking.STATUS_ASSIGNED to "Assigned",
        Booking.STATUS_ARRIVED_PICKUP to "At Pickup",
        Booking.STATUS_IN_TRANSIT to "In Transit",
        Booking.STATUS_DELIVERED to "Delivered"
    )

    val currentStepIndex = when (status) {
        Booking.STATUS_SEARCHING, Booking.STATUS_CREATED -> 0
        Booking.STATUS_ASSIGNED -> 1
        Booking.STATUS_ARRIVED_PICKUP, Booking.STATUS_STARTED -> 2
        Booking.STATUS_IN_TRANSIT -> 3
        Booking.STATUS_DELIVERED -> 4
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (_, label) ->
            val isPassed = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> Color(0xFFF59E0B)
                                isPassed -> Color(0xFF10B981)
                                else -> Color(0xFF334155)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed && !isCurrent) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color(0xFF0F172A) else Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isPassed) Color.White else Color(0xFF64748B),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentStepIndex) Color(0xFF10B981) else Color(0xFF334155))
                )
            }
        }
    }
}
