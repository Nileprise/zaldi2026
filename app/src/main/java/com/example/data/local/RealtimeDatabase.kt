Here is the modernized version of your RealtimeDatabase class.
I have applied several major Android and Kotlin best practices for Firebase integration:
 * callbackFlow for Realtime Streams: Replaced the legacy callback interfaces (ValueEventListener) with Kotlin's callbackFlow. This natively converts Firebase streams into standard Kotlin Flows that your UI or ViewModel can easily collect.
 * Memory Leak Prevention: Used awaitClose { ref.removeEventListener(...) } inside the callbackFlow. This ensures that the Firebase listener is automatically detached the exact moment the coroutine collecting the Flow is cancelled (e.g., when the user leaves the screen).
 * Coroutines for Writes (await()): Changed the publish methods to suspend functions and added .await() (from kotlinx-coroutines-play-services). This ensures you know exactly when a write operation succeeds or fails, without blocking the thread.
 * Dependency Injection: Passed the FirebaseDatabase instance through the constructor to make the class easily testable and compatible with DI frameworks like Hilt or Koin.
 * Removed Dead StateFlows: The original _driverLocations and _orderUpdates StateFlows were declared but never updated. Modern architecture favors returning a scoped Flow for the specific ID requested rather than keeping a global map of all active streams.
Modernized RealtimeDatabaseManager.kt
package com.example.data.local

import com.example.domain.model.DriverLocationPing
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Modernized Firebase Realtime Database wrapper.
 * Inject this class as a Singleton using your preferred DI framework.
 */
class RealtimeDatabaseManager(
    private val database: FirebaseDatabase = Firebase.database
) {
    private val dbRef = database.reference

    companion object {
        private const val NODE_DRIVERS = "drivers"
        private const val NODE_LOCATION = "location"
        private const val NODE_ORDERS = "orders"
        private const val NODE_STATUS = "status"
    }

    /**
     * Publishes a driver's location.
     * Uses suspend and .await() to ensure the write completes asynchronously.
     */
    suspend fun publishDriverLocation(driverId: String, ping: DriverLocationPing) {
        val locationData = mapOf(
            "lat" to ping.lat,
            "lng" to ping.lng,
            "accuracy" to ping.accuracy,
            "timestamp" to ping.timestamp
        )

        dbRef.child(NODE_DRIVERS)
            .child(driverId)
            .child(NODE_LOCATION)
            .setValue(locationData)
            .await() // Suspends until Firebase confirms the write
    }

    /**
     * Subscribes to a specific driver's location updates.
     * Uses callbackFlow to automatically manage the ValueEventListener lifecycle.
     */
    fun getDriverLocationStream(driverId: String): Flow<DriverLocationPing> = callbackFlow {
        val locationRef = dbRef.child(NODE_DRIVERS).child(driverId).child(NODE_LOCATION)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Safely parse the snapshot, falling back to defaults if data is missing
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: 0.0
                val accuracy = snapshot.child("accuracy").getValue(Float::class.java) ?: 10f
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                val ping = DriverLocationPing(
                    driverId = driverId,
                    lat = lat,
                    lng = lng,
                    accuracy = accuracy,
                    timestamp = timestamp
                )

                // Emit the value to the Flow
                trySend(ping)
            }

            override fun onCancelled(error: DatabaseError) {
                // Close the Flow with an exception if Firebase fails
                close(error.toException())
            }
        }

        locationRef.addValueEventListener(listener)

        // CRITICAL: Automatically detaches the listener when the Flow collection is cancelled
        // This prevents memory leaks and background battery drain.
        awaitClose { 
            locationRef.removeEventListener(listener) 
        }
    }

    /**
     * Updates an order's status.
     */
    suspend fun publishOrderStatus(orderId: String, status: String) {
        dbRef.child(NODE_ORDERS)
            .child(orderId)
            .child(NODE_STATUS)
            .setValue(status)
            .await()
    }

    /**
     * Subscribes to realtime status updates for a specific order.
     */
    fun getOrderStatusStream(orderId: String): Flow<String> = callbackFlow {
        val statusRef = dbRef.child(NODE_ORDERS).child(orderId).child(NODE_STATUS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: ""
                trySend(status)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        statusRef.addValueEventListener(listener)

        // Clean up when the collector stops observing
        awaitClose { 
            statusRef.removeEventListener(listener) 
        }
    }
}

