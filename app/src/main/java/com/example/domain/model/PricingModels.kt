package com.example.domain.model

import com.example.data.model.UserRole
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration

// ============================================================================
// Core Value Objects (Assuming Money and GeoCoordinate exist in your domain)
// ============================================================================

data class Money(
    val amount: BigDecimal,
    val currencyCode: String = "INR"
)

data class GeoCoordinate(
    val lat: Double,
    val lng: Double
)

// ============================================================================
// Enums
// ============================================================================

enum class SupplyLevel { CRITICAL, LOW, NORMAL, HIGH, EXCESS }
enum class DemandLevel { VERY_LOW, LOW, NORMAL, HIGH, EXTREME }

// ============================================================================
// Fare Calculation Rules & Breakdowns
// ============================================================================

/**
 * Detailed, auditable breakdown of a trip's cost.
 * Essential for generating legal receipts and driver payout statements.
 */
data class FareBreakdown(
    val baseAmount: Money,
    val distanceAmount: Money,
    val timeAmount: Money, // Time-based fare (e.g., traffic delays)
    val distanceKm: Double,
    val surgeMultiplier: BigDecimal = BigDecimal.ONE,
    
    // Additional Logistics Fees
    val tollsAmount: Money,
    val waitingTimeFee: Money,
    val discountAmount: Money,
    
    // Percentages stored as decimals (e.g., 0.05 for 5%)
    val platformFeePercent: BigDecimal,
    val taxPercent: BigDecimal,
    
    // Final calculated amounts
    val grossFare: Money,
    val commissionAmount: Money, // Driver's cut
    val taxAmount: Money,
    val platformFeeAmount: Money, // Company's cut
    val finalPayableFare: Money
)

data class SurgeRule(
    val id: String,
    val minDemandRatio: Double, // Demand / Supply ratio
    val maxDemandRatio: Double,
    val multiplier: BigDecimal,
    val validFrom: Instant,
    val validTo: Instant
) {
    fun isActive(atTime: Instant): Boolean {
        return atTime.isAfter(validFrom) && atTime.isBefore(validTo)
    }
}

data class VehiclePricingRule(
    val vehicleId: String,
    val vehicleName: String,
    val baseFare: Money,
    val perKmRate: Money,
    val perMinuteRate: Money, // Crucial for dense urban traffic
    val minimumFare: Money,
    val cancellationFee: Money,
    
    val surgePricingEnabled: Boolean = true,
    val peakHourMultiplier: BigDecimal = BigDecimal("1.5"),
    val pricingTimeZone: ZoneId = ZoneId.of("Asia/Kolkata")
)

// ============================================================================
// Pricing Requests & Responses
// ============================================================================

/**
 * The domain request sent to the pricing engine.
 */
data class PricingRequest(
    val vehicleId: String,
    val pickupLocation: GeoCoordinate,
    val dropoffLocation: GeoCoordinate,
    
    val estimatedDistanceKm: Double,
    val estimatedDuration: Duration, // Modern Kotlin Duration
    
    val isScheduled: Boolean = false,
    val scheduledTime: Instant? = null,
    
    val promoCode: String? = null, // Future-proofing for discounts
    val userRole: UserRole
)

/**
 * Information regarding active surge pricing to display to the user.
 */
data class SurgeInfo(
    val isSurgeActive: Boolean,
    val multiplier: BigDecimal,
    val reasonText: String? = null, // e.g., "High demand in your area"
    val supplyLevel: SupplyLevel = SupplyLevel.NORMAL,
    val demandLevel: DemandLevel = DemandLevel.NORMAL
)

/**
 * The calculated fare response, returned to the UI for user approval.
 */
data class DynamicFareResponse(
    val quoteId: String, // Unique ID for this specific fare calculation
    val vehicleId: String,
    
    val grossFare: Money,
    val finalPayableFare: Money,
    val breakdown: FareBreakdown,
    
    val surgeInfo: SurgeInfo,
    val estimatedDuration: Duration,
    
    // The time window this exact price is guaranteed for
    val quoteValidFor: Duration,
    val expiresAt: Instant,
    
    // Security: Encrypted token containing the fare data.
    // The client sends this back when confirming the order to prevent tampering.
    val pricingToken: String 
) {
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)
}
