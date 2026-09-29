package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Modernized Retrofit API Interface.
 * 
 * Note: Returns direct DTOs instead of Response<T>. 
 * Network errors (4xx, 5xx) will throw HttpException, which should be caught 
 * in the Repository layer using a Result wrapper or runCatching block.
 */
interface ApiService {
    
    // --- Pricing Endpoints ---
    
    @POST("/api/v1/pricing/calculate-fare")
    suspend fun calculateFare(
        @Body request: PricingRequestDto
    ): DynamicFareResponseDto
    
    @GET("/api/v1/pricing/vehicle-rules/{vehicleId}")
    suspend fun getVehiclePricingRules(
        @Path("vehicleId") vehicleId: String
    ): VehiclePricingRulesDto
    
    
    // --- Allocation Endpoints ---
    
    @POST("/api/v1/allocation/find-driver")
    suspend fun findBestDriver(
        @Body request: AllocationRequestDto
    ): AllocationResultDto
    
    @GET("/api/v1/allocation/nearby-drivers")
    suspend fun getNearbyDrivers(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double,
        @Query("vehicleId") vehicleId: String
    ): List<NearbyDriverDto>
    
    
    // --- Order Management Endpoints ---
    
    @POST("/api/v1/orders/create")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): OrderDto
    
    @GET("/api/v1/orders/{orderId}")
    suspend fun getOrder(
        @Path("orderId") orderId: String
    ): OrderDto
    
    @POST("/api/v1/orders/{orderId}/assign-driver")
    suspend fun assignDriver(
        @Path("orderId") orderId: String,
        @Body request: AssignDriverRequest
    ): OrderDto
    
    @POST("/api/v1/orders/{orderId}/update-status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateOrderStatusRequest
    ): OrderDto
    
    @POST("/api/v1/orders/{orderId}/complete")
    suspend fun completeOrder(
        @Path("orderId") orderId: String,
        @Body request: CompleteOrderRequest
    ): OrderDto
    
    @POST("/api/v1/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body request: CancelOrderRequest
    ): OrderDto
    
    @GET("/api/v1/orders/customer/{customerId}")
    suspend fun getCustomerOrders(
        @Path("customerId") customerId: String
    ): List<OrderDto>
    
    @GET("/api/v1/orders/driver/{driverId}")
    suspend fun getDriverOrders(
        @Path("driverId") driverId: String
    ): List<OrderDto>
}

// ============================================================================
// DTOs (Data Transfer Objects) - Usually placed in a separate /dto package
// ============================================================================

// Type-safe replacements for untyped Map<String, String> payloads

data class AssignDriverRequest(
    val driverId: String,
    val vehicleId: String,
    val estimatedArrivalMinutes: Int
)

data class UpdateOrderStatusRequest(
    val status: String, // Or an Enum if Moshi/Gson is configured to parse it
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class CompleteOrderRequest(
    val completionNotes: String? = null,
    val finalDistanceKm: Double,
    val finalFareAmount: Double,
    val signatureUrl: String? = null
)

data class CancelOrderRequest(
    val reasonCode: String,
    val detailedReason: String? = null,
    val cancelledByRole: String // e.g., "CUSTOMER", "DRIVER", "ADMIN"
)

// Network representations of your Domain Models

data class OrderDto(
    val id: String,
    val status: String,
    val customerId: String,
    val driverId: String?,
    // ... other network fields mapped to JSON
)

data class NearbyDriverDto(
    val driverId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val distanceKm: Double,
    val vehicleTierId: String
)

data class VehiclePricingRulesDto(
    val baseFare: Double,
    val perKmRate: Double,
    val surgeMultiplier: Double,
    val activeTolls: List<String>
)

// Placeholders for your existing Requests/Responses migrated to DTOs
class PricingRequestDto(/*...*/)
class DynamicFareResponseDto(/*...*/)
class AllocationRequestDto(/*...*/)
class AllocationResultDto(/*...*/)
class CreateOrderRequest(/*...*/)
