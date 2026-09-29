package com.example.ui.customer

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CustomerSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

@Composable
fun CustomerPaymentScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val activeBooking by viewModel.activeCustomerBooking.collectAsState()
    var selectedMethod by remember { mutableStateOf("UPI") }

    if (activeBooking == null) {
        viewModel.setCustomerSubScreen(CustomerSubScreen.HOME)
        return
    }

    val booking = activeBooking!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("customer_payment_screen")
    ) {
        // Payment Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Delivered",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Shipment Delivered!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Text(
                    text = "Order #${booking.bookingCode} • ${booking.goodsType}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "₹${booking.totalFare.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = Color(0xFFF59E0B)
                )
                Text(
                    text = "Total Amount Payable",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Options
        Text(
            text = "Select Payment Method",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        val methods = listOf(
            Triple("UPI", "Instant UPI (Google Pay, PhonePe, Paytm)", Icons.Default.QrCode2),
            Triple("CARD", "Credit / Debit Card (Visa, Mastercard, RuPay)", Icons.Default.CreditCard),
            Triple("WALLET", "Zaldi Fast Wallet (Balance: ₹1,450)", Icons.Default.AccountBalanceWallet),
            Triple("CASH", "Cash on Delivery (Pay directly to driver)", Icons.Default.Money)
        )

        methods.forEach { (key, label, icon) ->
            val isSelected = selectedMethod == key
            Card(
                onClick = { selectedMethod = key },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31)
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("payment_method_$key")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = key,
                        tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = key,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedMethod = key },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF59E0B))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Itemized Invoice Summary
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth().testTag("payment_invoice_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Official Invoice Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Base Booking Fare", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text("₹${booking.baseFare.toInt()}", fontSize = 12.sp, color = Color.White)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Distance Traveled (${booking.distanceKm} km)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text("₹${booking.distanceFare.toInt()}", fontSize = 12.sp, color = Color.White)
                }
                if (booking.helpersFare > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Loading Helpers (${booking.helperCount})", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("₹${booking.helpersFare.toInt()}", fontSize = 12.sp, color = Color.White)
                    }
                }
                if (booking.discountFare > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Coupon Discount", fontSize = 12.sp, color = Color(0xFF34D399))
                        Text("-₹${booking.discountFare.toInt()}", fontSize = 12.sp, color = Color(0xFF34D399))
                    }
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Final Charge", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("₹${booking.totalFare.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFF59E0B))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm & Pay Button
        Button(
            onClick = { viewModel.processPayment(booking.id, selectedMethod) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_payment_button")
        ) {
            Icon(Icons.Default.Shield, contentDescription = "Secure")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pay Securely ₹${booking.totalFare.toInt()} via $selectedMethod", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
