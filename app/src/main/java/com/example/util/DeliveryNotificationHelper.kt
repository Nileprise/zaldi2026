package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.BookingOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modernized Notification Manager designed for Dependency Injection.
 * 
 * Usage in ViewModel/Repository:
 * @Inject constructor(private val notificationManager: DeliveryNotificationManager)
 */
@Singleton
class DeliveryNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "driver_delivery_assignments_channel"
        private const val CHANNEL_NAME = "Driver Order Assignments"
        private const val CHANNEL_DESCRIPTION = "Real-time alerts for assigned freight and delivery requests"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun notifyDriverAssignment(order: BookingOrder, driverName: String) {
        // Check POST_NOTIFICATIONS permission on Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                // Fallback to haptic feedback if UI notifications were disabled by the user
                triggerHapticFeedback()
                return
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TARGET_ROLE", "DRIVER")
            putExtra("EXTRA_ORDER_ID", order.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            order.id.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Safe address parsing prevents out-of-bounds crashes
        val shortPickup = order.pickupAddress.split(",").firstOrNull()?.trim() ?: "Unknown Origin"
        val shortDropoff = order.dropoffAddress.split(",").firstOrNull()?.trim() ?: "Unknown Destination"

        val bigText = buildString {
            append("📦 Cargo: ${order.goodsType}\n")
            append("📍 Pickup: $shortPickup\n")
            append("🎯 Drop-off: $shortDropoff\n")
            append("💰 Earnings: ₹${order.fare.toInt()} (${order.distanceKm} km)\n")
            append("🚚 Vehicle: ${order.vehicleName}\n")
            append("🔑 Start OTP: ${order.startOtp}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // Note: Replace android.R.drawable.ic_dialog_info with your app's custom R.drawable.ic_notification
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("🚚 New Delivery: $driverName")
            .setContentText("Order #${order.id} • ₹${order.fare.toInt()}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setSummaryText("New Freight Assignment")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT) // CATEGORY_EVENT is safer than MESSAGE without a Person object
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_directions,
                "Accept & View Route",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = order.id.hashCode() and 0x7FFFFFFF // Ensure positive integer for ID
        manager?.notify(notificationId, notification)

        // Trigger physical vibration
        triggerHapticFeedback()
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 250, 150, 250),
                        intArrayOf(0, 200, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 250, 150, 250), -1)
                }
            }
        } catch (_: Exception) {
            // Silently ignore if vibration fails (e.g., running on an emulator or device without a motor)
        }
    }
}
