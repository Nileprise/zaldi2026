package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationUtils {

    /**
     * Resolves geographic coordinates for delivery destinations based on address names.
     */
    fun getDestinationCoordinates(address: String): Pair<Double, Double> {
        return when {
            address.contains("Koramangala", ignoreCase = true) -> 12.9352 to 77.6245
            address.contains("MG Road", ignoreCase = true) -> 12.9756 to 77.6066
            address.contains("Rajajinagar", ignoreCase = true) -> 12.9982 to 77.5530
            address.contains("Electronic City", ignoreCase = true) -> 12.8399 to 77.6770
            address.contains("Whitefield", ignoreCase = true) -> 12.9698 to 77.7499
            address.contains("Indiranagar", ignoreCase = true) -> 12.9784 to 77.6408
            address.contains("Peenya", ignoreCase = true) -> 13.0285 to 77.5197
            address.contains("HSR", ignoreCase = true) -> 12.9121 to 77.6446
            else -> 12.9352 to 77.6245 // Default Bengaluru delivery destination
        }
    }

    /**
     * Launches the default maps application with the delivery destination coordinates.
     * Uses the standard geo URI scheme, with fallback to Google Maps web directions URL.
     */
    fun launchMapsNavigation(
        context: Context,
        destinationLatitude: Double,
        destinationLongitude: Double,
        destinationLabel: String = "Delivery Destination"
    ) {
        val encodedLabel = Uri.encode(destinationLabel)
        // Standard Android geo URI with coordinates and query label for destination navigation
        val geoUri = Uri.parse("geo:$destinationLatitude,$destinationLongitude?q=$destinationLatitude,$destinationLongitude($encodedLabel)")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to web maps directions URL if no native geo handler is available
            try {
                val webUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=$destinationLatitude,$destinationLongitude"
                )
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (fallbackEx: Exception) {
                Toast.makeText(
                    context,
                    "Unable to launch maps navigation: ${fallbackEx.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
