package com.example.data.model

import com.google.android.gms.maps.model.LatLng

enum class RouteOptimizationType {
    FUEL_EFFICIENT, // Eco-friendly route: steady speeds, minimal stop-and-go, lower carbon footprint
    FASTEST         // Express route: quickest ETA using real-time highway corridors and traffic bypassing
}

data class TrafficRouteOption(
    val id: String,
    val type: RouteOptimizationType,
    val title: String,
    val viaRoad: String,
    val etaMinutes: Int,
    val distanceKm: Double,
    val fuelSavedLiters: Double,
    val fuelEfficiencyRating: String, // e.g. "Optimal (14.2 km/l)", "Normal (11.5 km/l)"
    val trafficCondition: String,      // e.g. "Clear & Fluid Traffic", "Moderate Congestion (+3m)"
    val trafficDelayMinutes: Int,
    val savingsText: String,           // e.g. "Saves ₹85 fuel & 0.4L diesel", "Arrives 7 mins earlier"
    val waypoints: List<LatLng>
)
