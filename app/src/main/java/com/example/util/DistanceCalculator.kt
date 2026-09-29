package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteDistanceInfo(
    val distanceKm: Double,
    val estimatedDurationMinutes: Int,
    val viaRoad: String,
    val routeSummary: String
)

/**
 * Modernized, Production-Ready Distance & Routing Calculator.
 * 
 * In an enterprise environment, this acts as a fast, offline fallback heuristic engine
 * when network-based routing APIs (like Google Maps Directions API) fail or time out.
 * It relies purely on coordinate mathematics rather than hardcoded string lookups.
 */
object DistanceCalculator {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the exact straight-line (great-circle) distance between two GPS coordinates.
     */
    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates estimated road distance and ETA based on real GPS coordinates.
     * Applies a dynamic urban sinuosity index to account for road curves and city grids.
     */
    fun calculateRouteInfo(
        pickupLat: Double,
        pickupLng: Double,
        dropoffLat: Double,
        dropoffLng: Double
    ): RouteDistanceInfo {
        
        // 1. Calculate base straight-line distance
        val straightLineKm = haversineDistanceKm(pickupLat, pickupLng, dropoffLat, dropoffLng)

        // 2. Apply Dynamic Sinuosity (Road Detour) Factor
        // Short urban trips have more detours. Long highway trips are straighter.
        val roadFactor = when {
            straightLineKm < 5.0 -> 1.35   // Dense urban city routing
            straightLineKm < 20.0 -> 1.28  // Metro cross-city routing
            straightLineKm < 50.0 -> 1.20  // Suburban/Peripheral routing
            else -> 1.15                   // Highway/Intercity routing
        }

        val actualRoadKm = max(0.1, straightLineKm * roadFactor)
        val roundedKm = (actualRoadKm * 10.0).roundToInt() / 10.0

        // 3. Calculate ETA based on dynamic speed profiles
        val estMinutes = calculateEstimatedMinutes(roundedKm)

        // 4. Generate dynamic via-routing heuristic based on distance magnitude
        val via = when {
            roundedKm > 100.0 -> "National Highway Corridor"
            roundedKm > 35.0 -> "Expressway / Peripheral Ring Road"
            roundedKm > 15.0 -> "Outer Ring Road / Arterial Corridors"
            else -> "Local Commercial Transit Route"
        }

        return RouteDistanceInfo(
            distanceKm = roundedKm,
            estimatedDurationMinutes = estMinutes,
            viaRoad = via,
            routeSummary = "$roundedKm km • ~$estMinutes mins"
        )
    }

    /**
     * Calculates dynamic ETA based on distance and varying urban travel speeds.
     */
    fun calculateEstimatedMinutes(distanceKm: Double): Int {
        // Dynamic speed profile based on distance (km/h)
        val averageSpeedKmh = when {
            distanceKm < 3.0 -> 18.0   // Heavy traffic / local streets
            distanceKm < 10.0 -> 24.0  // Standard city traffic
            distanceKm < 25.0 -> 32.0  // Arterial roads
            distanceKm < 60.0 -> 45.0  // Expressways
            else -> 60.0               // Highways
        }

        // Time = Distance / Speed * 60 (to get minutes)
        val baseMinutes = (distanceKm / averageSpeedKmh) * 60.0
        
        // Add fixed buffer for pickup/dropoff logistics (parking, loading, customer verification, etc.)
        val logisticsBufferMinutes = 6
        
        return max(5, baseMinutes.roundToInt() + logisticsBufferMinutes)
    }

    /**
     * DEPRECATED: Legacy string-based calculation.
     * In a production app, address strings cannot be mathematically calculated without making 
     * a network call to a Geocoder. 
     * 
     * Addresses MUST be geocoded to LatLng first using Google Places API in your ViewModel,
     * and then passed to the [calculateRouteInfo] coordinate function above.
     * 
     * Provided here ONLY to prevent breaking legacy UI components during your migration.
     */
    @Deprecated("Use coordinate-based calculateRouteInfo instead. Geocoding must happen at the ViewModel layer.")
    fun calculateExactDistance(pickupAddress: String, dropoffAddress: String): RouteDistanceInfo {
        if (pickupAddress.trim().equals(dropoffAddress.trim(), ignoreCase = true)) {
            return RouteDistanceInfo(
                distanceKm = 0.0,
                estimatedDurationMinutes = 0,
                viaRoad = "Same Location",
                routeSummary = "0 km • 0 mins"
            )
        }
        
        // Safety Fallback if legacy UI components still call this before geocoding completes
        return RouteDistanceInfo(
            distanceKm = 0.0,
            estimatedDurationMinutes = 0,
            viaRoad = "Pending GPS resolution",
            routeSummary = "Pending GPS resolution"
        )
    }
}
