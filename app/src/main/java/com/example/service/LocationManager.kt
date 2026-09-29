package com.example.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.DriverLocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modernized Location Manager.
 * Designed to be provided as a Singleton via Dependency Injection (e.g., Hilt).
 * 
 * Example Hilt Module:
 * @Provides
 * @Singleton
 * fun provideLocationManager(@ApplicationContext context: Context) = LocationManager(context)
 */
@Singleton
class LocationManager @Inject constructor(
    private val context: Context // Must be ApplicationContext to prevent memory leaks
) {
    companion object {
        private const val TAG = "LocationManager"
    }

    // Default to null. Never hardcode mock coordinates in production state managers.
    private val _currentLocation = MutableStateFlow<DriverLocationData?>(null)
    val currentLocation: StateFlow<DriverLocationData?> = _currentLocation.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    /**
     * Checks all mandatory permissions required to run a Location Foreground Service 
     * on modern Android versions (13+ & 14+).
     */
    fun hasRequiredPermissions(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Android 13+ requires notification permissions to run Foreground Services safely
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return (fineGranted || coarseGranted) && notificationsGranted
    }

    /**
     * Modern thread-safe update using .update {}
     */
    fun updateLocation(location: DriverLocationData) {
        _currentLocation.update { location }
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.update { running }
        
        // Clear location state if service is stopped to prevent stale UI data
        if (!running) {
            _currentLocation.update { null }
        }
    }

    fun startLocationService() {
        if (!hasRequiredPermissions()) {
            Log.e(TAG, "Cannot start location service: Missing required permissions.")
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
            Log.e(TAG, "Failed to start location service", e)
        }
    }

    fun stopLocationService() {
        try {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = DriverLocationService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop location service", e)
        }
    }

    /**
     * Updates the service's battery profile based on driver state.
     * Integrates with the optimized DriverLocationService.
     */
    fun setDeliveringState(isActivelyDelivering: Boolean) {
        if (!_isServiceRunning.value) return
        
        try {
            val intent = Intent(context, DriverLocationService::class.java).apply {
                action = DriverLocationService.ACTION_SET_DELIVERING_STATE
                putExtra(DriverLocationService.EXTRA_IS_DELIVERING, isActivelyDelivering)
            }
            context.startService(intent) // startService delivers the intent to onStartCommand
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update delivering state", e)
        }
    }
}
