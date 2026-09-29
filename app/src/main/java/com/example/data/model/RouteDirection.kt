package com.example.data.model

data class RouteDirectionStep(
    val stepNumber: Int,
    val instruction: String,
    val distanceText: String,
    val streetOrHighway: String,
    val iconType: String = "STRAIGHT" // "LEFT", "RIGHT", "STRAIGHT", "MERGE", "ARRIVE"
)

data class RouteDetails(
    val origin: LocationPoint,
    val destination: LocationPoint,
    val exactDistanceKm: Double,
    val estimatedDurationMin: Int,
    val primaryRouteName: String,
    val trafficCondition: String, // "NORMAL", "MODERATE", "HEAVY"
    val hasToll: Boolean,
    val steps: List<RouteDirectionStep>
) {
    companion object {
        fun buildRoute(origin: LocationPoint, destination: LocationPoint): RouteDetails {
            val dist = LocationPoint.calculateDistanceKm(
                origin.latitude, origin.longitude,
                destination.latitude, destination.longitude
            )
            val durationMin = Math.max(12, (dist * 3.2 + 8).toInt())

            val routeName = when {
                dist > 25.0 -> "Via Expressway & Outer Ring Freight Corridor"
                dist > 10.0 -> "Via Outer Ring Road / NH 44 Service Lane"
                else -> "Via City Arterial Road & Commercial Corridor"
            }

            val steps = listOf(
                RouteDirectionStep(
                    stepNumber = 1,
                    instruction = "Start from ${origin.title}, head towards main gate",
                    distanceText = "350 m",
                    streetOrHighway = origin.street.ifBlank { "Service Road" },
                    iconType = "STRAIGHT"
                ),
                RouteDirectionStep(
                    stepNumber = 2,
                    instruction = "Turn onto ${if (dist > 10) "Outer Ring Road (NH 44)" else "Main Arterial Way"}",
                    distanceText = "${(dist * 0.4).coerceAtLeast(1.2).let { String.format(java.util.Locale.US, "%.1f", it) }} km",
                    streetOrHighway = "Main Freight Route",
                    iconType = "RIGHT"
                ),
                RouteDirectionStep(
                    stepNumber = 3,
                    instruction = "Continue past the Commercial Flyover junction",
                    distanceText = "${(dist * 0.35).coerceAtLeast(0.8).let { String.format(java.util.Locale.US, "%.1f", it) }} km",
                    streetOrHighway = "Corridor Overpass",
                    iconType = "STRAIGHT"
                ),
                RouteDirectionStep(
                    stepNumber = 4,
                    instruction = "Take exit towards ${destination.area.ifBlank { destination.title }}",
                    distanceText = "850 m",
                    streetOrHighway = "Exit Ramp",
                    iconType = "LEFT"
                ),
                RouteDirectionStep(
                    stepNumber = 5,
                    instruction = "Arrive at ${destination.title}, near ${destination.landmark.ifBlank { "unloading gate" }}",
                    distanceText = "200 m",
                    streetOrHighway = destination.address,
                    iconType = "ARRIVE"
                )
            )

            return RouteDetails(
                origin = origin,
                destination = destination,
                exactDistanceKm = dist,
                estimatedDurationMin = durationMin,
                primaryRouteName = routeName,
                trafficCondition = if (durationMin > 35) "MODERATE" else "NORMAL",
                hasToll = dist > 20.0,
                steps = steps
            )
        }
    }
}
