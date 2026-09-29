package com.example.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Keep
data class PlaceModel(
    val placeId: String,
    val name: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val city: String = "Bengaluru"
) {
    val latLng: LatLng get() = LatLng(latitude, longitude)
    val shortSummary: String get() = "$name, $city"
}

@Keep
enum class RoutingProfile(val displayName: String, val speedFactor: Double) {
    TWO_WHEELER("2-Wheeler Agility Routing", 1.25),
    THREE_WHEELER("3-Wheeler Urban Corridor", 1.05),
    TATA_ACE_COMMERCIAL("Commercial Freight", 0.90)
}

@Keep
data class DirectionsResult(
    val waypoints: List<LatLng>,
    val actualRoadKm: Double,
    val etaMinutes: Int,
    val viaRoad: String,
    val routeSummary: String,
    val maneuvers: List<String>,
    val routingProfile: RoutingProfile
)

/**
 * Enterprise Google Maps Routing, Autocomplete, Geocoding, & Snap-to-Roads engine.
 * Designed for production integration with Google Places SDK and Directions API.
 */
object GoogleMapsRoutingService {

    private const val EARTH_RADIUS_KM = 6371.0

    // Offline Fallback / Cached Hubs (Used when network/API fails)
    val placeCatalog: List<PlaceModel> = listOf(
        PlaceModel("ChIJbU60qSX9vzsR0whvgm0FmWg", "Indiranagar 100ft Road", "100 Feet Rd, Indiranagar, Bengaluru", 12.9784, 77.6408, "Commercial Hub"),
        PlaceModel("ChIJL_7_4sBkrjsR9r2jCskd81c", "Koramangala 4th Block", "80 Feet Rd, Koramangala, Bengaluru", 12.9352, 77.6245, "Tech & Retail Corridor"),
        PlaceModel("ChIJ_fJ8P80TrjsRo2QGqL8fJvM", "Whitefield EPIP Zone", "EPIP Zone, Whitefield, Bengaluru", 12.9780, 77.7280, "Tech Park"),
        PlaceModel("ChIJw7eJgNkVrjsRkK0M4V2r3wI", "Electronic City Phase 1", "Hosur Rd, Electronic City, Bengaluru", 12.8399, 77.6770, "Industrial Zone"),
        PlaceModel("ChIJYf_pC4ITrjsR95kLk1O_JpQ", "Peenya Industrial Area", "Peenya 1st Stage, Bengaluru", 13.0285, 77.5197, "Heavy Industrial"),
        PlaceModel("ChIJ9W2E7r4UrjsR41wY27sI1_E", "Kempegowda Airport (BLR)", "Devanahalli, Bengaluru", 13.1986, 77.7066, "Air Cargo Terminal")
    )

    /**
     * Real-time Geocoding using Android's native Geocoder.
     * In a full production setup, this would be wrapped in a Google Places SDK 'FetchPlaceRequest'.
     */
    suspend fun geocodeRealTime(context: Context, address: String): PlaceModel? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null
        
