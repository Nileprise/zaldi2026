package com.example.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Pure Kotlin representation of coordinates.
 * DO NOT use Google's LatLng in the domain layer to maintain Clean Architecture.
 */
data class GeoCoordinate(
    val lat: Double,
    val lng: Double
)

/**
 * Type-safe state machine for driver availability.
 */
enum class DriverAvailabilityState {
    OFFLINE,        // Not working
    ONLINE,         // Active, searching for rides
    BUSY,           // On an active delivery
    ON_BREAK,       // Temporarily unavailable
    LOCATION_STALE, // Location data outdated (e.g., GPS signal lost)
    SUSPENDED       // Future-proofing: Admin/System suspension
}

/**
 * Represents a raw GPS ping from the driver's device.
 */
data class DriverLocationPing(
    val driverId: String,
    val location: GeoCoordinate,
    val accuracyMeters: Float = 10f,
    val speedKmh: Float = 0f,
    val headingDegrees: Float = 0f,
    // Future-proofing: Crucial for dispatch algorithms to avoid assigning 
    // long trips to a driver whose device is about to die.
    val batteryLevelPercentage: Int? = null, 
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * A frozen-in-time snapshot of a driver's state used by the Allocation Engine 
 * to score and rank candidates.
 */
data class DriverAllocationSnapshot(
    val driverId: String,
    val name: String,
    val phone: String,
    val location: GeoCoordinate,
    val availabilityState: DriverAvailabilityState,
    val currentOrderId: String? = null,
    val rating: Double,
    val totalCompletedTrips: Int,
    val vehicleId: String,
    val vehicleNumber: String,
    val lastLocationUpdateTime: Long,
    val distanceFromPickupKm: Double = 0.0,
    val estimatedArrival: Duration, // Modernized: Replaced Int minutes with Duration
    val acceptanceRate: Float = 0.95f
)

/**
 * The criteria sent to the matching engine to find the best driver.
 */
data class AllocationRequest(
    val orderId: String,
    val pickupLocation: GeoCoordinate,
    val dropoffLocation: GeoCoordinate,
    val requiredVehicleId: String,
    val maxAllocationRadiusKm: Double = 5.0,
    val minDriverRating: Double = 3.5,
    val preferredDriverIds: List<String> = emptyList(),
    // Future-proofing: Allocation requests should always have a TTL (Time-To-Live)
    val timeout: Duration = 30.seconds 
)

/**
 * Modernized Result Wrapper.
 * Forces the caller (ViewModel/UseCase) to handle failures explicitly 
 * rather than relying on nullable returns.
 */
sealed interface AllocationResult {
    data class Success(
        val orderId: String,
        val allocatedDriverId: String,
        val driverSnapshot: DriverAllocationSnapshot,
        val allocationScore: Float, // 0-100, higher is better match
        val estimatedArrival: Duration,
        val fallbackOptions: List<DriverAllocationSnapshot> = emptyList()
    ) : AllocationResult

    data class NoDriversAvailable(
        val orderId: String, 
        val reason: String = "No drivers found within the radius"
    ) : AllocationResult
    
    data class Timeout(
        val orderId: String
    ) : AllocationResult
}

/**
 * Spatial index cell for ultra-fast geospatial driver querying (e.g., Redis).
 */
data class GeohashCell(
    val geohash: String,
    val level: Int, // precision level (e.g., 6 for ~1.2km x 600m)
    val driverIds: List<String>,
    val updatedAt: Long = System.currentTimeMillis()
)
