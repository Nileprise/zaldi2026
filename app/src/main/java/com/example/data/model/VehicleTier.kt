package com.example.data.model

data class VehicleTier(
    val id: String,
    val name: String,
    val capacity: String,
    val baseFare: Double,
    val perKmRate: Double,
    val etaMinutes: Int,
    val maxWeightKg: Int,
    val dimensions: String
)

object VehicleCatalog {
    val tiers = listOf(
        VehicleTier(
            id = "bike",
            name = "2-Wheeler (Bike)",
            capacity = "Up to 20 kg",
            baseFare = 49.0,
            perKmRate = 12.0,
            etaMinutes = 6,
            maxWeightKg = 20,
            dimensions = "40 x 40 x 40 cm"
        ),
        VehicleTier(
            id = "auto",
            name = "3-Wheeler Auto",
            capacity = "Up to 300 kg",
            baseFare = 99.0,
            perKmRate = 18.0,
            etaMinutes = 9,
            maxWeightKg = 300,
            dimensions = "4.5 x 3.5 x 3.5 ft"
        ),
        VehicleTier(
            id = "tata",
            name = "Tata Ace (Mini Truck)",
            capacity = "Up to 750 kg",
            baseFare = 249.0,
            perKmRate = 25.0,
            etaMinutes = 12,
            maxWeightKg = 750,
            dimensions = "7 x 4.5 x 5 ft"
        ),
        VehicleTier(
            id = "truck",
            name = "Pickup Truck (8ft)",
            capacity = "Up to 1,500 kg",
            baseFare = 449.0,
            perKmRate = 35.0,
            etaMinutes = 16,
            maxWeightKg = 1500,
            dimensions = "8.5 x 5 x 6 ft"
        )
    )

    val goodsCategories = listOf(
        "Electronics & Gadgets",
        "Fragile & Glassware",
        "Furniture & Home Goods",
        "Industrial & Hardware",
        "Documents & Parcels",
        "Textiles & Garments"
    )

    val paymentOptions = listOf(
        "UPI (Instant QR)",
        "Cards (Visa / Rupay)",
        "Cash on Delivery (COD)"
    )
}

enum class UserRole {
    CUSTOMER,
    DRIVER,
    ADMIN
}