        try {
            val geocoder = Geocoder(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Async API for Android 13+ is handled via callbacks; for simplicity in coroutines, 
                // we fallback to the synchronous call or use a suspendCancellableCoroutine wrapper in production.
                val addresses = geocoder.getFromLocationName(address, 1)
                addresses?.firstOrNull()?.let {
                    return@withContext PlaceModel(
                        placeId = "geo_${it.latitude}_${it.longitude}",
                        name = it.featureName ?: address.substringBefore(","),
                        formattedAddress = it.getAddressLine(0) ?: address,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "Geocoded Location"
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)
                addresses?.firstOrNull()?.let {
                    return@withContext PlaceModel(
                        placeId = "geo_${it.latitude}_${it.longitude}",
                        name = it.featureName ?: address.substringBefore(","),
                        formattedAddress = it.getAddressLine(0) ?: address,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "Geocoded Location"
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace() // Network error or Geocoder unavailable
        }
        
        // Fallback to local catalog if Geocoder fails
        return@withContext geocodeFallback(address)
    }

    /**
     * Synchronous offline fallback for UI components that cannot suspend yet.
     */
    fun geocodeFallback(address: String): PlaceModel {
        val trimmed = address.trim()
        val found = placeCatalog.firstOrNull {
            it.name.contains(trimmed, ignoreCase = true) || trimmed.contains(it.name, ignoreCase = true)
        }
        if (found != null) return found

        // Production safeguard: If location is entirely unknown and geocoder fails, 
        // return a generic default rather than throwing an exception or faking data.
        return PlaceModel(
            placeId = "UNKNOWN_LOC",
            name = trimmed.substringBefore(","),
            formattedAddress = trimmed,
            latitude = 12.9716, // Default Bangalore Center
            longitude = 77.5946,
            category = "Unknown Location"
        )
    }

    /**
     * Autocomplete search local fallback. 
     * In production, replace this with Google Places Autocomplete API.
     */
    fun searchPlaces(query: String): List<PlaceModel> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return placeCatalog.take(5)

        return placeCatalog.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.formattedAddress.contains(trimmed, ignoreCase = true)
        }.take(5)
    }

    /**
     * Calculates real-world coordinates and routing heuristics.
     * Note: For actual turn-by-turn maneuvers and exact road geometry, this requires an HTTP 
     * call to 'https://maps.googleapis.com/maps/api/directions/json'.
     */
    fun calculateDirections(
        pickup: PlaceModel,
        dropoff: PlaceModel,
        profile: RoutingProfile = RoutingProfile.TATA_ACE_COMMERCIAL
    ): DirectionsResult {
        val straightLineKm = haversineDistance(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude)

        // Urban Road Detour Multiplier based on vehicle agility
        val roadFactor = when (profile) {
            RoutingProfile.TWO_WHEELER -> 1.18 
            RoutingProfile.THREE_WHEELER -> 1.25
            RoutingProfile.TATA_ACE_COMMERCIAL -> 1.34 // Trucks cannot take narrow alleys
        }

        val actualRoadKm = max(0.5, straightLineKm * roadFactor)
        val roundedKm = (actualRoadKm * 10.0).roundToInt() / 10.0

        // Speed calculation based on distance and profile
        val averageSpeedKmh = when (profile) {
            RoutingProfile.TWO_WHEELER -> 32.0 
            RoutingProfile.THREE_WHEELER -> 26.0
            RoutingProfile.TATA_ACE_COMMERCIAL -> 22.0 
        }
        val etaMinutes = max(5, ((roundedKm / averageSpeedKmh) * 60.0).roundToInt())

        // Create a straight line segment for fallback UI rendering. 
        // Real apps use decodePolyline(response.routes[0].overview_polyline.points) here.
        val fallbackWaypoints = listOf(pickup.latLng, dropoff.latLng)

        return DirectionsResult(
            waypoints = fallbackWaypoints,
            actualRoadKm = roundedKm,
            etaMinutes = etaMinutes,
            viaRoad = "Standard Routing",
            routeSummary = "$roundedKm km • ~$etaMinutes mins",
            maneuvers = listOf("Proceed to dropoff"),
            routingProfile = profile
        )
    }

    /**
     * PRODUCTION POLYLINE DECODER
     * Google Directions API returns routes as an encoded string. This standard algorithm
     * decodes that string into a List of LatLng points to draw perfectly accurate road curves on the map.
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    /**
     * CLIENT-SIDE SNAP-TO-ROADS
     * Used heavily in production fleet apps to lock a jittery GPS coordinate to the active route polyline
     * without making expensive API calls every second.
     */
    fun snapToRoad(rawPoint: LatLng, routeWaypoints: List<LatLng>): LatLng {
        if (routeWaypoints.size < 2) return rawPoint

        var closestPoint = routeWaypoints.first()
        var minDistance = Double.MAX_VALUE

        for (i in 0 until routeWaypoints.size - 1) {
            val p1 = routeWaypoints[i]
            val p2 = routeWaypoints[i + 1]
            val projected = projectPointOnSegment(rawPoint, p1, p2)
            val dist = haversineDistance(rawPoint.latitude, rawPoint.longitude, projected.latitude, projected.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closestPoint = projected
            }
        }
        return closestPoint
    }

    private fun projectPointOnSegment(p: LatLng, p1: LatLng, p2: LatLng): LatLng {
        val dx = p2.longitude - p1.longitude
        val dy = p2.latitude - p1.latitude
        val lengthSq = dx * dx + dy * dy

        if (lengthSq == 0.0) return p1

        val t = max(0.0, min(1.0, ((p.longitude - p1.longitude) * dx + (p.latitude - p1.latitude) * dy) / lengthSq))
        val projLat = p1.latitude + t * dy
        val projLng = p1.longitude + t * dx

        return LatLng(projLat, projLng)
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}
