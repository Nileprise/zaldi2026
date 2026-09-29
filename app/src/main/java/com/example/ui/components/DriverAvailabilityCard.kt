package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DriverAvailabilityCard(
    modifier: Modifier = Modifier,
    driverId: String? = null,
    driverName: String = "Unknown Driver",
    statusText: String = "Available",
    onClick: (() -> Unit)? = null
) {
    // Material 3 OutlinedCard natively handles the 0-elevation and border structural design
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    // A11y: Explicitly define the role as a Button for screen readers
                    Modifier.clickable(role = Role.Button) { onClick() }
                } else {
                    Modifier
                }
            )
            .testTag(
                driverId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "driver_card_$it" } 
                    ?: "driver_availability_card"
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = SurfaceCard // Ensure this is defined in your Theme
        ),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Left Section: Driver Info ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Replaced Amber with AccentBlue as requested
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null, // Decorative icon
                        tint = AccentBlue
                    )
                }

                Column {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!driverId.isNullOrBlank()) {
                        Text(
                            text = "ID: $driverId",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Right Section: Status ---
            Text(
                text = statusText.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                modifier = Modifier
                    .background(
                        color = AccentBlueContainer,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ============================================================================
// Theme Placeholders (Replace with your actual theme colors)
// ============================================================================
private val SurfaceCard = Color(0xFFFFFFFF)
private val BorderLight = Color(0xFFE5E7EB)
private val AccentBlue = Color(0xFF2563EB)
private val AccentBlueContainer = Color(0xFFDBEAFE)

// ============================================================================
// UI Previews
// ============================================================================

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DriverAvailabilityCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverAvailabilityCard(
                driverId = "DRV-101",
                driverName = "Ravi Kumar",
                statusText = "Online",
                onClick = {}
            )
            
            DriverAvailabilityCard(
                driverId = null,
                driverName = "Assign Driver",
                statusText = "Pending",
                onClick = null
            )
        }
    }
}
