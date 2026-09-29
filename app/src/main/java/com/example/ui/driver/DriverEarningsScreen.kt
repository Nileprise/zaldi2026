package com.example.ui.driver

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.Driver
import com.example.ui.viewmodel.DriverSubScreen
import com.example.ui.viewmodel.ZaldiViewModel

@Composable
fun DriverEarningsScreen(
    viewModel: ZaldiViewModel,
    modifier: Modifier = Modifier
) {
    val currentDriverId by viewModel.currentDriverId.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val driver = allDrivers.firstOrNull { it.id == currentDriverId } ?: Driver(id = "DRV-101", name = "Venkatesh Rao", phone = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("driver_earnings_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setDriverSubScreen(DriverSubScreen.DASHBOARD) },
                modifier = Modifier.testTag("earnings_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Earnings & Payout Ledger",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
        }

        // Wallet Balance Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Available Wallet Balance", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${driver.walletBalance.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.showToast("Payout request of ₹${driver.walletBalance.toInt()} transferred to HDFC Bank A/C ending in 4821!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("request_payout_btn")
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = "Bank", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Bank Payout", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily & Weekly breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Today's Net", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "₹${driver.todayEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "This Week", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "₹${(driver.todayEarnings * 4.5).toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Incentive Challenge Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Daily Peak Incentive", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(text = "Earn ₹300 bonus for completing 6 freight deliveries", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFF334155),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "4 of 6 trips done", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Text(text = "₹300 Bonus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Driver Payouts
        Text(
            text = "Settlement History",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            Triple("Delivered Order #ZD-6219", "₹272 credited", "Yesterday"),
            Triple("Weekly Incentive Target Bonus", "₹500 credited", "3 days ago"),
            Triple("Bank Transfer to HDFC A/C ...4821", "₹2,500 debited", "5 days ago")
        ).forEach { (desc, amt, time) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = desc, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = time, fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Text(
                        text = amt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amt.contains("credited")) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
