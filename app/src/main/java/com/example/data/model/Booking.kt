package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingCode: String = "ZD-${(1000..9999).random()}",
    val customerId: String = "cust_default",
    val customerName: String = "Rajesh Sharma",
    val customerPhone: String = "+91 98765 43210",
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehicleNo: String? = null,
    val driverRating: Double = 4.8,
    val vehicleType: String = VehicleType.MINI_TRUCK.name,
    val pickupTitle: String = "Central Logistics Hub",
    val pickupAddress: String = "Gate 4, Metro Freight Corridor",
    val pickupLat: Double = 12.9716,
    val pickupLng: Double = 77.5946,
    val dropTitle: String = "Cyber City Tech Park",
    val dropAddress: String = "Tower C, Main Boulevard",
    val dropLat: Double = 12.9850,
    val dropLng: Double = 77.6250,
    val distanceKm: Double = 5.4,
    val estimatedDurationMin: Int = 22,
    val baseFare: Double = 150.0,
    val distanceFare: Double = 135.0,
    val helpersFare: Double = 0.0,
    val discountFare: Double = 0.0,
    val totalFare: Double = 285.0,
    val goodsType: String = "General Goods & Boxes",
    val goodsWeightKg: Int = 120,
    val helperCount: Int = 0,
    val status: String = STATUS_SEARCHING,
    val pickupOtp: String = "${(1000..9999).random()}",
    val paymentMethod: String = "UPI",
    val paymentStatus: String = PAYMENT_PENDING,
    val rating: Int = 0,
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val driverCurrentLat: Double = 12.9716,
    val driverCurrentLng: Double = 77.5946,
    val driverProgressPercent: Float = 0f
) {
    companion object {
        const val STATUS_CREATED = "CREATED"
        const val STATUS_SEARCHING = "SEARCHING"
        const val STATUS_ASSIGNED = "ASSIGNED"
        const val STATUS_ARRIVED_PICKUP = "ARRIVED_PICKUP"
        const val STATUS_STARTED = "STARTED"
        const val STATUS_IN_TRANSIT = "IN_TRANSIT"
        const val STATUS_DELIVERED = "DELIVERED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val PAYMENT_PENDING = "PENDING"
        const val PAYMENT_PAID = "PAID"
    }

    val isTerminalState: Boolean
        get() = status == STATUS_DELIVERED || status == STATUS_CANCELLED

    val isActiveTrip: Boolean
        get() = status in listOf(STATUS_ASSIGNED, STATUS_ARRIVED_PICKUP, STATUS_STARTED, STATUS_IN_TRANSIT)
}
