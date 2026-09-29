package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promo_codes")
data class PromoCode(
    @PrimaryKey
    val code: String,
    val discountPercent: Int = 0,
    val flatDiscount: Double = 0.0,
    val maxDiscount: Double = 100.0,
    val minOrderValue: Double = 100.0,
    val description: String = "Special discount",
    val isActive: Boolean = true
) {
    fun calculateDiscount(fare: Double): Double {
        if (!isActive || fare < minOrderValue) return 0.0
        val discount = if (flatDiscount > 0) {
            flatDiscount
        } else {
            (fare * discountPercent) / 100.0
        }
        return Math.min(discount, maxDiscount)
    }
}
