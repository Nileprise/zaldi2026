Here is the modernized, production-ready version of your DriverKyc data class.
I have applied similar modern Android architecture practices as before, plus a crucial security consideration for local databases:
 * Type-Safe Enums: Replaced the raw strings for status and availabilityStatus with Kotlin Enums (KycStatus and DriverAvailability). This prevents invalid states from being saved to the database. Room natively handles saving enums as strings or integers.
 * Explicit SQL Naming: Added @ColumnInfo(name = "snake_case") to all properties. This decouples your Kotlin variable names from your database schema, making future refactoring safe and preventing migration crashes.
 * Removed Hardcoded Mocks: Default values like "Indiranagar, Bengaluru", 142 trips, and 4.85 rating have been replaced with proper logical defaults (null, 0, 0.0).
 * Security Best Practice (PII): Added a note regarding the aadhaarNumber. In a production app, storing sensitive, highly-regulated government IDs in plain text inside a local SQLite (Room) database is a security risk. You should typically encrypt these fields using EncryptedSharedPreferences or SQLCipher if local caching is strictly necessary.
Modernized DriverKyc.kt
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

