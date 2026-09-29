package com.example.domain.usecase

import com.example.domain.model.CancellationActor
import com.example.domain.model.GeoCoordinate
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.OrderStatusUpdate
import com.example.domain.model.ProofOfDelivery
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject

/**
 * Interface definitions to keep the Domain layer decoupled from Data/Infrastructure layers.
 */
interface OrderRepository {
    suspend fun saveOrder(order: Order): Result<Order>
}

interface EventPublisher {
    suspend fun publish(eventType: String, payload: Map<String, Any?>)
}

/**
 * Modernized Use Case orchestrating core Order lifecycle business logic.
 */
class OrderManagementUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val eventPublisher: EventPublisher
) {
    
    suspend fun createOrder(order: Order): Result<Order> {
        return runCatching {
            // Domain Validation
            require(order.fareBreakdown.totalPayable.amount > BigDecimal.ZERO) { 
                "Order fare must be greater than zero." 
            }
            
            val newOrder = order.copy(
                createdAt = Instant.now(),
                orderStatus = OrderStatus.PENDING
            )
            
            // Persist and Publish
            val savedOrder = orderRepository.saveOrder(newOrder).getOrThrow()
            eventPublisher.publish("OrderCreated", mapOf("orderId" to savedOrder.id))
            
            savedOrder
        }
    }
    
    suspend fun assignDriverToOrder(order: Order, driverId: String): Result<Order> {
        return runCatching {
            require(order.orderStatus == OrderStatus.PENDING) {
                "Cannot assign driver. Order is currently in ${order.orderStatus} state."
            }
            
            val updatedOrder = order.copy(
                assignedDriverId = driverId,
                assignedAt = Instant.now(),
                orderStatus = OrderStatus.ASSIGNED
            )
            
            val savedOrder = orderRepository.saveOrder(updatedOrder).getOrThrow()
            
            eventPublisher.publish("DriverAssigned", mapOf(
                "orderId" to savedOrder.id,
                "driverId" to driverId,
                "fareAmount" to savedOrder.fareBreakdown.totalPayable.amount
            ))
            
            savedOrder
        }
    }
    
    suspend fun updateOrderStatus(
        order: Order, 
        newStatus: OrderStatus, 
        notes: String? = null,
        currentLocation: GeoCoordinate? = null
    ): Result<Order> {
        return runCatching {
            validateStateTransition(order.orderStatus, newStatus)
            
            val statusUpdate = OrderStatusUpdate(
                status = newStatus,
                timestamp = Instant.now(),
                location = currentLocation,
                notes = notes
            )
            
            val updatedOrder = order.copy(
                orderStatus = newStatus,
                trackingHistory = order.trackingHistory + statusUpdate
            )
            
            val savedOrder = orderRepository.saveOrder(updatedOrder).getOrThrow()
            
            eventPublisher.publish("OrderStatusChanged", mapOf(
                "orderId" to savedOrder.id,
                "status" to newStatus.name,
                "timestamp" to Instant.now().toString()
            ))
            
            savedOrder
        }
    }
    
    suspend fun completeOrderWithPOD(
        order: Order,
        pod: ProofOfDelivery,
        providedPin: String? = null // Future-proofing: OTP verification
    ): Result<Order> {
        return runCatching {
            require(order.orderStatus == OrderStatus.DELIVERY_IN_PROGRESS) {
                "Cannot complete order from ${order.orderStatus} state. Must be IN_PROGRESS."
            }
            
            // Security / Anti-fraud check
            if (order.securityPin != null) {
                require(order.securityPin == providedPin) {
                    "Invalid security PIN (OTP). Cannot complete delivery."
                }
            }
            
            val statusUpdate = OrderStatusUpdate(
                status = OrderStatus.COMPLETED,
                timestamp = Instant.now(),
                notes = "Delivered to ${pod.recipientName}"
            )
            
            val updatedOrder = order.copy(
                orderStatus = OrderStatus.COMPLETED,
                completedAt = Instant.now(),
                proofOfDelivery = pod,
                trackingHistory = order.trackingHistory + statusUpdate
            )
            
            val savedOrder = orderRepository.saveOrder(updatedOrder).getOrThrow()
            
            eventPublisher.publish("DeliveryCompleted", mapOf(
                "orderId" to savedOrder.id,
                "driverId" to savedOrder.assignedDriverId,
                "payableFare" to savedOrder.fareBreakdown.totalPayable.amount,
                "podSignatureUrl" to pod.signaturePhotoUrl
            ))
            
            savedOrder
        }
    }
    
    suspend fun cancelOrder(
        order: Order, 
        reason: String, 
        actor: CancellationActor
    ): Result<Order> {
        return runCatching {
            require(order.orderStatus !in listOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
                "Order is already ${order.orderStatus} and cannot be cancelled."
            }
            
            val updatedOrder = order.copy(
                orderStatus = OrderStatus.CANCELLED,
                cancellationReason = reason,
                cancelledBy = actor
            )
            
            val savedOrder = orderRepository.saveOrder(updatedOrder).getOrThrow()
            
            eventPublisher.publish("OrderCancelled", mapOf(
                "orderId" to savedOrder.id,
                "reason" to reason,
                "actor" to actor.name
            ))
            
            savedOrder
        }
    }

    /**
     * Centralized strict state machine validation.
     */
    private fun validateStateTransition(current: OrderStatus, next: OrderStatus) {
        val validTransitions = mapOf(
            OrderStatus.PENDING to listOf(OrderStatus.ASSIGNED, OrderStatus.CANCELLED),
            OrderStatus.ASSIGNED to listOf(OrderStatus.DRIVER_ACCEPTED, OrderStatus.CANCELLED),
            OrderStatus.DRIVER_ACCEPTED to listOf(OrderStatus.DRIVER_EN_ROUTE, OrderStatus.CANCELLED),
            OrderStatus.DRIVER_EN_ROUTE to listOf(OrderStatus.DRIVER_ARRIVED, OrderStatus.CANCELLED),
            OrderStatus.DRIVER_ARRIVED to listOf(OrderStatus.PICKUP_IN_PROGRESS, OrderStatus.CANCELLED),
            OrderStatus.PICKUP_IN_PROGRESS to listOf(OrderStatus.IN_TRANSIT, OrderStatus.CANCELLED),
            OrderStatus.IN_TRANSIT to listOf(OrderStatus.DELIVERY_ARRIVED, OrderStatus.FAILED),
            OrderStatus.DELIVERY_ARRIVED to listOf(OrderStatus.DELIVERY_IN_PROGRESS, OrderStatus.FAILED),
            OrderStatus.DELIVERY_IN_PROGRESS to listOf(OrderStatus.COMPLETED, OrderStatus.FAILED),
            
            // Terminal States
            OrderStatus.COMPLETED to emptyList(),
            OrderStatus.FAILED to listOf(OrderStatus.CANCELLED),
            OrderStatus.CANCELLED to emptyList()
        )
        
        val allowedTransitions = validTransitions[current] ?: emptyList()
        require(next in allowedTransitions) {
            "Invalid State Transition: Cannot transition from $current to $next"
        }
    }
}
