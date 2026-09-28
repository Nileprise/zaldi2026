package com.example.domain.usecase

import com.example.domain.model.DynamicFareResponse
import com.example.domain.model.FareBreakdown
import com.example.domain.model.PricingRequest
import com.example.domain.model.SurgeInfo
import kotlin.math.max

class PricingUseCase {
    
    fun calculateFare(request: PricingRequest): DynamicFareResponse {
        // Get vehicle pricing rules
        val pricingRule = getPricingRuleForVehicle(request.vehicleId)
        
        // Calculate base fare
        val baseFare = pricingRule.baseFare
        val perKmAmount = request.distanceKm * pricingRule.perKmRate
        val subtotal = baseFare + perKmAmount
        
        // Apply surge pricing
        val surgeMultiplier = calculateSurgeMultiplier()
        val surgeInfo = SurgeInfo(
            isSurgeActive = surgeMultiplier > 1.0f,
            multiplier = surgeMultiplier,
            reason = if (surgeMultiplier > 1.0f) "High demand" else "",
            supplyLevel = getSupplyLevel(),
            demandLevel = getDemandLevel()
        )
        
        // Apply time-based surge (peak hours)
        val timeSurge = if (isPeakHour()) pricingRule.peakHourMultiplier else 1.0f
        
        // Gross fare before tax
        val grossFare = max(
            pricingRule.minimumFare,
            (subtotal * surgeMultiplier * timeSurge)
        )
        
        // Calculate breakdown
        val breakdown = FareBreakdown(
            baseAmount = baseFare,
            perKmAmount = perKmAmount,
            distanceKm = request.distanceKm,
            surgeMultiplier = surgeMultiplier,
            platformFeePercent = 5f,
            taxPercent = 5f,
            grossFare = grossFare,
            commission = grossFare * 0.20, // 20% platform commission
            tax = grossFare * 0.05,
            platformFee = grossFare * 0.05,
            payableFare = grossFare * 0.70 // Driver gets 70% after commission & tax
        )
        
        // Estimate duration (rough approximation: 4 min per km + 2 min buffer)
        val estimatedDuration = (request.distanceKm * 4 + 2).toInt()
        
        return DynamicFareResponse(
            vehicleId = request.vehicleId,
            grossFare = grossFare,
            payableFare = breakdown.payableFare,
            breakdown = breakdown,
            surgeInfo = surgeInfo,
            estimatedDurationMinutes = estimatedDuration
        )
    }
    
    private fun getPricingRuleForVehicle(vehicleId: String): com.example.domain.model.VehiclePricingRule {
        // Mock pricing rules - replace with DB lookup
        return when (vehicleId) {
            "bike" -> com.example.domain.model.VehiclePricingRule(
                vehicleId = "bike",
                vehicleName = "2-Wheeler (Bike)",
                baseFare = 45.0,
                perKmRate = 10.0,
                minimumFare = 50.0
            )
            "auto" -> com.example.domain.model.VehiclePricingRule(
                vehicleId = "auto",
                vehicleName = "3-Wheeler Auto",
                baseFare = 99.0,
                perKmRate = 16.0,
                minimumFare = 100.0
            )
            "tata" -> com.example.domain.model.VehiclePricingRule(
                vehicleId = "tata",
                vehicleName = "Tata Ace (Chota Hathi)",
                baseFare = 249.0,
                perKmRate = 24.0,
                minimumFare = 250.0
            )
            else -> com.example.domain.model.VehiclePricingRule(
                vehicleId = vehicleId,
                vehicleName = "Standard Vehicle",
                baseFare = 100.0,
                perKmRate = 15.0,
                minimumFare = 100.0
            )
        }
    }
    
    private fun calculateSurgeMultiplier(): Float {
        // Mock surge calculation based on demand/supply ratio
        // In production, this would query Redis for real-time metrics
        val demandSupplyRatio = getDemandSupplyRatio()
        
        return when {
            demandSupplyRatio > 3.0f -> 2.0f  // 3x surge
            demandSupplyRatio > 2.0f -> 1.5f  // 1.5x surge
            demandSupplyRatio > 1.5f -> 1.25f // 1.25x surge
            else -> 1.0f
        }
    }
    
    private fun getDemandSupplyRatio(): Float {
        // Query Redis for active orders vs available drivers
        // Mock: return 1.5f
        return 1.5f
    }
    
    private fun isPeakHour(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 8..10 || hour in 18..20 // Morning and evening rush
    }
    
    private fun getSupplyLevel(): String {
        return when (getDemandSupplyRatio()) {
            in 2.0f..Float.MAX_VALUE -> "LOW"
            in 1.5f..2.0f -> "MODERATE"
            else -> "HIGH"
        }
    }
    
    private fun getDemandLevel(): String {
        return when (getDemandSupplyRatio()) {
            in 0.0f..0.5f -> "LOW"
            in 0.5f..1.5f -> "NORMAL"
            else -> "HIGH"
        }
    }
}
