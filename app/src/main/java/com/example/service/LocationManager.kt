package com.example.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.DriverLocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocationManager {
    private val _currentLocation = MutableStateFlow<DriverLocationData?>(
        DriverLocationData(
            latitude = 12.9784,
            longitude = 77.6408,
            speedKmh = 24.5f,
            accuracyMeters = 3.2f,
            altitude = 918.0,
            bearing = 45.0f,
            isLiveGps = true
        )
    )
    val currentLocation: StateFlow<DriverLocationData?> = _currentLocation.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun updateLocation(location: DriverLocationData) {
        _currentLocation.value = location
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun startLocationService(context: Context) {
        if (!hasLocationPermission(context)) {
            // Cannot start location foreground service without location permissions on Android 14+
            return
        }

        try {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = DriverLocationService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationService(context: Context) {
        try {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = DriverLocationService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
