package com.example.domain.model

import java.math.BigDecimal
import java.time.Instant

/**
 * Encapsulates all data related to a physical location.
 * Cleans up the Order class by grouping address, coordinates, and instructions.
 */
data class LocationPoint(
    val address: String,
    val coordinate: GeoCoordinate,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val specialInstructions: String? = null // e.g., "Ring bell twice", "Gate code 1234"
)

enum class OrderStatus {
    PENDING,              // Created, waiting for driver assignment
    ASSIGNED,             // Driver assigned
    DRIVER_ACCEPTED,      // Driver accepted the order
    DRIVER_EN_ROUTE,      // Driver heading to pickup
    DRIVER_ARRIVED,       // Driver at pickup location
    PICKUP_IN_PROGRESS,   // Items being loaded
    IN_TRANSIT,           // In delivery
    DELIVERY_ARRIVED,     // At delivery location
    DELIVERY_IN_PROGRESS, // Handoff happening
    COMPLETED,            // Successfully delivered
    CANCELLED,            // Order cancelled
    FAILED                // Delivery failed (e.g., recipient not found)
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
    
    // --- Financials (Always use BigDecimal for money) ---
    val grossFare: BigDecimal,
    val payableFare: BigDecimal,
    val taxAmount: BigDecimal,
    val commissionAmount: BigDecimal,
    val paymentMethod: PaymentMethod,
    
    // --- State & Timestamps (Using Instant for strict time precision) ---
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val scheduledTime: Instant? = null, // If null, assume ASAP
    val createdAt: Instant = Instant.now(),
    val assignedAt: Instant? = null,
    val completedAt: Instant? = null,
    
    // --- Cancellation Metadata ---
    val cancellationReason: String? = null,
    val cancelledBy: CancellationActor? = null,
    
    // --- Sub-Entities ---
    val orderItems: List<OrderItem> = emptyList(),
    val trackingHistory: List<OrderStatusUpdate> = emptyList(),
    val proofOfDelivery: ProofOfDelivery? = null
)

data class OrderItem(
    val itemId: String,
    val description: String,
    val weightKg: Double, // Clarified unit in variable name
    val quantity: Int,
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
    
    // Future-proofing for cold-chain / pharmaceutical logistics
    val temperatureCelsius: Double? = null, 
    val notes: String? = null
)

/**
 * Used for multi-modal logistics (e.g., Truck takes to warehouse, Bike takes to door).
 */
data class OrderChain(
    val chainId: String,
    val customerId: String,
    val orderIds: List<String>, // ordered sequence of leg IDs
    val sequenceIndex: Int, 
    val handoffLocation: LocationPoint? = null,
    val scheduledHandoffTime: Instant? = null
)
