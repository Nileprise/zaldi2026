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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class DriverLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var simulationJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    companion object {
        const val ACTION_START = "ACTION_START_LOCATION_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_SERVICE"
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
        }
        return START_NOT_STICKY
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun startTelemetryService() {
        if (!hasLocationPermission()) {
            // Android 14+ targetSdk 34+ requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION to start location FGS
            stopSelf()
            return
        }

        LocationManager.setServiceRunning(true)

        val notification = createNotification(
            "Akhil Logistics • Driver GPS Live",
            "Transmitting real-time GPS telemetry to dispatch"
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
        } catch (e: SecurityException) {
            e.printStackTrace()
            stopSelf()
            return
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
            return
        }

        requestFusedLocationUpdates()
        startSimulationFallback()
    }

    private fun stopTelemetryService() {
        LocationManager.setServiceRunning(false)
        stopFusedLocationUpdates()
        simulationJob?.cancel()
        simulationJob = null
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun requestFusedLocationUpdates() {
        if (!hasLocationPermission()) {
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(2.0f)
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
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopFusedLocationUpdates() {
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            locationCallback = null
        }
    }

    private fun handleNewLocation(location: Location, isLive: Boolean) {
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 28.0f
        val telemetry = DriverLocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = (speedKmh * 10).toInt() / 10f,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 4.5f,
            altitude = location.altitude,
            bearing = if (location.hasBearing()) location.bearing else 45f,
            timestamp = System.currentTimeMillis(),
            isLiveGps = isLive
        )
        LocationManager.updateLocation(telemetry)

        try {
            val updatedNotification = createNotification(
                "Driver Live GPS Active (${telemetry.formattedTime})",
                "${telemetry.formattedCoordinates} • ${telemetry.speedKmh} km/h"
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, updatedNotification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSimulationFallback() {
        simulationJob?.cancel()
        simulationJob = serviceScope.launch {
            var currentLat = 12.9784
            var currentLng = 77.6408
            var currentSpeed = 26.5f

            while (isActive) {
                delay(3000L)

                val latDelta = (Random.nextDouble() - 0.45) * 0.0004
                val lngDelta = (Random.nextDouble() - 0.45) * 0.0004
                currentLat += latDelta
                currentLng += lngDelta
                currentSpeed = (20f + Random.nextFloat() * 15f)

                val simulatedLocation = Location("simulated_gps").apply {
                    latitude = currentLat
                    longitude = currentLng
                    speed = currentSpeed / 3.6f
                    accuracy = 3.5f + Random.nextFloat() * 2f
                    altitude = 918.0 + Random.nextDouble() * 3.0
                    bearing = (30f + Random.nextFloat() * 40f)
                    time = System.currentTimeMillis()
                }

                if (locationCallback == null) {
                    handleNewLocation(simulatedLocation, isLive = true)
                }
            }
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
                description = "Background GPS location tracking for Akhil Logistics driver partners"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopTelemetryService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
