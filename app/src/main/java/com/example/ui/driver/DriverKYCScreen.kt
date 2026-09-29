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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun DriverKYCScreen(
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
            .testTag("driver_kyc_screen")
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
                modifier = Modifier.testTag("kyc_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Partner KYC & Vehicle Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
        }

        // KYC Status Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "KYC Verification: ${driver.kycStatus}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Your documents are compliant with transport authority safety standards.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Document Cards
        Text(
            text = "Verified Documents",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocItem(
            icon = Icons.Default.PermIdentity,
            title = "Commercial Driving License (DL)",
            number = "KA0120190048211",
            validity = "Valid until 12 Oct 2028"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DocItem(
            icon = Icons.Default.DirectionsCar,
            title = "Vehicle Registration (RC Book)",
            number = driver.vehiclePlate,
            validity = "${driver.vehicleModel} • Fitness Valid"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DocItem(
            icon = Icons.Default.Description,
            title = "Goods Commercial Vehicle Insurance",
            number = "POL-9842109281",
            validity = "Comprehensive Cover • Active"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DocItem(
            icon = Icons.Default.VerifiedUser,
            title = "Identity (Aadhaar & PAN)",
            number = "XXXX-XXXX-4819",
            validity = "Biometric Verified"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.showToast("All documents verified and up to date.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Update Documents", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DocItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    number: String,
    validity: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                Text(text = number, fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                Text(text = validity, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
        }
    }
}
