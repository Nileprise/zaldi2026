package com.example.data.model

import com.google.android.gms.maps.model.LatLng
import kotlin.time.Duration

/**
 * Defines the primary objective for the routing engine.
 */
enum class RouteOptimizationType {
    /** Eco-friendly route: steady speeds, minimal stop-and-go, lower carbon footprint. */
    FUEL_EFFICIENT,
    
    /** Express route: quickest ETA using real-time highway corridors and traffic bypassing. */
    FASTEST
}

/**
 * Type-safe representation of current route congestion.
 * Useful for UI mappers (e.g., Clear = Green polyline, Heavy = Red polyline).
 */
enum class TrafficSeverity {
    CLEAR,
    MODERATE,
    HEAVY,
    SEVERE
}

/**
 * Modernized domain model representing a calculated route option.
 */
data class TrafficRouteOption(
    val id: String,
    val type: RouteOptimizationType,
    val title: String,
    val viaRoad: String,
    
    // 1. Modern Kotlin: Use Duration instead of raw Int/Long for time representation
    val eta: Duration,
    val distanceKm: Double,
    
    // 2. Fuel & Efficiency Metrics
    val fuelSavedLiters: Double = 0.0,
    val fuelEfficiencyRating: String, // e.g., "Optimal (14.2 km/l)"
    
    // 3. Traffic Metadata
    val trafficSeverity: TrafficSeverity = TrafficSeverity.CLEAR,
    val trafficDelay: Duration = Duration.ZERO,
    
    // 4. UI Presentation
    // Note: In strict clean architecture, pre-formatted strings like this are often 
    // generated in the ViewModel/UI layer, but keeping it here is fine for simple apps.
    val savingsText: String, 
    
    // 5. Waypoints
    // Defaults to emptyList() so test mocks don't require giant LatLng arrays
    val waypoints: List<LatLng> = emptyList()
) {
    /**
     * Computed property to cleanly check if the route has delays.
     * Prevents UI layers from doing manual math like `trafficDelay.inWholeMinutes > 0`.
     */
    val hasTrafficDelay: Boolean
        get() = trafficDelay > Duration.ZERO
}
