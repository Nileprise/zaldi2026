package com.example.domain.model

import java.math.BigDecimal
import java.time.Instant

// ============================================================================
// Core Value Objects
// ============================================================================

/**
 * Ensures financial calculations never mix up currencies.
 */
data class Money(
    val amount: BigDecimal,
    val currencyCode: String = "INR"
) {
    operator fun plus(other: Money): Money {
        require(this.currencyCode == other.currencyCode) { "Cannot add mixed currencies" }
        return Money(this.amount + other.amount, this.currencyCode)
    }
}

/**
 * Represents a scheduled timeframe rather than a specific second.
 */
data class TimeWindow(
    val earliestStart: Instant,
    val latestEnd: Instant
)

/**
 * Groups physical dimensions for capacity planning.
 */
data class ItemDimensions(
    val lengthCm: Double,
    val widthCm: Double,
    val heightCm: Double
) {
    val volumeCubicCm: Double get() = lengthCm * widthCm * heightCm
}

/**
 * Encapsulates all data related to a physical location.
 */
data class LocationPoint(
    val address: String,
    val coordinate: GeoCoordinate,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val specialInstructions: String? = null // e.g., "Ring bell twice", "Gate code 1234"
)

// ============================================================================
// Enums
// ============================================================================

enum class OrderStatus {
    PENDING,
    ASSIGNED,
    DRIVER_ACCEPTED,
    DRIVER_EN_ROUTE,
    DRIVER_ARRIVED,
    PICKUP_IN_PROGRESS,
    IN_TRANSIT,
    DELIVERY_ARRIVED,
    DELIVERY_IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}

enum class DeliveryFailureReason {
    CUSTOMER_UNAVAILABLE,
    WRONG_ADDRESS,
    GOODS_DAMAGED,
    VEHICLE_BREAKDOWN,
    PAYMENT_FAILED,
    RECIPIENT_REJECTED
}

enum class PaymentMethod {
    COD, 
    ONLINE_UPI, 
    CREDIT_CARD, 
    CORPORATE_ACCOUNT, 
    WALLET
}

enum class CancellationActor {
    CUSTOMER,
    DRIVER,
    ADMIN,
    SYSTEM_TIMEOUT
}

enum class OrderPriority {
    STANDARD,
    EXPRESS,
    SCHEDULED
}

// ============================================================================
// Main Domain Models
// ============================================================================

/**
 * Granular breakdown of the trip cost for receipts and UI display.
 */
data class FareBreakdown(
    val baseFare: Money,
    val distanceFare: Money,
    val timeFare: Money,
    val surgeFee: Money,
    val tollCharges: Money,
    val discount: Money,
    val taxAmount: Money,
    val totalPayable: Money,
    val driverCommission: Money // What the driver earns
)

/**
 * Modernized Order Domain Model.
 */
data class Order(
    val id: String,
    val customerId: String,
    val customerPhone: String,
    val customerName: String,
    
    // --- Logistics & Routing ---
    val assignedDriverId: String? = null,
    val pickupLocation: LocationPoint,
    val dropoffLocation: LocationPoint,
    val vehicleTierId: String,
    val estimatedDistanceKm: Double,
    val priority: OrderPriority = OrderPriority.STANDARD,
    
    // --- Goods & Verification ---
    val goodsDescription: String,
    val securityPin: String? = null, // OTP required to start or complete trip
    val trackingUrlId: String? = null, // Unique hash for public web tracking link
    
    // --- Financials ---
    val fareBreakdown: FareBreakdown,
    val paymentMethod: PaymentMethod,
    val isPaid: Boolean = false,
    
    // --- State & Timestamps ---
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val deliveryWindow: TimeWindow? = null, // If null, assume ASAP
    val createdAt: Instant = Instant.now(),
    val assignedAt: Instant? = null,
    val completedAt: Instant? = null,
    
    // --- Failure/Cancellation Metadata ---
    val cancellationReason: String? = null,
    val cancelledBy: CancellationActor? = null,
    val failureReason: DeliveryFailureReason? = null,
    
    // --- Sub-Entities ---
    val orderItems: List<OrderItem> = emptyList(),
    val trackingHistory: List<OrderStatusUpdate> = emptyList(),
    val proofOfDelivery: ProofOfDelivery? = null
)

data class OrderItem(
    val itemId: String,
    val description: String,
    val weightKg: Double,
    val quantity: Int,
    // Modern Additions for precise vehicle allocation
    val dimensions: ItemDimensions? = null,
    val isFragile: Boolean = false,
    val requiresColdChain: Boolean = false,
    val photos: List<String> = emptyList()
)

data class OrderStatusUpdate(
    val status: OrderStatus,
    val timestamp: Instant = Instant.now(),
    val location: GeoCoordinate? = null,
    val notes: String? = null
)

data class ProofOfDelivery(
    val deliveryTime: Instant = Instant.now(),
    val recipientName: String,
    val recipientPhone: String,
    val signaturePhotoUrl: String? = null,
    val itemPhotosBeforeDelivery: List<String> = emptyList(),
    val itemPhotosAfterDelivery: List<String> = emptyList(),
    val temperatureCelsius: Double? = null, 
    val notes: String? = null
)

data class OrderChain(
    val chainId: String,
    val customerId: String,
    val orderIds: List<String>, 
    val sequenceIndex: Int, 
    val handoffLocation: LocationPoint? = null,
    val scheduledHandoffWindow: TimeWindow? = null
)
