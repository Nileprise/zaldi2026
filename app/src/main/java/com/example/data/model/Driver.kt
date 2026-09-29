package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double = 4.8,
    val tripsCount: Int = 142,
    val vehicleType: String = VehicleType.MINI_TRUCK.name,
    val vehicleModel: String = "Tata Ace Gold",
    val vehiclePlate: String = "KA-01-MJ-4821",
    val kycStatus: String = KYC_VERIFIED,
    val isOnline: Boolean = true,
    val currentLat: Double = 12.9716,
    val currentLng: Double = 77.5946,
    val currentBookingId: Long? = null,
    val walletBalance: Double = 1450.0,
    val todayEarnings: Double = 840.0
) {
    companion object {
        const val KYC_VERIFIED = "VERIFIED"
        const val KYC_PENDING = "PENDING_REVIEW"
        const val KYC_REJECTED = "REJECTED"
    }
}
