package com.example.ui.customer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.LocationPoint
import com.example.data.model.VehicleType
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.ZaldiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerHistoryScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val allBookings by viewModel.allBookings.collectAsState()
    var selectedInvoiceBooking by remember { mutableStateOf<Booking?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp)
            .testTag("customer_history_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setCustomerSubScreen(CustomerSubScreen.HOME) },
                modifier = Modifier.testTag("history_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Booking History & Invoices",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        if (allBookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No booking records yet.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allBookings) { booking ->
                    BookingHistoryCard(
                        booking = booking,
                        onViewInvoice = { selectedInvoiceBooking = booking },
                        onReorder = {
                            viewModel.setSelectedVehicle(
                                VehicleType.values().firstOrNull { it.name == booking.vehicleType } ?: VehicleType.MINI_TRUCK
                            )
                            viewModel.setSelectedPickup(
                                LocationPoint(
                                    id = "re_pickup",
                                    title = booking.pickupTitle,
                                    address = booking.pickupAddress,
                                    latitude = booking.pickupLat,
                                    longitude = booking.pickupLng
                                )
                            )
                            viewModel.setSelectedDrop(
                                LocationPoint(
                                    id = "re_drop",
                                    title = booking.dropTitle,
                                    address = booking.dropAddress,
                                    latitude = booking.dropLat,
                                    longitude = booking.dropLng
                                )
                            )
                            viewModel.setCustomerSubScreen(CustomerSubScreen.HOME)
                            viewModel.showToast("Route loaded for re-booking!")
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Invoice Details Dialog
    if (selectedInvoiceBooking != null) {
        InvoiceDetailsDialog(
            booking = selectedInvoiceBooking!!,
            onDismiss = { selectedInvoiceBooking = null }
        )
    }
}

@Composable
fun BookingHistoryCard(
    booking: Booking,
    onViewInvoice: () -> Unit,
    onReorder: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(booking.createdAt))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth().testTag("history_card_${booking.bookingCode}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER ${booking.bookingCode}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Status chip
                val (statusColor, textColor) = when (booking.status) {
                    Booking.STATUS_DELIVERED -> Color(0xFF10B981).copy(alpha = 0.2f) to Color(0xFF34D399)
                    Booking.STATUS_CANCELLED -> Color(0xFFEF4444).copy(alpha = 0.2f) to Color(0xFFF87171)
                    else -> Color(0xFF0284C7).copy(alpha = 0.2f) to Color(0xFF38BDF8)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route Points
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.pickupTitle, fontSize = 12.sp, color = Color(0xFFE2E8F0), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.dropTitle, fontSize = 12.sp, color = Color(0xFFE2E8F0), maxLines = 1)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rating if given
            if (booking.rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(booking.rating) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                    }
                    if (booking.review.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "\"${booking.review}\"", fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Divider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${booking.totalFare.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "${booking.vehicleType} • ${booking.paymentMethod}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = onViewInvoice,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("view_invoice_btn_${booking.bookingCode}")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = "Invoice", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invoice", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onReorder,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("reorder_btn_${booking.bookingCode}")
                    ) {
                        Text("Rebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailsDialog(
    booking: Booking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tax Invoice: ${booking.bookingCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }
        },
        text = {
            Column {
                Text("Zaldi Technologies Private Limited", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("GSTIN: 29AABCU9603R1ZM", fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Base Freight Fare", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("₹${booking.baseFare.toInt()}", fontSize = 12.sp, color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Distance (${booking.distanceKm} km)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("₹${booking.distanceFare.toInt()}", fontSize = 12.sp, color = Color.White)
                }
                if (booking.helpersFare > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Helpers Loading Fee", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("₹${booking.helpersFare.toInt()}", fontSize = 12.sp, color = Color.White)
                    }
                }
                if (booking.discountFare > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Promocode Discount", fontSize = 12.sp, color = Color(0xFF34D399))
                        Text("-₹${booking.discountFare.toInt()}", fontSize = 12.sp, color = Color(0xFF34D399))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Paid", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("₹${booking.totalFare.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF59E0B))
                }
                Text("Payment Method: ${booking.paymentMethod} (${booking.paymentStatus})", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}
