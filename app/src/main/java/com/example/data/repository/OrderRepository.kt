package com.example.data.repository

import com.example.data.cache.RedisCache
import com.example.data.local.RealtimeDatabase
import com.example.data.remote.ApiService
import com.example.domain.model.AllocationRequest
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.usecase.AllocationUseCase
import com.example.domain.usecase.OrderManagementUseCase
import com.example.domain.usecase.PricingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderRepository(
    private val apiService: ApiService,
    private val realtimeDatabase: RealtimeDatabase,
    private val pricingUseCase: PricingUseCase,
    private val allocationUseCase: AllocationUseCase,
    private val orderManagementUseCase: OrderManagementUseCase
) {
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()
    
    suspend fun createAndAllocateOrder(order: Order): Order? {
        try {
            // Step 1: Create order
            val createdOrder = orderManagementUseCase.createOrder(order)
            
            // Step 2: Try to allocate driver
            val allocationRequest = AllocationRequest(
                orderId = createdOrder.id,
                pickupLat = createdOrder.pickupLat,
                pickupLng = createdOrder.pickupLng,
                dropoffLat = createdOrder.dropoffLat,
                dropoffLng = createdOrder.dropoffLng,
                requiredVehicleId = createdOrder.vehicleType,
                maxAllocationRadiusKm = 5.0
            )
            
            val allocationResult = allocationUseCase.findBestDriverMatch(allocationRequest)
            
            return if (allocationResult != null) {
                // Assign driver
                val assignedOrder = orderManagementUseCase.assignDriverToOrder(
                    createdOrder.id,
                    allocationResult.allocatedDriverId,
                    createdOrder
                )
                
                // Cache order status
                RedisCache.cacheOrderStatus(assignedOrder.id, assignedOrder.orderStatus.name)
                
                // Publish to realtime database
                realtimeDatabase.publishOrderStatus(assignedOrder.id, assignedOrder.orderStatus.name)
                
                _activeOrder.value = assignedOrder
                assignedOrder
            } else {
                // No driver available, keep in PENDING state
                RedisCache.cacheOrderStatus(createdOrder.id, OrderStatus.PENDING.name)
                realtimeDatabase.publishOrderStatus(createdOrder.id, OrderStatus.PENDING.name)
                createdOrder
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        try {
            val currentOrder = _activeOrder.value ?: return
            val updated = orderManagementUseCase.updateOrderStatus(currentOrder, newStatus)
            
            // Update cache
            RedisCache.cacheOrderStatus(orderId, newStatus.name)
            
            // Publish to realtime database for live tracking
            realtimeDatabase.publishOrderStatus(orderId, newStatus.name)
            
            _activeOrder.value = updated
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getOrdersByCustomer(customerId: String) {
        try {
            val response = apiService.getCustomerOrders(customerId)
            if (response.isSuccessful) {
                _orders.value = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
