package com.example.data.local

import com.example.domain.model.DriverLocationPing
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RealtimeDatabase {
    
    private val database: DatabaseReference = Firebase.database.reference
    
    // Live driver locations stream
    private val _driverLocations = MutableStateFlow<Map<String, DriverLocationPing>>(emptyMap())
    val driverLocations: StateFlow<Map<String, DriverLocationPing>> = _driverLocations.asStateFlow()
    
    // Live order status stream
    private val _orderUpdates = MutableStateFlow<Map<String, String>>(emptyMap())
    val orderUpdates: StateFlow<Map<String, String>> = _orderUpdates.asStateFlow()
    
    fun publishDriverLocation(driverId: String, ping: DriverLocationPing) {
        database.child("drivers").child(driverId).child("location").apply {
            setValue(mapOf(
                "lat" to ping.lat,
                "lng" to ping.lng,
                "accuracy" to ping.accuracy,
                "timestamp" to ping.timestamp
            ))
        }
    }
    
    fun subscribeToDriverLocation(driverId: String, onLocationReceived: (DriverLocationPing) -> Unit) {
        database.child("drivers").child(driverId).child("location")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val lat = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
                    val lng = snapshot.child("lng").getValue(Double::class.java) ?: 0.0
                    val accuracy = snapshot.child("accuracy").getValue(Float::class.java) ?: 10f
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                    
                    onLocationReceived(DriverLocationPing(
                        driverId = driverId,
                        lat = lat,
                        lng = lng,
                        accuracy = accuracy,
                        timestamp = timestamp
                    ))
                }
                
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    // Handle error
                }
            })
    }
    
    fun publishOrderStatus(orderId: String, status: String) {
        database.child("orders").child(orderId).child("status").setValue(status)
    }
    
    fun subscribeToOrderUpdates(orderId: String, onStatusChange: (String) -> Unit) {
        database.child("orders").child(orderId).child("status")
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val status = snapshot.getValue(String::class.java) ?: ""
                    onStatusChange(status)
                }
                
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    // Handle error
                }
            })
    }
}
