package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.DriverLocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Modernized Foreground Service for Driver Telemetry.
 * Compliant with Android 14+ strict location service rules.
 */
class DriverLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var simulationJob: Job? = null
    
    // Modern Coroutine Scope: Use SupervisorJob so a single failure doesn't crash the service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Future-proofing: State to track if driver is actively delivering (high accuracy needed) 
    // vs just waiting for orders (balanced power needed).
    private var isActivelyDelivering = false

    companion object {
        private const val TAG = "DriverLocationService"
        const val ACTION_START = "ACTION_START_LOCATION_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_SERVICE"
        const val ACTION_SET_DELIVERING_STATE = "ACTION_SET_DELIVERING_STATE"
        const val EXTRA_IS_DELIVERING = "EXTRA_IS_DELIVERING"
        
        const val NOTIFICATION_CHANNEL_ID = "driver_telemetry_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTelemetryService()
            ACTION_STOP -> stopTelemetryService()
            ACTION_SET_DELIVERING_STATE -> {
                val newState = intent.getBooleanExtra(EXTRA_IS_DELIVERING, false)
                if (newState != isActivelyDelivering) {
                    isActivelyDelivering = newState
                    restartLocationUpdatesWithOptimalPower()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        // Android 14+ requires explicit background tracking permissions for foreground services
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        return (fine || coarse) && background
    }

    private fun startTelemetryService() {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Insufficient location permissions. Stopping service.")
            stopSelf()
            return
        }

        LocationManager.setServiceRunning(true)

        val notification = createNotification(
            title = "Akhil Logistics • Driver GPS Live",
            content = "Transmitting real-time GPS telemetry to dispatch"
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
            stopSelf()
            return
        }

        requestFusedLocationUpdates()
        
        // Start simulation ONLY in debug/demo mode
        // if (BuildConfig.DEBUG) startSimulationFallback() 
    }

    private fun stopTelemetryService() {
        LocationManager.setServiceRunning(false)
        stopFusedLocationUpdates()
        
        simulationJob?.cancel()
        simulationJob = null
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground: ${e.message}")
        }
        
        stopSelf()
    }

    private fun restartLocationUpdatesWithOptimalPower() {
        if (locationCallback != null) {
            stopFusedLocationUpdates()
            requestFusedLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFusedLocationUpdates() {
        if (!hasLocationPermission()) return

        // Modern Battery Optimization: 
        // If delivering, update every 4 secs. If just idling online, update every 15 secs to save battery.
        val intervalMillis = if (isActivelyDelivering) 4000L else 15000L
        val priority = if (isActivelyDelivering) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val locationRequest = LocationRequest.Builder(priority, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setMinUpdateDistanceMeters(if (isActivelyDelivering) 2.0f else 10.0f)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    handleNewLocation(loc, isLive = true)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates: ${e.message}")
        }
    }

    private fun stopFusedLocationUpdates() {
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove location updates: ${e.message}")
            }
            locationCallback = null
        }
    }

    private fun handleNewLocation(location: Location, isLive: Boolean) {
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0.0f
        
        val telemetry = DriverLocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = (speedKmh * 10).toInt() / 10f, // Rounded to 1 decimal
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 10f,
            altitude = location.altitude,
            bearing = if (location.hasBearing()) location.bearing else 0f,
            timestamp = System.currentTimeMillis(),
            isLiveGps = isLive
        )
        
        // 1. Update internal state
        LocationManager.updateLocation(telemetry)
        
        // 2. Publish to backend (RealtimeDatabase / Kafka via Repository)
        // In a real app, you would inject a repository here to send this to the cloud:
        // locationRepository.publishLiveTelemetry(telemetry)

        // 3. Update the persistent notification so Android doesn't kill the service
        try {
            val statusText = if (isActivelyDelivering) "In Transit" else "Online & Waiting"
            val updatedNotification = createNotification(
                title = "Driver GPS Active • $statusText",
                content = "${telemetry.formattedCoordinates} • ${telemetry.speedKmh} km/h"
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, updatedNotification)
        } catch (e: Exception) {
             Log.e(TAG, "Failed to update notification: ${e.message}")
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        // Modern Intent routing
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Keep low to prevent constant sound/vibration
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Driver Live Telemetry",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background GPS location tracking for dispatch operations"
                setShowBadge(false) // Don't clutter the app icon with a badge for an ongoing service
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopTelemetryService()
        serviceScope.cancel() // Clean up coroutines strictly to prevent memory leaks
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
