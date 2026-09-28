package com.example.domain.model

import com.example.data.model.UserRole

// Order State Machine
enum class OrderStatus {
    PENDING,           // Created, waiting for driver assignment
    ASSIGNED,          // Driver assigned
    DRIVER_ACCEPTED,   // Driver accepted the order
    DRIVER_EN_ROUTE,   // Driver heading to pickup
    DRIVER_ARRIVED,    // Driver at pickup location
    PICKUP_IN_PROGRESS,// Items being loaded
    IN_TRANSIT,        // In delivery
    DELIVERY_ARRIVED,  // At delivery location
    DELIVERY_IN_PROGRESS, // Handoff happening
    COMPLETED,         // Successfully delivered
    CANCELLED,         // Order cancelled
    FAILED             // Delivery failed
}

data class Order(
    val id: String,
    val customerId: String,
    val customerPhone: String,
    val customerName: String,
    val assignedDriverId: String? = null,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val vehicleType: String,
    val goodsDescription: String,
    val estimatedDistanceKm: Double,
    val grossFare: Double,
    val payableFare: Double,
    val taxAmount: Double,
    val commissionAmount: Double,
    val paymentMethod: String, // "COD", "ONLINE", "CORPORATE"
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val assignedAt: Long? = null,
    val completedAt: Long? = null,
    val cancellationReason: String? = null,
    val orderItems: List<OrderItem> = emptyList(),
    val proofOfDelivery: ProofOfDelivery? = null,
    val trackingHistory: List<OrderStatusUpdate> = emptyList()
)

data class OrderItem(
    val itemId: String,
    val description: String,
    val weight: Double, // kg
    val quantity: Int,
    val photos: List<String> = emptyList()
)

data class OrderStatusUpdate(
    val status: OrderStatus,
    val timestamp: Long,
    val location: Pair<Double, Double>? = null,
    val notes: String? = null
)

data class ProofOfDelivery(
    val deliveryTime: Long,
    val recipientName: String,
    val recipientPhone: String,
    val signaturePhotoUrl: String? = null,
    val itemPhotosBeforeDelivery: List<String> = emptyList(),
    val itemPhotosAfterDelivery: List<String> = emptyList(),
    val temperatureReading: Double? = null,
    val notes: String? = null
)

data class OrderChain(
    val chainId: String,
    val customerId: String,
    val orders: List<String>, // order IDs in sequence
    val sequence: Int, // for multi-vehicle scenarios
    val handoffLocation: Pair<Double, Double>? = null, // if transferring between vehicles
    val handoffTime: Long? = null
)
