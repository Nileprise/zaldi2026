package com.example.ui.driver

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.components.InteractiveMapView
import com.example.ui.viewmodel.DriverSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

@Composable
fun DriverActiveTripScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    var otpInput by remember { mutableStateOf("") }
    var showDirections by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("driver_active_trip_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setDriverSubScreen(DriverSubScreen.DASHBOARD) },
                modifier = Modifier.testTag("driver_trip_back")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Live Navigation & Delivery",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
        }

        if (activeBooking == null || activeBooking!!.status == Booking.STATUS_SEARCHING) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active trip assigned yet.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
            return
        }

        val booking = activeBooking!!

        // Map View (220dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            InteractiveMapView(
                modifier = Modifier.fillMaxSize(),
                activeBooking = booking
            )

            // HUD Overlay: Speedometer & Directions
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    modifier = Modifier.clickable { showDirections = true }.testTag("driver_directions_hud_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Directions", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Directions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (booking.status == Booking.STATUS_IN_TRANSIT) "42 km/h" else "0 km/h",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Action workflow & details (scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stage Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().testTag("driver_trip_stage_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ORDER #${booking.bookingCode} • ${booking.vehicleType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = when (booking.status) {
                            Booking.STATUS_ASSIGNED -> "Navigate to Customer Pickup"
                            Booking.STATUS_ARRIVED_PICKUP -> "Verify Customer OTP & Load Cargo"
                            Booking.STATUS_IN_TRANSIT -> "In-Transit to Destination"
                            Booking.STATUS_DELIVERED -> "Delivered! Collect Payment"
                            else -> booking.status
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic action depending on stage
                    when (booking.status) {
                        Booking.STATUS_ASSIGNED -> {
                            Text(
                                text = "Pickup: ${booking.pickupAddress}",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    // Move to arrived at pickup
                                    viewModel.verifyPickupOtp(booking.id, "0000") // Will trigger pickup verification
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("arrived_at_pickup_btn")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "Arrived")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I Have Arrived at Pickup Gate", fontWeight = FontWeight.Bold)
                            }
                        }

                        Booking.STATUS_ARRIVED_PICKUP, Booking.STATUS_ASSIGNED -> {
                            // OTP Input prompt
                            Text(
                                text = "Ask the customer for their 4-digit pickup code before loading goods:",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { if (it.length <= 4) otpInput = it },
                                    placeholder = { Text("4-digit OTP", fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFF59E0B),
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("otp_input_field")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.verifyPickupOtp(booking.id, otpInput)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("verify_otp_btn")
                                ) {
                                    Text("Verify & Start", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick Fill Helper for Demo / Testing
                            TextButton(
                                onClick = { otpInput = booking.pickupOtp }
                            ) {
                                Text("Auto-fill Customer's OTP (${booking.pickupOtp})", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            }
                        }

                        Booking.STATUS_IN_TRANSIT -> {
                            Text(
                                text = "Heading to: ${booking.dropTitle}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFF87171)
                            )
                            Text(
                                text = booking.dropAddress,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.completeDelivery(booking.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("driver_complete_delivery_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Complete")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Delivery Completed", fontWeight = FontWeight.Bold)
                            }
                        }

                        Booking.STATUS_DELIVERED -> {
                            Text(
                                text = "Delivery successfully completed! You have earned ₹${(booking.totalFare * 0.85).toInt()}.",
                                fontSize = 13.sp,
                                color = Color(0xFF34D399)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.setDriverSubScreen(DriverSubScreen.DASHBOARD) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Customer Contact Card
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Customer: ${booking.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(text = booking.customerPhone, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    IconButton(
                        onClick = { viewModel.showToast("Calling customer ${booking.customerPhone}...") },
                        modifier = Modifier.clip(CircleShape).background(Color(0xFF10B981).copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
