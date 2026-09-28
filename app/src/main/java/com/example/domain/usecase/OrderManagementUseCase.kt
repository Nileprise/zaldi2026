package com.example.domain.usecase

import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.OrderStatusUpdate
import com.example.domain.model.ProofOfDelivery

class OrderManagementUseCase {
    
    fun createOrder(order: Order): Order {
        // Validate order
        require(order.pickupLat != 0.0 && order.pickupLng != 0.0) { "Invalid pickup location" }
        require(order.dropoffLat != 0.0 && order.dropoffLng != 0.0) { "Invalid dropoff location" }
        require(order.grossFare > 0) { "Invalid fare" }
        
        // In production, save to database and publish OrderCreatedEvent to Kafka
        return order.copy(
            createdAt = System.currentTimeMillis(),
            orderStatus = OrderStatus.PENDING
        )
    }
    
    fun assignDriverToOrder(orderId: String, driverId: String, order: Order): Order {
        // Validate state transition
        require(order.orderStatus == OrderStatus.PENDING) {
            "Cannot assign driver to order in ${order.orderStatus} state"
        }
        
        val updated = order.copy(
            assignedDriverId = driverId,
            assignedAt = System.currentTimeMillis(),
            orderStatus = OrderStatus.ASSIGNED
        )
        
        // Publish DriverAssignedEvent to notify driver via push notification
        publishEvent("DriverAssigned", mapOf(
            "orderId" to orderId,
            "driverId" to driverId,
            "fare" to order.grossFare
        ))
        
        return updated
    }
    
    fun updateOrderStatus(order: Order, newStatus: OrderStatus, notes: String? = null): Order {
        // Validate state transitions
        val validTransitions = mapOf(
            OrderStatus.PENDING to listOf(OrderStatus.ASSIGNED, OrderStatus.CANCELLED),
            OrderStatus.ASSIGNED to listOf(OrderStatus.DRIVER_ACCEPTED, OrderStatus.CANCELLED),
            OrderStatus.DRIVER_ACCEPTED to listOf(OrderStatus.DRIVER_EN_ROUTE, OrderStatus.CANCELLED),
            OrderStatus.DRIVER_EN_ROUTE to listOf(OrderStatus.DRIVER_ARRIVED),
            OrderStatus.DRIVER_ARRIVED to listOf(OrderStatus.PICKUP_IN_PROGRESS),
            OrderStatus.PICKUP_IN_PROGRESS to listOf(OrderStatus.IN_TRANSIT),
            OrderStatus.IN_TRANSIT to listOf(OrderStatus.DELIVERY_ARRIVED),
            OrderStatus.DELIVERY_ARRIVED to listOf(OrderStatus.DELIVERY_IN_PROGRESS),
            OrderStatus.DELIVERY_IN_PROGRESS to listOf(OrderStatus.COMPLETED, OrderStatus.FAILED),
            OrderStatus.COMPLETED to emptyList(),
            OrderStatus.FAILED to listOf(OrderStatus.CANCELLED),
            OrderStatus.CANCELLED to emptyList()
        )
        
        val allowedTransitions = validTransitions[order.orderStatus] ?: emptyList()
        require(newStatus in allowedTransitions) {
            "Cannot transition from ${order.orderStatus} to $newStatus"
        }
        
        val statusUpdate = OrderStatusUpdate(
            status = newStatus,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        
        val updated = order.copy(
            orderStatus = newStatus,
            trackingHistory = order.trackingHistory + statusUpdate,
            completedAt = if (newStatus == OrderStatus.COMPLETED) System.currentTimeMillis() else order.completedAt
        )
        
        // Publish OrderStatusChangedEvent for real-time customer updates
        publishEvent("OrderStatusChanged", mapOf(
            "orderId" to order.id,
            "status" to newStatus.name,
            "timestamp" to System.currentTimeMillis()
        ))
        
        return updated
    }
    
    fun completeOrderWithPOD(
        order: Order,
        pod: ProofOfDelivery
    ): Order {
        require(order.orderStatus == OrderStatus.DELIVERY_IN_PROGRESS) {
            "Cannot complete order in ${order.orderStatus} state"
        }
        
        val updated = order.copy(
            orderStatus = OrderStatus.COMPLETED,
            completedAt = System.currentTimeMillis(),
            proofOfDelivery = pod,
            trackingHistory = order.trackingHistory + OrderStatusUpdate(
                status = OrderStatus.COMPLETED,
                timestamp = System.currentTimeMillis(),
                notes = "Delivered to ${pod.recipientName}"
            )
        )
        
        // Publish DeliveryCompletedEvent for payment processing
        publishEvent("DeliveryCompleted", mapOf(
            "orderId" to order.id,
            "driverId" to order.assignedDriverId,
            "payableFare" to order.payableFare,
            "podPhotos" to pod.signaturePhotoUrl
        ))
        
        return updated
    }
    
    fun cancelOrder(order: Order, reason: String): Order {
        require(order.orderStatus !in listOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            "Cannot cancel order in ${order.orderStatus} state"
        }
        
        val updated = order.copy(
            orderStatus = OrderStatus.CANCELLED,
            cancellationReason = reason
        )
        
        // Publish OrderCancelledEvent
        publishEvent("OrderCancelled", mapOf(
            "orderId" to order.id,
            "reason" to reason,
            "refundAmount" to order.grossFare
        ))
        
        return updated
    }
    
    private fun publishEvent(eventType: String, payload: Map<String, Any?>) {
        // In production, publish to Kafka topic
        // Topic: events.${eventType}
        // Payload: JSON encoded
        println("Event published: $eventType -> $payload")
    }
}
