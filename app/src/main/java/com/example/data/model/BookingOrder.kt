package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Type-safe states for the order lifecycle.
 * Room automatically converts these to/from Strings in the database.
 */
enum class OrderStatus {
    PENDING,
    SEARCHING,
    DRIVER_ASSIGNED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}

@Entity(tableName = "orders")
data class BookingOrder(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    // --- Customer Details ---
    @ColumnInfo(name = "customer_phone")
    val customerPhone: String,
    @ColumnInfo(name = "customer_name")
    val customerName: String,

    // --- Location: Pickup ---
    @ColumnInfo(name = "pickup_address")
    val pickupAddress: String,
    @ColumnInfo(name = "pickup_place_id")
    val pickupPlaceId: String? = null,
    @ColumnInfo(name = "pickup_lat")
    val pickupLat: Double = 0.0,
    @ColumnInfo(name = "pickup_lng")
    val pickupLng: Double = 0.0,

    // --- Location: Dropoff ---
    @ColumnInfo(name = "dropoff_address")
    val dropoffAddress: String,
    @ColumnInfo(name = "dropoff_place_id")
    val dropoffPlaceId: String? = null,
    @ColumnInfo(name = "dropoff_lat")
    val dropoffLat: Double = 0.0,
    @ColumnInfo(name = "dropoff_lng")
    val dropoffLng: Double = 0.0,

    // --- Vehicle & Goods Details ---
    @ColumnInfo(name = "vehicle_tier_id")
    val vehicleTierId: String,
    @ColumnInfo(name = "vehicle_name")
    val vehicleName: String,
    @ColumnInfo(name = "goods_type")
    val goodsType: String,
    
    // --- Routing & Pricing ---
    @ColumnInfo(name = "routing_profile")
    val routingProfile: String = "DEFAULT",
    @ColumnInfo(name = "route_summary")
    val routeSummary: String = "",
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double,
    @ColumnInfo(name = "actual_road_km")
    val actualRoadKm: Double = 0.0,
    @ColumnInfo(name = "fare")
    val fare: Double,
    @ColumnInfo(name = "helper_required")
    val helperRequired: Boolean = false,
    @ColumnInfo(name = "helper_fee")
    val helperFee: Double = 0.0,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,

    // --- Status & Metadata ---
    @ColumnInfo(name = "status")
    val status: OrderStatus = OrderStatus.PENDING,
    @ColumnInfo(name = "start_otp")
    val startOtp: String? = null,
    @ColumnInfo(name = "eta_minutes")
    val etaMinutes: Int? = null,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    // --- Assigned Driver Details (Nullable for unassigned orders) ---
    @ColumnInfo(name = "assigned_driver_id")
    val assignedDriverId: String? = null,
    @ColumnInfo(name = "driver_name")
    val driverName: String? = null,
    @ColumnInfo(name = "driver_phone")
    val driverPhone: String? = null,
    @ColumnInfo(name = "driver_rating")
    val driverRating: Double? = null,
    @ColumnInfo(name = "driver_vehicle_number")
    val driverVehicleNumber: String? = null
)
