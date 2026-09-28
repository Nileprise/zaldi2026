package com.example.domain.model

import com.google.android.gms.maps.model.LatLng

// Driver State Machine
enum class DriverAvailabilityState {
    OFFLINE,      // Not working
    ONLINE,       // Active, searching for rides
    BUSY,         // On an active delivery
    ON_BREAK,     // Temporarily unavailable
    LOCATION_STALE // Location data outdated
}

data class DriverLocationPing(
    val driverId: String,
    val lat: Double,
    val lng: Double,
    val accuracy: Float = 10f,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val heading: Float = 0f
)

data class DriverAllocationSnapshot(
    val driverId: String,
    val name: String,
    val phone: String,
    val currentLocation: LatLng,
    val availabilityState: DriverAvailabilityState,
    val currentOrderId: String? = null,
    val rating: Double,
    val totalCompletedTrips: Int,
    val vehicleId: String,
    val vehicleNumber: String,
    val lastLocationUpdateTime: Long,
    val distanceFromPickupKm: Double = 0.0,
    val estimatedArrivalMinutes: Int = 0,
    val acceptanceRate: Float = 0.95f
)

data class AllocationRequest(
    val orderId: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val requiredVehicleId: String,
    val maxAllocationRadiusKm: Double = 5.0,
    val minDriverRating: Double = 3.5,
    val preferredDriverIds: List<String> = emptyList()
)

data class AllocationResult(
    val orderId: String,
    val allocatedDriverId: String,
    val driverSnapshot: DriverAllocationSnapshot,
    val allocationScore: Float, // 0-100, higher is better match
    val estimatedArrivalSeconds: Int,
    val fallbackOptions: List<DriverAllocationSnapshot> = emptyList()
)

data class GeohashCell(
    val geohash: String,
    val level: Int, // precision level
    val driverIds: List<String>,
    val updatedAt: Long
)
