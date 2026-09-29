package com.example.data.model

import com.example.util.RoutingProfile
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class UserRole {
    CUSTOMER,
    DRIVER,
    ADMIN
}

enum class MeasurementUnit(val symbol: String) {
    CENTIMETERS("cm"),
    FEET("ft")
}

data class Dimensions(
    val length: Double,
    val width: Double,
    val height: Double,
    val unit: MeasurementUnit
) {
    /** Generates a UI-ready string, e.g., "7.0 x 4.5 x 5.0 ft" */
    fun toDisplayString(): String = "$length x $width x $height ${unit.symbol}"
}

data class VehicleTier(
    val id: String,
    val name: String,
    val capacityDescription: String,
    
    // Future-Proofing: Always use BigDecimal for financial calculations
    val baseFare: BigDecimal,
    val perKmRate: BigDecimal,
    
    // Future-Proofing: Use Kotlin Duration API to prevent unit-conversion bugs
    val baseEta: Duration,
    
    val maxWeightKg: Int,
    
    // Future-Proofing: Structured data instead of raw strings for easy filtering
    val dimensions: Dimensions,
    
    val routingProfile: RoutingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
)

/**
 * Mock data provider. 
 * In a production app, this acts as default/fallback data if the remote config/API fails to load.
 */
object VehicleCatalog {
    val tiers = listOf(
        VehicleTier(
            id = "bike",
            name = "2-Wheeler (Bike)",
            capacityDescription = "Up to 20 kg",
            baseFare = BigDecimal("45.00"),
            perKmRate = BigDecimal("10.00"),
            baseEta = 6.minutes,
            maxWeightKg = 20,
            dimensions = Dimensions(40.0, 40.0, 40.0, MeasurementUnit.CENTIMETERS),
            routingProfile = RoutingProfile.TWO_WHEELER
        ),
        VehicleTier(
            id = "auto",
            name = "3-Wheeler Auto",
            capacityDescription = "Up to 300 kg",
            baseFare = BigDecimal("99.00"),
            perKmRate = BigDecimal("16.00"),
            baseEta = 8.minutes,
            maxWeightKg = 300,
            dimensions = Dimensions(4.5, 3.5, 3.5, MeasurementUnit.FEET),
            routingProfile = RoutingProfile.THREE_WHEELER
        ),
        VehicleTier(
            id = "tata",
            name = "Tata Ace (Chota Hathi)",
            capacityDescription = "Up to 750 kg",
            baseFare = BigDecimal("249.00"),
            perKmRate = BigDecimal("24.00"),
            baseEta = 11.minutes,
            maxWeightKg = 750,
            dimensions = Dimensions(7.0, 4.5, 5.0, MeasurementUnit.FEET),
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "truck",
            name = "Pickup Truck (8ft)",
            capacityDescription = "Up to 1,500 kg",
            baseFare = BigDecimal("449.00"),
            perKmRate = BigDecimal("32.00"),
            baseEta = 14.minutes,
            maxWeightKg = 1500,
            dimensions = Dimensions(8.5, 5.0, 6.0, MeasurementUnit.FEET),
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "truck_14ft",
            name = "14ft Truck (Tata 407)",
            capacityDescription = "Up to 2,500 kg",
            baseFare = BigDecimal("699.00"),
            perKmRate = BigDecimal("40.00"),
            baseEta = 18.minutes,
            maxWeightKg = 2500,
            dimensions = Dimensions(14.0, 6.0, 6.5, MeasurementUnit.FEET),
            routingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
        ),
        VehicleTier(
            id = "heavy_hauler",
            name = "Heavy Hauler (Multi-Axle)",
            capacityDescription = "Up to 5,000 kg",
            baseFare = BigDecimal("1199.00"),
            perKmRate = BigDecimal("52.00"),
            baseEta = 24.minutes,
            maxWeightKg = 5000,
            dimensions = Dimensions(19.0, 7.0, 7.5, MeasurementUnit.FEET),
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

    // Using an Enum for this in the future is highly recommended if you 
    // need to run logic based on payment type (e.g., hiding COD for orders > ₹5000).
    val paymentOptions = listOf(
        "UPI (Instant QR)",
        "Cards (Visa / Rupay)",
        "Cash on Delivery (COD)",
        "Corporate Account"
    )
}
