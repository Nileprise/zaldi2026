package com.example.util

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

object DeliveryNotificationHelper {

    const val CHANNEL_ID = "driver_delivery_assignments_channel"
    private const val CHANNEL_NAME = "Driver Order Assignments"
    private const val CHANNEL_DESCRIPTION = "Real-time alerts for assigned freight and delivery requests"

    fun initNotificationChannel(context: Context) {
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

    fun notifyDriverAssignment(
        context: Context,
        order: BookingOrder,
        driverName: String
    ) {
        initNotificationChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                // If notification permission not granted, trigger vibration fallback
                triggerHapticFeedback(context)
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

        val bigText = buildString {
            append("📦 Cargo: ${order.goodsType}\n")
            append("📍 Pickup: ${order.pickupAddress.split(",")[0].trim()}\n")
            append("🎯 Drop-off: ${order.dropoffAddress.split(",")[0].trim()}\n")
            append("💰 Earnings / Fare: ₹${order.fare.toInt()} (${order.distanceKm} km)\n")
            append("🚚 Vehicle: ${order.vehicleName}\n")
            append("🔑 Start OTP: ${order.startOtp}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("🚚 New Delivery Assigned: $driverName")
            .setContentText("Order #${order.id} • ${order.goodsType} (₹${order.fare.toInt()})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle("🚚 New Delivery Assigned: $driverName")
                    .setSummaryText("Order #${order.id} • Assigned")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_directions,
                "Accept / View Route",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = (order.id.hashCode() and 0x7FFFFFFF)
        manager?.notify(notificationId, notification)

        // Trigger physical vibration
        triggerHapticFeedback(context)
    }

    private fun triggerHapticFeedback(context: Context) {
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
            // Ignore if vibration fails on emulator/container
        }
    }
}
