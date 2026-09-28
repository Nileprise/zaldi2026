package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverLocationData
import com.example.ui.components.GoogleMapsView
import com.example.ui.components.SimulatedMapView
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsBlueContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.google.android.gms.maps.model.LatLng

@Composable
fun LiveMapScreen(
    driverLocation: DriverLocationData?,
    isServiceRunning: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGoogleMapsMode by remember { mutableStateOf(true) }

    val pickupCoords = LatLng(12.9784, 77.6408) // Indiranagar
    val dropoffCoords = LatLng(12.9352, 77.6245) // Koramangala

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        if (isGoogleMapsMode) {
            GoogleMapsView(
                driverLocation = driverLocation,
                modifier = Modifier.fillMaxSize(),
                pickupLatLng = pickupCoords,
                dropoffLatLng = dropoffCoords,
                pickupTitle = "Indiranagar 100ft Rd",
                dropoffTitle = "Koramangala 4th Block",
                pickupPlaceId = "ChIJbU60qSX9vzsR0whvgm0FmWg",
                dropoffPlaceId = "ChIJL_7_4sBkrjsR9r2jCskd81c",
                isInteractive = true
            )
        } else {
            SimulatedMapView(
                driverLocation = driverLocation,
                modifier = Modifier.fillMaxSize(),
                isTrackingActiveRide = true,
                pickupName = "Indiranagar 100ft Rd",
                dropoffName = "Koramangala 4th Block",
                etaMinutes = 11
            )
        }

        // Top Header Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("live_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "Live Fleet Telemetry",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (isServiceRunning) SuccessGreen else AmberPrimary, CircleShape)
                            )
                            Text(
                                text = if (isServiceRunning) " Background GPS Streaming Live" else " Live GPS Provider",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Map Style Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isGoogleMapsMode) AmberPrimary else Color.Transparent,
                        modifier = Modifier.clickable { isGoogleMapsMode = true }
                    ) {
                        Text(
                            text = "SDK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoogleMapsMode) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isGoogleMapsMode) AmberPrimary else Color.Transparent,
                        modifier = Modifier.clickable { isGoogleMapsMode = false }
                    ) {
                        Text(
                            text = "Vector",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isGoogleMapsMode) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Bottom Telemetry HUD Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROADS API SNAP-TO-ROADS TELEMETRY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Ravi Kumar • Tata Ace KA 05 MX 2190",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessContainer
                    ) {
                        Text(
                            text = "Active Trip",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Speed", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${driverLocation?.speedKmh?.toInt() ?: 24} km/h",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Bearing",
                            tint = LogisticsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Heading", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${driverLocation?.bearing?.toInt() ?: 145}° SE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Accuracy",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "GPS Accuracy", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "±${driverLocation?.accuracyMeters?.toInt() ?: 8}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }
    }
}
