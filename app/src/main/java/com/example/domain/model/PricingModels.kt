package com.example.domain.model

import com.example.data.model.UserRole

// Fare Calculation Models
data class FareBreakdown(
    val baseAmount: Double,
    val perKmAmount: Double,
    val distanceKm: Double,
    val surgeMultiplier: Float = 1.0f,
    val platformFeePercent: Float = 5f,
    val taxPercent: Float = 5f,
    val grossFare: Double,
    val commission: Double,
    val tax: Double,
    val platformFee: Double,
    val payableFare: Double
)

data class SurgeRule(
    val minDemandRatio: Float, // demand / supply ratio
    val maxDemandRatio: Float,
    val multiplier: Float,
    val validFrom: Long,
    val validTo: Long
)

data class VehiclePricingRule(
    val vehicleId: String,
    val vehicleName: String,
    val baseFare: Double,
    val perKmRate: Double,
    val minimumFare: Double = 0.0,
    val surgePricingEnabled: Boolean = true,
    val peakHourMultiplier: Float = 1.5f,
    val timeZone: String = "Asia/Kolkata"
)

data class PricingRequest(
    val vehicleId: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val isScheduled: Boolean = false,
    val scheduledTime: Long? = null,
    val userRole: UserRole
)

data class DynamicFareResponse(
    val orderId: String = "",
    val vehicleId: String,
    val grossFare: Double,
    val payableFare: Double,
    val breakdown: FareBreakdown,
    val surgeInfo: SurgeInfo,
    val estimatedDurationMinutes: Int,
    val validForSeconds: Int = 300
)

data class SurgeInfo(
    val isSurgeActive: Boolean,
    val multiplier: Float,
    val reason: String = "",
    val supplyLevel: String = "NORMAL",
    val demandLevel: String = "NORMAL"
)
