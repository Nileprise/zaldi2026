package com.example.data.repository

import com.example.data.cache.RedisCacheManager
import com.example.data.local.RealtimeDatabaseManager
import com.example.data.remote.AllocationRequestDto
import com.example.data.remote.ApiService
import com.example.data.remote.AssignDriverRequest
import com.example.data.remote.CreateOrderRequest
import com.example.data.remote.UpdateOrderStatusRequest
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Modernized Repository.
 * Handles data mapping (DTO <-> Domain) and coordinates between API, Cache, and Realtime DB.
 */
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val realtimeDatabase: RealtimeDatabaseManager,
    private val cacheManager: RedisCacheManager
) {

    /**
     * Creates an order and attempts to allocate a driver immediately.
     * Returns a standard Kotlin Result to handle success/failure elegantly in the ViewModel.
     */
    suspend fun createAndAllocateOrder(order: Order): Result<Order> = withContext(Dispatchers.IO) {
        runCatching {
            // Step 1: Map Domain to DTO and Create Order via API
            val createRequest = CreateOrderRequest(/* map order fields here */)
            val createdOrderDto = apiService.createOrder(createRequest)

            // Step 2: Attempt Allocation
            val allocationRequest = AllocationRequestDto(
                orderId = createdOrderDto.id,
                pickupLat = order.pickupLat,
                pickupLng = order.pickupLng,
                dropoffLat = order.dropoffLat,
                dropoffLng = order.dropoffLng,
                requiredVehicleId = order.vehicleType,
                maxAllocationRadiusKm = 5.0
            )
            
            // In a real backend, allocation might return null/empty if no drivers are found.
            // Assuming the backend returns the best match or throws/returns empty:
            val allocationResult = runCatching { apiService.findBestDriver(allocationRequest) }.getOrNull()

            val finalOrderDto = if (allocationResult != null) {
                // Step 3a: Driver found, assign via API
                val assignRequest = AssignDriverRequest(
                    driverId = allocationResult.allocatedDriverId,
                    vehicleId = order.vehicleType,
                    estimatedArrivalMinutes = allocationResult.etaMinutes
                )
                val assignedDto = apiService.assignDriver(createdOrderDto.id, assignRequest)
                
                // Sync Live States for Assigned Order
                syncLiveOrderState(assignedDto.id, OrderStatus.DRIVER_ASSIGNED)
                assignedDto
            } else {
                // Step 3b: No driver found, leave as PENDING
                syncLiveOrderState(createdOrderDto.id, OrderStatus.PENDING)
                createdOrderDto
            }

            // Step 4: Map DTO back to Domain Model
            finalOrderDto.toDomain()
        }
    }

    /**
     * Updates the status of an order across the API, Cache, and Realtime stream.
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order> = withContext(Dispatchers.IO) {
        runCatching {
            // Update backend via API
            val request = UpdateOrderStatusRequest(status = newStatus.name)
            val updatedOrderDto = apiService.updateOrderStatus(orderId, request)
            
            // Sync local & realtime components
            syncLiveOrderState(orderId, newStatus)
            
            updatedOrderDto.toDomain()
        }
    }

    /**
     * Fetches the customer's order history. 
     * The ViewModel should collect this Result and update its own StateFlow.
     */
    suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        runCatching {
            val dtoList = apiService.getCustomerOrders(customerId)
            dtoList.map { it.toDomain() }
        }
    }

    /**
     * Helper to keep Cache and Firebase Realtime Database in sync.
     */
    private suspend fun syncLiveOrderState(orderId: String, status: OrderStatus) {
        cacheManager.cacheOrderStatus(orderId, status.name)
        realtimeDatabase.publishOrderStatus(orderId, status.name)
    }
}

// ============================================================================
// Extension Mappers (Keep mapping logic out of the core repository functions)
// ============================================================================

private fun com.example.data.remote.OrderDto.toDomain(): Order {
    return Order(
        id = this.id,
        orderStatus = runCatching { OrderStatus.valueOf(this.status) }.getOrDefault(OrderStatus.PENDING),
        // ... map remaining fields
        pickupLat = 0.0,
        pickupLng = 0.0,
        dropoffLat = 0.0,
        dropoffLng = 0.0,
        vehicleType = "default"
    )
}
