package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_kyc")
data class DriverKyc(
    @PrimaryKey
    val driverId: String,
    val name: String,
    val phone: String,
    val vehicleTier: String,
    val vehicleNumber: String,
    val licenseNumber: String,
    val aadhaarNumber: String,
    val status: String, // APPROVED, PENDING, REJECTED
    val submissionDate: String,
    val rating: Double = 4.85,
    val completedTrips: Int = 142,
    val earningsToday: Double = 1840.0,
    val pendingPayout: Double = 6420.0,
    val availabilityStatus: String = "AVAILABLE", // AVAILABLE, BUSY, OFFLINE
    val assignedOrderId: String? = null,
    val currentArea: String = "Indiranagar, Bengaluru"
)
