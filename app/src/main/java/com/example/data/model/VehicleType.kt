package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RvHookup
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

enum class VehicleType(
    val title: String,
    val subtitle: String,
    val capacityKg: Int,
    val defaultBaseFare: Double,
    val defaultPerKmFare: Double,
    val sizeDescription: String,
    val idealFor: String
) {
    BIKE(
        title = "Bike",
        subtitle = "Fastest for small parcels",
        capacityKg = 20,
        defaultBaseFare = 40.0,
        defaultPerKmFare = 10.0,
        sizeDescription = "Up to 20 kg • 40x40x40 cm",
        idealFor = "Documents, food, small electronics, apparel"
    ),
    AUTO(
        title = "Auto Cargo",
        subtitle = "Affordable 3-wheeler freight",
        capacityKg = 350,
        defaultBaseFare = 60.0,
        defaultPerKmFare = 14.0,
        sizeDescription = "Up to 350 kg • 3.5 ft open/covered",
        idealFor = "Medium boxes, cartons, small appliances"
    ),
    MINI_TRUCK(
        title = "Mini Truck (7ft)",
        subtitle = "Best for home & shop shifting",
        capacityKg = 1000,
        defaultBaseFare = 150.0,
        defaultPerKmFare = 25.0,
        sizeDescription = "Up to 1,000 kg • 7ft Tata Ace / Bolero",
        idealFor = "Furniture, refrigerators, commercial freight"
    ),
    TRUCK(
        title = "Truck (14ft)",
        subtitle = "Heavy industrial & bulk loads",
        capacityKg = 3500,
        defaultBaseFare = 300.0,
        defaultPerKmFare = 40.0,
        sizeDescription = "Up to 3,500 kg • 14ft Eicher / Cargo",
        idealFor = "House relocation, construction, warehouse distribution"
    ),
    LARGE_TRUCK(
        title = "Large Truck (19ft)",
        subtitle = "Multi-axle container capacity",
        capacityKg = 8000,
        defaultBaseFare = 600.0,
        defaultPerKmFare = 65.0,
        sizeDescription = "Up to 8,000 kg • Heavy Container",
        idealFor = "Industrial equipment, wholesale supply, intercity freight"
    );

    fun getIcon(): ImageVector {
        return when (this) {
            BIKE -> Icons.Default.TwoWheeler
            AUTO -> Icons.Default.ElectricRickshaw
            MINI_TRUCK -> Icons.Default.LocalShipping
            TRUCK -> Icons.Default.LocalShipping
            LARGE_TRUCK -> Icons.Default.RvHookup
        }
    }
}
