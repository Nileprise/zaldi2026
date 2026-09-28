package com.example.domain.usecase

import com.example.domain.model.AllocationRequest
import com.example.domain.model.AllocationResult
import com.example.domain.model.DriverAllocationSnapshot
import com.example.domain.model.DriverAvailabilityState
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class AllocationUseCase {
    
    fun findBestDriverMatch(request: AllocationRequest): AllocationResult? {
        // Step 1: Get nearby drivers using geohashing
        val nearbyDrivers = getNearbyDrivers(
            lat = request.pickupLat,
            lng = request.pickupLng,
            radiusKm = request.maxAllocationRadiusKm,
            vehicleId = request.requiredVehicleId
        )
        
        if (nearbyDrivers.isEmpty()) {
            return null // No drivers available
        }
        
        // Step 2: Filter by availability state
        val availableDrivers = nearbyDrivers.filter {
            it.availabilityState == DriverAvailabilityState.ONLINE
        }
        
        if (availableDrivers.isEmpty()) {
            return null
        }
        
        // Step 3: Score each driver
        val scoredDrivers = availableDrivers.map { driver ->
            Pair(
                driver,
                calculateAllocationScore(
                    driver = driver,
                    pickupLat = request.pickupLat,
                    pickupLng = request.pickupLng,
                    minRating = request.minDriverRating
                )
            )
        }.filter { (_, score) -> score > 0f } // Filter out ineligible drivers
        
        if (scoredDrivers.isEmpty()) {
            return null
        }
        
        // Step 4: Sort by score and pick best
        val sorted = scoredDrivers.sortByDescending { it.second }
        val bestDriver = sorted[0].first
        val bestScore = sorted[0].second
        
        // Step 5: Calculate arrival time
        val distanceKm = haversineDistance(
            request.pickupLat, request.pickupLng,
            bestDriver.currentLocation.latitude, bestDriver.currentLocation.longitude
        )
        val arrivalMinutes = (distanceKm * 4).toInt() // Rough estimate: 4 min per km
        
        return AllocationResult(
            orderId = request.orderId,
            allocatedDriverId = bestDriver.driverId,
            driverSnapshot = bestDriver.copy(
                distanceFromPickupKm = distanceKm,
                estimatedArrivalMinutes = arrivalMinutes
            ),
            allocationScore = bestScore,
            estimatedArrivalSeconds = arrivalMinutes * 60,
            fallbackOptions = sorted.drop(1).map { it.first }.take(3)
        )
    }
    
    private fun getNearbyDrivers(
        lat: Double,
        lng: Double,
        radiusKm: Double,
        vehicleId: String
    ): List<DriverAllocationSnapshot> {
        // In production, this would:
        // 1. Query Redis geospatial index with geohash
        // 2. Filter by vehicle type
        // 3. Return with location data
        
        // Mock implementation
        return listOf(
            DriverAllocationSnapshot(
                driverId = "DRV-101",
                name = "Ravi Kumar",
                phone = "+91 98452 11094",
                currentLocation = com.google.android.gms.maps.model.LatLng(lat + 0.01, lng + 0.01),
                availabilityState = DriverAvailabilityState.ONLINE,
                rating = 4.88,
                totalCompletedTrips = 2340,
                vehicleId = vehicleId,
                vehicleNumber = "KA 05 MX 2190",
                lastLocationUpdateTime = System.currentTimeMillis()
            ),
            DriverAllocationSnapshot(
                driverId = "DRV-102",
                name = "Amit Singh",
                phone = "+91 99876 54321",
                currentLocation = com.google.android.gms.maps.model.LatLng(lat - 0.015, lng + 0.02),
                availabilityState = DriverAvailabilityState.ONLINE,
                rating = 4.65,
                totalCompletedTrips = 1890,
                vehicleId = vehicleId,
                vehicleNumber = "KA 05 MX 2191",
                lastLocationUpdateTime = System.currentTimeMillis()
            )
        )
    }
    
    private fun calculateAllocationScore(
        driver: DriverAllocationSnapshot,
        pickupLat: Double,
        pickupLng: Double,
        minRating: Double
    ): Float {
        if (driver.rating < minRating) return 0f
        
        // Score factors (out of 100):
        // - Proximity: 40 points
        // - Rating: 30 points
        // - Completion rate: 20 points
        // - Location freshness: 10 points
        
        val distance = haversineDistance(
            driver.currentLocation.latitude, driver.currentLocation.longitude,
            pickupLat, pickupLng
        )
        
        val proximityScore = if (distance <= 1.0) 40f else (40f * (1.0 / distance)).coerceAtMost(40f)
        val ratingScore = (driver.rating / 5.0 * 30).toFloat()
        val completionScore = (driver.acceptanceRate * 20).toFloat()
        val freshnessScore = if (System.currentTimeMillis() - driver.lastLocationUpdateTime < 60000) 10f else 5f
        
        return (proximityScore + ratingScore + completionScore + freshnessScore).coerceIn(0f, 100f)
    }
    
    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * acos(kotlin.math.sqrt(a))
        return R * c
    }
}
