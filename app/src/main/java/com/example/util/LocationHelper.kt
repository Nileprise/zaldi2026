package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.LocationPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (LocationPoint) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted. Please allow location access to use current GPS position.")
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancelToken = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelToken.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val resolvedPoint = reverseGeocode(context, location.latitude, location.longitude)
                        onSuccess(resolvedPoint)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                val resolvedPoint = reverseGeocode(context, lastLoc.latitude, lastLoc.longitude)
                                onSuccess(resolvedPoint)
                            } else {
                                // Graceful fallback to Central Logistics Hub
                                onSuccess(LocationPoint.PRESET_LOCATIONS[3])
                            }
                        }.addOnFailureListener {
                            onSuccess(LocationPoint.PRESET_LOCATIONS[3])
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Graceful fallback to Central Logistics Hub
                    onSuccess(LocationPoint.PRESET_LOCATIONS[3])
                }
        } catch (ex: Exception) {
            onError("Unable to access GPS: ${ex.localizedMessage}")
        }
    }

    fun reverseGeocode(context: Context, latitude: Double, longitude: Double): LocationPoint {
        try {
            val geocoder = Geocoder(context, Locale("en", "IN"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.featureName ?: "Current Location"
                    val subLocality = addr.subLocality ?: addr.locality ?: "Central Zone"
                    val city = addr.locality ?: "Bengaluru"
                    val state = addr.adminArea ?: "Karnataka"
                    val postal = addr.postalCode ?: "560001"
                    val full = addr.getAddressLine(0) ?: "$feature, $subLocality, $city"

                    return LocationPoint(
                        id = "gps_${System.currentTimeMillis()}",
                        title = if (feature.length < 25) feature else subLocality,
                        address = full,
                        latitude = latitude,
                        longitude = longitude,
                        category = "GPS Live Location",
                        area = subLocality,
                        city = city,
                        state = state,
                        pincode = postal,
                        landmark = "Near Current GPS Point"
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.featureName ?: "Current Location"
                    val subLocality = addr.subLocality ?: addr.locality ?: "Central Zone"
                    val city = addr.locality ?: "Bengaluru"
                    val state = addr.adminArea ?: "Karnataka"
                    val postal = addr.postalCode ?: "560001"
                    val full = addr.getAddressLine(0) ?: "$feature, $subLocality, $city"

                    return LocationPoint(
                        id = "gps_${System.currentTimeMillis()}",
                        title = if (feature.length < 25) feature else subLocality,
                        address = full,
                        latitude = latitude,
                        longitude = longitude,
                        category = "GPS Live Location",
                        area = subLocality,
                        city = city,
                        state = state,
                        pincode = postal,
                        landmark = "Near Current GPS Point"
                    )
                }
            }
        } catch (_: Exception) {
            // Network or geocoder service not responding
        }

        // Find closest preset Indian logistics location if geocoder fails
        var closest = LocationPoint.PRESET_LOCATIONS[0]
        var minDistance = Double.MAX_VALUE
        for (preset in LocationPoint.PRESET_LOCATIONS) {
            val dist = LocationPoint.calculateDistanceKm(latitude, longitude, preset.latitude, preset.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closest = preset
            }
        }

        return LocationPoint(
            id = "gps_closest_${System.currentTimeMillis()}",
            title = "My Location (${closest.area})",
            address = "Near ${closest.title}, ${closest.address}",
            latitude = latitude,
            longitude = longitude,
            category = "GPS Live Location",
            area = closest.area,
            city = closest.city,
            state = closest.state,
            pincode = closest.pincode,
            landmark = closest.landmark
        )
    }
}
