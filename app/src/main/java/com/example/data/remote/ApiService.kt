package com.example.data.remote

import com.example.domain.model.AllocationRequest
import com.example.domain.model.AllocationResult
import com.example.domain.model.DynamicFareResponse
import com.example.domain.model.Order
import com.example.domain.model.PricingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    // Pricing endpoints
    @POST("/api/v1/pricing/calculate-fare")
    suspend fun calculateFare(@Body request: PricingRequest): Response<DynamicFareResponse>
    
    @GET("/api/v1/pricing/vehicle-rules/{vehicleId}")
    suspend fun getVehiclePricingRules(@Path("vehicleId") vehicleId: String): Response<Map<String, Any>>
    
    // Allocation endpoints
    @POST("/api/v1/allocation/find-driver")
    suspend fun findBestDriver(@Body request: AllocationRequest): Response<AllocationResult>
    
    @GET("/api/v1/allocation/nearby-drivers")
    suspend fun getNearbyDrivers(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double,
        @Query("vehicleId") vehicleId: String
    ): Response<List<Map<String, Any>>>
    
    // Order Management endpoints
    @POST("/api/v1/orders/create")
    suspend fun createOrder(@Body order: Order): Response<Order>
    
    @GET("/api/v1/orders/{orderId}")
    suspend fun getOrder(@Path("orderId") orderId: String): Response<Order>
    
    @POST("/api/v1/orders/{orderId}/assign-driver")
    suspend fun assignDriver(
        @Path("orderId") orderId: String,
        @Body payload: Map<String, String>
    ): Response<Order>
    
    @POST("/api/v1/orders/{orderId}/update-status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body payload: Map<String, String>
    ): Response<Order>
    
    @POST("/api/v1/orders/{orderId}/complete")
    suspend fun completeOrder(
        @Path("orderId") orderId: String,
        @Body payload: Map<String, Any>
    ): Response<Order>
    
    @POST("/api/v1/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body payload: Map<String, String>
    ): Response<Order>
    
    @GET("/api/v1/orders/customer/{customerId}")
    suspend fun getCustomerOrders(@Path("customerId") customerId: String): Response<List<Order>>
    
    @GET("/api/v1/orders/driver/{driverId}")
    suspend fun getDriverOrders(@Path("driverId") driverId: String): Response<List<Order>>
}
