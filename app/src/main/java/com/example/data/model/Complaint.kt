package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingCode: String,
    val submittedByRole: String, // "CUSTOMER" or "DRIVER"
    val subject: String,
    val description: String,
    val status: String = STATUS_OPEN, // "OPEN" or "RESOLVED"
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_OPEN = "OPEN"
        const val STATUS_RESOLVED = "RESOLVED"
    }
}

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val details: String,
    val category: String, // "DISPATCH", "PAYMENT", "PRICING", "SECURITY"
    val timestamp: Long = System.currentTimeMillis()
)
