package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriverLocationData(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float = 0f,
    val accuracyMeters: Float = 5.0f,
    val altitude: Double = 920.0,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveGps: Boolean = true
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val formattedCoordinates: String
        get() = String.format(Locale.US, "%.5f° N, %.5f° E", latitude, longitude)
}
