package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Enterprise-grade utility for launching external navigation applications.
 */
object NavigationUtils {

    /**
     * Resolves geographic coordinates dynamically using the central GoogleMapsRoutingService.
     * 
     * [REMOVED]: Hardcoded demo dictionary of Bangalore locations.
     */
    @Deprecated(
        message = "Do not resolve coordinates by string at the UI layer. Pass exact dropoffLat/dropoffLng directly from the BookingOrder.",
        replaceWith = ReplaceWith("order.dropoffLat to order.dropoffLng")
    )
    fun getDestinationCoordinates(address: String): Pair<Double, Double> {
        // Real-time dynamic fallback instead of hardcoded strings
        val place = GoogleMapsRoutingService.geocodeFallback(address)
        return Pair(place.latitude, place.longitude)
    }

    /**
     * Launches Turn-by-Turn Google Maps Navigation.
     * 
     * In a production fleet app, this prioritizes the direct `google.navigation` scheme 
     * over the generic `geo:` scheme to instantly start driving directions without 
     * requiring the driver to manually click "Start".
     */
    fun launchMapsNavigation(
        context: Context,
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationLabel: String = "Delivery Destination"
    ) {
        // 1. Primary: Direct Turn-by-Turn Navigation URI
        // 'mode=d' specifies driving mode. Use 'mode=l' for two-wheeler routing in supported regions.
        val navUri = Uri.parse("google.navigation:q=$destinationLatitude,$destinationLongitude&mode=d")
        val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            // Force Google Maps package to bypass the Android "Open with..." intent chooser dialog
            setPackage("com.google.android.apps.maps") 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        try {
            // Attempt to launch Google Maps Turn-by-Turn directly
            context.startActivity(navIntent)
        } catch (e: ActivityNotFoundException) {
            
            // 2. First Fallback: Generic Geo Intent
            // Triggered if the official Google Maps app is uninstalled, disabled, or restricted.
            val encodedLabel = Uri.encode(destinationLabel)
            val geoUri = Uri.parse("geo:$destinationLatitude,$destinationLongitude?q=$destinationLatitude,$destinationLongitude($encodedLabel)")
            val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            try {
                context.startActivity(geoIntent)
            } catch (fallbackEx: ActivityNotFoundException) {
                
                // 3. Last Resort Fallback: Web Browser Directions
                // Triggered if the device has absolutely no map applications installed.
                try {
                    val webUri = Uri.parse(
                        "https://www.google.com/maps/dir/?api=1&destination=$destinationLatitude,$destinationLongitude"
                    )
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(webIntent)
                } catch (webEx: ActivityNotFoundException) {
                    // Total failure (No Maps, No Browser)
                    Toast.makeText(
                        context,
                        "Unable to launch navigation: No maps app or web browser found.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
