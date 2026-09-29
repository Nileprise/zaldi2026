package com.example.domain.usecase

import com.example.domain.model.DemandLevel
import com.example.domain.model.DynamicFareResponse
import com.example.domain.model.FareBreakdown
import com.example.domain.model.GeoCoordinate
import com.example.domain.model.Money
import com.example.domain.model.PricingRequest
import com.example.domain.model.SupplyLevel
import com.example.domain.model.SurgeInfo
import com.example.domain.model.VehiclePricingRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * Interface to decouple data fetching (Database, Redis, API) from business logic.
 */
interface PricingRepository {
    suspend fun getPricingRuleForVehicle(vehicleId: String): VehiclePricingRule?
    suspend fun getDemandSupplyRatio(location: GeoCoordinate): Double
}

/**
 * Modernized Use Case for dynamic fare calculation.
 * Uses strict BigDecimal math for all financial operations.
 */
class PricingUseCase @Inject constructor(
    private val pricingRepository: PricingRepository
) {

    suspend fun calculateFare(request: PricingRequest): Result<DynamicFareResponse> = withContext(Dispatchers.Default) {
        runCatching {
            val rule = pricingRepository.getPricingRuleForVehicle(request.vehicleId)
                ?: throw IllegalArgumentException("Pricing rule not found for vehicle: ${request.vehicleId}")

            val demandSupplyRatio = pricingRepository.getDemandSupplyRatio(request.pickupLocation)

            // 1. Calculate Core Fare Components
            val baseFare = rule.baseFare.amount
            
            val distanceAmount = rule.perKmRate.amount.multiply(
                BigDecimal(request.estimatedDistanceKm.toString())
            )
            
            val timeAmount = rule.perMinuteRate.amount.multiply(
                BigDecimal(request.estimatedDuration.inWholeMinutes.toString())
            )
            
            val subtotal = baseFare.add(distanceAmount).add(timeAmount)

            // 2. Calculate Surge & Peak Multipliers
            val surgeMultiplier = calculateSurgeMultiplier(demandSupplyRatio, rule.surgePricingEnabled)
            val peakMultiplier = if (isPeakHour(rule)) rule.peakHourMultiplier else BigDecimal.ONE
            val combinedMultiplier = surgeMultiplier.multiply(peakMultiplier)

            // 3. Apply Multipliers and Minimum Fare bounds
            var grossAmount = subtotal.multiply(combinedMultiplier)
            if (grossAmount < rule.minimumFare.amount) {
                grossAmount = rule.minimumFare.amount
            }

            // 4. Calculate Taxes and Commissions (Strict 2-decimal rounding)
            val taxPercent = BigDecimal("0.05") // 5% GST/Tax
            val platformFeePercent = BigDecimal("0.05") // 5% Platform Fee
            val commissionPercent = BigDecimal("0.20") // 20% Driver Commission Deduction

            val taxAmount = grossAmount.multiply(taxPercent).setScale(2, RoundingMode.HALF_UP)
            val platformFeeAmount = grossAmount.multiply(platformFeePercent).setScale(2, RoundingMode.HALF_UP)
            
            // What the driver actually keeps (Gross - Commission)
            val commissionDeduction = grossAmount.multiply(commissionPercent).setScale(2, RoundingMode.HALF_UP)
            val driverEarnings = grossAmount.subtract(commissionDeduction)

            // Total Customer Payable = Gross + Tax
            val finalPayable = grossAmount.add(taxAmount)

            // 5. Construct the safe FareBreakdown using the Money object
            val currency = rule.baseFare.currencyCode
            
            val breakdown = FareBreakdown(
                baseAmount = Money(baseFare, currency),
                distanceAmount = Money(distanceAmount, currency),
                timeAmount = Money(timeAmount, currency),
                distanceKm = request.estimatedDistanceKm,
                surgeMultiplier = combinedMultiplier,
                tollsAmount = Money(BigDecimal.ZERO, currency), // Placeholder for future toll API
                waitingTimeFee = Money(BigDecimal.ZERO, currency),
                discountAmount = Money(BigDecimal.ZERO, currency),
                platformFeePercent = platformFeePercent,
                taxPercent = taxPercent,
                grossFare = Money(grossAmount, currency),
                commissionAmount = Money(driverEarnings, currency), // Driver's take-home
                taxAmount = Money(taxAmount, currency),
                platformFeeAmount = Money(platformFeeAmount, currency),
                finalPayableFare = Money(finalPayable, currency)
            )

            val quoteValidFor = 5.minutes
            val expiresAt = Instant.now().plusMillis(quoteValidFor.inWholeMilliseconds)

            // 6. Return the finalized Response
            DynamicFareResponse(
                quoteId = UUID.randomUUID().toString(),
                vehicleId = request.vehicleId,
                grossFare = Money(grossAmount, currency),
                finalPayableFare = Money(finalPayable, currency),
                breakdown = breakdown,
                surgeInfo = buildSurgeInfo(demandSupplyRatio, combinedMultiplier),
                estimatedDuration = request.estimatedDuration,
                quoteValidFor = quoteValidFor,
                expiresAt = expiresAt,
                pricingToken = generateSecurePricingToken(grossAmount, expiresAt)
            )
        }
    }

    private fun calculateSurgeMultiplier(ratio: Double, isSurgeEnabled: Boolean): BigDecimal {
        if (!isSurgeEnabled) return BigDecimal.ONE
        
        return when {
            ratio > 3.0 -> BigDecimal("2.0")   // 2.0x surge
            ratio > 2.0 -> BigDecimal("1.5")   // 1.5x surge
            ratio > 1.5 -> BigDecimal("1.25")  // 1.25x surge
            else -> BigDecimal.ONE
        }
    }

    private fun buildSurgeInfo(ratio: Double, multiplier: BigDecimal): SurgeInfo {
        val supplyLevel = when {
            ratio > 2.0 -> SupplyLevel.LOW
            ratio > 1.5 -> SupplyLevel.NORMAL
            else -> SupplyLevel.HIGH
        }
        
        val demandLevel = when {
            ratio > 2.0 -> DemandLevel.EXTREME
            ratio > 1.5 -> DemandLevel.HIGH
            ratio > 0.5 -> DemandLevel.NORMAL
            else -> DemandLevel.LOW
        }

        return SurgeInfo(
            isSurgeActive = multiplier > BigDecimal.ONE,
            multiplier = multiplier,
            reasonText = if (multiplier > BigDecimal.ONE) "Fares are higher due to increased demand" else null,
            supplyLevel = supplyLevel,
            demandLevel = demandLevel
        )
    }

    private fun isPeakHour(rule: VehiclePricingRule): Boolean {
        // Modern Time API: Respects the local timezone of the specific region
        val localTime = ZonedDateTime.now(rule.pricingTimeZone).toLocalTime()
        
        val morningRushStart = LocalTime.of(8, 0)
        val morningRushEnd = LocalTime.of(10, 30)
        val eveningRushStart = LocalTime.of(18, 0)
        val eveningRushEnd = LocalTime.of(20, 30)

        val isMorningRush = !localTime.isBefore(morningRushStart) && !localTime.isAfter(morningRushEnd)
        val isEveningRush = !localTime.isBefore(eveningRushStart) && !localTime.isAfter(eveningRushEnd)

        return isMorningRush || isEveningRush
    }

    /**
     * Future-proofing: Generates an encrypted hash of the quote.
     * When the user clicks "Confirm", the client passes this back to the backend.
     * The backend verifies the token to ensure the user didn't modify the price.
     */
    private fun generateSecurePricingToken(amount: BigDecimal, expiresAt: Instant): String {
        // In production, this would use a JWT or HMAC-SHA256
        return "TOKEN_${amount}_${expiresAt.toEpochMilli()}"
    }
}
