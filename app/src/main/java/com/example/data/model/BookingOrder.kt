package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class BookingOrder(
    @PrimaryKey
    val id: String,
    val customerPhone: String,
    val customerName: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupPlaceId: String? = null,
    val dropoffPlaceId: String? = null,
    val pickupLat: Double = 12.9784,
    val pickupLng: Double = 77.6408,
    val dropoffLat: Double = 12.9352,
    val dropoffLng: Double = 77.6245,
    val vehicleTierId: String,
    val vehicleName: String,
    val goodsType: String,
    val helperRequired: Boolean,
    val helperFee: Double,
    val fare: Double,
    val distanceKm: Double,
    val actualRoadKm: Double = distanceKm,
    val routeSummary: String = "",
    val routingProfile: String = "TATA_ACE_COMMERCIAL",
    val paymentMethod: String,
    val status: String, // SEARCHING, DRIVER_ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED
    val driverName: String = "Ravi Kumar",
    val driverPhone: String = "+91 98452 11094",
    val driverRating: Double = 4.88,
    val driverVehicleNumber: String = "KA 05 MX 2190",
    val startOtp: String = "4821",
    val timestamp: Long = System.currentTimeMillis(),
    val etaMinutes: Int = 12,
    val assignedDriverId: String? = null
)
