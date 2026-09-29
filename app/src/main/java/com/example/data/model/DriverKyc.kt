package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Type-safe states for the driver's KYC application lifecycle.
 */
enum class KycStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * Type-safe states for the driver's current working status.
 */
enum class DriverAvailability {
    OFFLINE,
    AVAILABLE,
    BUSY
}

@Entity(tableName = "driver_kyc")
data class DriverKyc(
    @PrimaryKey
    @ColumnInfo(name = "driver_id")
    val driverId: String,

    // --- Personal Details ---
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone")
    val phone: String,

    // --- Vehicle Details ---
    @ColumnInfo(name = "vehicle_tier")
    val vehicleTier: String,
    
    @ColumnInfo(name = "vehicle_number")
    val vehicleNumber: String,

    // --- Legal & Government IDs ---
    @ColumnInfo(name = "license_number")
    val licenseNumber: String,
    
    // Security Note: In production, consider encrypting this column using SQLCipher 
    // or avoiding local plain-text storage for highly sensitive government IDs.
    @ColumnInfo(name = "aadhaar_number")
    val aadhaarNumber: String,

    // --- Status & Timestamps ---
    @ColumnInfo(name = "status")
    val status: KycStatus = KycStatus.PENDING,
    
    @ColumnInfo(name = "submission_date")
    val submissionDate: String, // Consider using a Long (epoch millis) for better DB sorting

    // --- Performance Metrics ---
    @ColumnInfo(name = "rating")
    val rating: Double = 0.0,
    
    @ColumnInfo(name = "completed_trips")
    val completedTrips: Int = 0,

    // --- Financials ---
    @ColumnInfo(name = "earnings_today")
    val earningsToday: Double = 0.0,
    
    @ColumnInfo(name = "pending_payout")
    val pendingPayout: Double = 0.0,

    // --- Real-time State ---
    @ColumnInfo(name = "availability_status")
    val availabilityStatus: DriverAvailability = DriverAvailability.OFFLINE,
    
    @ColumnInfo(name = "assigned_order_id")
    val assignedOrderId: String? = null,
    
    @ColumnInfo(name = "current_area")
    val currentArea: String? = null
)
