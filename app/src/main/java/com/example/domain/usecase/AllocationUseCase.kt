package com.example.domain.usecase

import com.example.domain.model.AllocationRequest
import com.example.domain.model.AllocationResult
import com.example.domain.model.DriverAllocationSnapshot
import com.example.domain.model.DriverAvailabilityState
import com.example.domain.model.GeoCoordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.minutes

/**
 * Interface to be implemented by the Data layer (e.g., LogisticsRepository).
 * This keeps the Domain layer 100% independent of databases or network calls.
 */
interface DriverRepository {
    suspend fun getNearbyDrivers(
        location: GeoCoordinate,
        radiusKm: Double,
        vehicleId: String
    ): List<DriverAllocationSnapshot>
}

/**
 * Modernized Use Case for Driver Allocation.
 */
class AllocationUseCase @Inject constructor(
    private val driverRepository: DriverRepository
) {
    /**
     * Finds the best driver match for a given order request.
     * Uses Dispatchers.Default because sorting and Haversine math are CPU-intensive.
     */
    suspend fun findBestDriverMatch(request: AllocationRequest): AllocationResult = withContext(Dispatchers.Default) {
        
        // Step 1: Fetch candidates via Repository (I/O bound behind the interface)
        val nearbyDrivers = driverRepository.getNearbyDrivers(
            location = request.pickupLocation,
            radiusKm = request.maxAllocationRadiusKm,
            vehicleId = request.requiredVehicleId
        )

        if (nearbyDrivers.isEmpty()) {
            return@withContext AllocationResult.NoDriversAvailable(
                orderId = request.orderId,
                reason = "No drivers found within ${request.maxAllocationRadiusKm}km."
            )
        }

        // Step 2: Filter eligible candidates
        val eligibleDrivers = nearbyDrivers.filter { driver ->
            driver.availabilityState == DriverAvailabilityState.ONLINE &&
            driver.rating >= request.minDriverRating &&
            // Future-proofing: Don't assign if phone battery is critically low (< 10%)
            (driver.batteryLevelPercentage == null || driver.batteryLevelPercentage > 10)
        }

        if (eligibleDrivers.isEmpty()) {
            return@withContext AllocationResult.NoDriversAvailable(
                orderId = request.orderId,
                reason = "Drivers found, but none are currently online or meet minimum criteria."
            )
        }

        // Step 3: Score each driver
        val scoredDrivers = eligibleDrivers.mapNotNull { driver ->
            val score = calculateAllocationScore(driver, request.pickupLocation)
            if (score > 0f) Pair(driver, score) else null
        }

        if (scoredDrivers.isEmpty()) {
            return@withContext AllocationResult.NoDriversAvailable(
                orderId = request.orderId,
                reason = "No eligible drivers passed the scoring threshold."
            )
        }

        // Step 4: Sort by score descending
        val sortedCandidates = scoredDrivers.sortedByDescending { it.second }
        
        val bestMatch = sortedCandidates.first()
        val bestDriver = bestMatch.first
        val bestScore = bestMatch.second

        // Step 5: Calculate precise ETA
        val distanceKm = haversineDistanceKm(bestDriver.location, request.pickupLocation)
        
        // Assuming ~15 km/h urban speed = ~4 mins per km
        val estimatedMinutes = (distanceKm * 4.0).toInt().coerceAtLeast(1)
        val arrivalDuration = estimatedMinutes.minutes

        // Step 6: Return fully mapped Success State
        AllocationResult.Success(
            orderId = request.orderId,
            allocatedDriverId = bestDriver.driverId,
            driverSnapshot = bestDriver.copy(
                distanceFromPickupKm = distanceKm,
                estimatedArrival = arrivalDuration
            ),
            allocationScore = bestScore,
            estimatedArrival = arrivalDuration,
            fallbackOptions = sortedCandidates.drop(1).take(3).map { it.first }
        )
    }

    /**
     * Calculates a matching score (0 to 100) for a driver.
     */
    private fun calculateAllocationScore(
        driver: DriverAllocationSnapshot,
        pickupLocation: GeoCoordinate
    ): Float {
        // Base weights: Proximity(40), Rating(25), Acceptance(20), Freshness(10), Battery(5)
        
        val distance = haversineDistanceKm(driver.location, pickupLocation)
        
        // 1. Proximity Score (Max 40)
        val proximityScore = if (distance <= 1.0) 40f else (40f / distance.toFloat()).coerceAtMost(40f)
        
        // 2. Rating Score (Max 25)
        val ratingScore = (driver.rating / 5.0 * 25).toFloat()
        
        // 3. Completion/Acceptance Score (Max 20)
        val completionScore = (driver.acceptanceRate * 20)
        
        // 4. Location Freshness Score (Max 10)
        val millisSinceUpdate = System.currentTimeMillis() - driver.lastLocationUpdateTime
        val freshnessScore = when {
            millisSinceUpdate < 30_000 -> 10f // Under 30 secs
            millisSinceUpdate < 60_000 -> 7f  // Under 1 min
            millisSinceUpdate < 120_000 -> 3f // Under 2 mins
            else -> 0f                        // Stale location
        }
        
        // 5. Hardware/Battery Score (Max 5)
        val batteryScore = when (driver.batteryLevelPercentage) {
            null -> 5f // Assume okay if not reported
            in 50..100 -> 5f
            in 20..49 -> 3f
            else -> 0f
        }

        return (proximityScore + ratingScore + completionScore + freshnessScore + batteryScore)
            .coerceIn(0f, 100f)
    }

    /**
     * Industry-standard Haversine formula using atan2 for numerical stability.
     */
    private fun haversineDistanceKm(coord1: GeoCoordinate, coord2: GeoCoordinate): Double {
        val earthRadiusKm = 6371.0
        
        val dLat = Math.toRadians(coord2.lat - coord1.lat)
        val dLng = Math.toRadians(coord2.lng - coord1.lng)
        
        val lat1 = Math.toRadians(coord1.lat)
        val lat2 = Math.toRadians(coord2.lat)

        val a = sin(dLat / 2).pow(2) + sin(dLng / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadiusKm * c
    }
}
