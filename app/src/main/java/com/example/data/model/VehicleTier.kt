package com.example.data.model

import com.example.util.RoutingProfile

data class VehicleTier(
    val id: String,
    val name: String,
    val capacity: String,
    val baseFare: Double,
    val perKmRate: Double,
    val etaMinutes: Int,
    val maxWeightKg: Int,
    val dimensions: String,
    val routingProfile: RoutingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
)

object VehicleCatalog {
    val tiers = listOf(
        VehicleTier(
            id = "bike",
            name = "2-Wheeler (Bike)",
            capacity = "Up to 20 kg",
            baseFare = 45.0,
            perKmRate = 10.0,
            etaMinutes = 6,
            maxWeightKg = 20,
            dimensions = "40 x 40 x 40 cm",
            routingProfile = RoutingProfile.TWO_WHEELER
        ),
        VehicleTier(
            id = "auto",
            name = "3-Wheeler Auto",
            capacity = "Up to 300 kg",
            baseFare = 99.0,
            perKmRate = 16.0,
            etaMinutes = 8,
            maxWeightKg = 300,
            dimensions = "4.5 x 3.5 x 3.5 ft",
            routingProfile = RoutingProfile.THREE_WHEELER
        ),
        VehicleTier(
            id = "tata",
            name = "Tata Ace (Chota Hathi)",
            capacity = "Up to 750 kg",
            baseFare = 249.0,
            perKmRate = 24.0,
            etaMinutes = 11,
            maxWeightKg = 750,
            dimensions = "7 x 4.5 x 5 ft",
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "truck",
            name = "Pickup Truck (8ft)",
            capacity = "Up to 1,500 kg",
            baseFare = 449.0,
            perKmRate = 32.0,
            etaMinutes = 14,
            maxWeightKg = 1500,
            dimensions = "8.5 x 5 x 6 ft",
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "truck_14ft",
            name = "14ft Truck (Tata 407)",
            capacity = "Up to 2,500 kg",
            baseFare = 699.0,
            perKmRate = 40.0,
            etaMinutes = 18,
            maxWeightKg = 2500,
            dimensions = "14 x 6 x 6.5 ft",
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "heavy_hauler",
            name = "Heavy Hauler (Multi-Axle)",
            capacity = "Up to 5,000 kg",
            baseFare = 1199.0,
            perKmRate = 52.0,
            etaMinutes = 24,
            maxWeightKg = 5000,
            dimensions = "19 x 7 x 7.5 ft",
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        )
    )

    val goodsCategories = listOf(
        "Electronics & Gadgets",
        "Fragile & Glassware",
        "Furniture & Home Goods",
        "Industrial & Hardware",
        "Documents & Parcels",
        "Textiles & Garments",
        "FMCG & Groceries"
    )

    val paymentOptions = listOf(
        "UPI (Instant QR)",
        "Cards (Visa / Rupay)",
        "Cash on Delivery (COD)",
        "Corporate Account"
    )
}

enum class UserRole {
    CUSTOMER,
    DRIVER,
    ADMIN
}
