package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pricing_rules")
data class PricingRule(
    @PrimaryKey
    val vehicleType: String,
    val baseFare: Double,
    val perKmFare: Double,
    val minFare: Double,
    val waitingChargePerMin: Double = 3.0
)
