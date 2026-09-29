package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppRole
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Creates and remembers the required runtime permissions for Zaldi logistics:
 * Precise GPS Location + Coarse Location + Post Notifications (Android 13+).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberZaldiPermissionsState(): MultiplePermissionsState {
    val permissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    return rememberMultiplePermissionsState(permissions = permissions)
}

/**
 * Helper to check if location permission (fine or coarse) is granted.
 */
@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isLocationGranted(): Boolean {
    return permissions.any {
        (it.permission == Manifest.permission.ACCESS_FINE_LOCATION ||
                it.permission == Manifest.permission.ACCESS_COARSE_LOCATION) && it.status.isGranted
    }
}

/**
 * Helper to check if notification permission is granted (or not required on < API 33).
 */
@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isNotificationGranted(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    val notifPermission = permissions.firstOrNull { it.permission == Manifest.permission.POST_NOTIFICATIONS }
    return notifPermission?.status?.isGranted ?: true
}

/**
 * Reusable banner prompting users or drivers to grant runtime Location and Notification permissions.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ZaldiPermissionBanner(
    permissionsState: MultiplePermissionsState,
    role: AppRole,
    modifier: Modifier = Modifier,
    onPermissionsGranted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isDismissed by remember { mutableStateOf(false) }

    val locationGranted = permissionsState.isLocationGranted()
    val notificationGranted = permissionsState.isNotificationGranted()
    val allGranted = permissionsState.allPermissionsGranted || (locationGranted && notificationGranted)

    if (allGranted) {
        onPermissionsGranted?.invoke()
        return
    }

    AnimatedVisibility(
        visible = !isDismissed && !allGranted,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (role == AppRole.DRIVER) Color(0xFF1E293B) else Color(0xFF1E293B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("runtime_permission_banner")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (role == AppRole.DRIVER) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (role == AppRole.DRIVER) Icons.Default.WarningAmber else Icons.Default.Security,
                                contentDescription = "Permissions Required",
                                tint = if (role == AppRole.DRIVER) Color(0xFFF59E0B) else Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (role == AppRole.DRIVER) "Driver Permissions Required" else "Enable Location & Live Alerts",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (role == AppRole.DRIVER) "Mandatory for live dispatch & routing" else "For precise pickup & delivery tracking",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (role != AppRole.DRIVER) {
                        IconButton(
                            onClick = { isDismissed = true },
                            modifier = Modifier.size(32.dp).testTag("dismiss_permission_banner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermissionStatusChip(
                        icon = Icons.Default.LocationOn,
                        label = "GPS Location",
                        isGranted = locationGranted,
                        modifier = Modifier.weight(1f)
                    )
                    PermissionStatusChip(
                        icon = Icons.Default.NotificationsActive,
                        label = "Notifications",
                        isGranted = notificationGranted,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (role == AppRole.DRIVER) {
                        "Zaldi Driver requires continuous background GPS telemetry to match you with nearby high-paying commercial loads and sound alarms for dispatch requests."
                    } else {
                        "Allow GPS to auto-detect your logistics pickup hubs in Bangalore, Hyderabad, or Mumbai, and notifications for OTP verification & vehicle arrival."
                    },
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (permissionsState.shouldShowRationale) {
                        // User previously denied, explain and open settings or retry
                        OutlinedButton(
                            onClick = { openAppSettings(context) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.weight(1f).testTag("open_settings_permission_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Settings", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            permissionsState.launchMultiplePermissionRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (role == AppRole.DRIVER) Color(0xFFF59E0B) else Color(0xFF2563EB),
                            contentColor = if (role == AppRole.DRIVER) Color(0xFF0F172A) else Color.White
                        ),
                        modifier = Modifier
                            .weight(if (permissionsState.shouldShowRationale) 1.5f else 1f)
                            .testTag("grant_permissions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (permissionsState.shouldShowRationale) "Grant in Settings / Allow" else "Allow Permissions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isGranted) Color(0xFF065F46).copy(alpha = 0.4f) else Color(0xFF7F1D1D).copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF34D399) else Color(0xFFF87171),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: ${if (isGranted) "Granted" else "Needed"}",
                color = if (isGranted) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Opens system application details settings in case permissions are permanently denied.
 */
private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
