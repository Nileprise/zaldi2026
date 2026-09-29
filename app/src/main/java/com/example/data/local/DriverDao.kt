Here is the modernized version of your DriverDao.
I have applied several important updates based on modern Android architecture guidelines and the latest Room features:
 * @Upsert instead of @Insert(REPLACE): In modern Room (2.5.0+), you should use @Upsert instead of @Insert(onConflict = OnConflictStrategy.REPLACE). The legacy REPLACE strategy actually deletes the row and re-inserts it, which resets the auto-generated ID, breaks foreign keys, and causes unnecessary Flow emissions. @Upsert performs a true SQL update if the row exists.
 * Stream vs. One-Shot Naming Convention: Following Google's recommended architecture, I suffixed Flow returns with Stream to clearly denote that they emit continuous updates. I also added a one-shot suspend read (getDriverByIdSync), which is highly useful when a background worker just needs to read the data once without collecting a Flow.
 * Multi-line Raw Strings: Converted the SQL queries to Kotlin's multi-line raw strings ("""..."""). This makes complex SQL updates much easier to read and maintain.
Modernized DriverDao.kt
package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.data.model.DriverKyc
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    // ------------------------------------------------------------------------
    // Reactive Reads (Flow Streams)
    // ------------------------------------------------------------------------

    @Query("SELECT * FROM driver_kyc ORDER BY submissionDate DESC")
    fun getAllDriversStream(): Flow<List<DriverKyc>>

    @Query("SELECT * FROM driver_kyc WHERE driverId = :driverId LIMIT 1")
    fun getDriverByIdStream(driverId: String): Flow<DriverKyc?>

    @Query("SELECT * FROM driver_kyc WHERE status = 'APPROVED' ORDER BY name ASC")
    fun getApprovedDriversStream(): Flow<List<DriverKyc>>

    @Query("SELECT * FROM driver_kyc WHERE availabilityStatus = :availability")
    fun getDriversByAvailabilityStream(availability: String): Flow<List<DriverKyc>>

    // ------------------------------------------------------------------------
    // One-Shot Reads (Suspend)
    // ------------------------------------------------------------------------
    
    // Modern pattern: Provide a non-Flow version for repository operations 
    // that only need to fetch the current state once (e.g., API syncing).
    @Query("SELECT * FROM driver_kyc WHERE driverId = :driverId LIMIT 1")
    suspend fun getDriverByIdSync(driverId: String): DriverKyc?

    // ------------------------------------------------------------------------
    // Writes (Upsert / Update)
    // ------------------------------------------------------------------------

    // Uses Room 2.5+ @Upsert to prevent row deletion/recreation issues 
    // caused by the older @Insert(OnConflictStrategy.REPLACE)
    @Upsert
    suspend fun upsertDriver(driver: DriverKyc)

    @Upsert
    suspend fun upsertDrivers(drivers: List<DriverKyc>)

    @Update
    suspend fun updateDriver(driver: DriverKyc)

    // ------------------------------------------------------------------------
    // Partial Updates (Formatted with raw strings for readability)
    // ------------------------------------------------------------------------

    @Query("""
        UPDATE driver_kyc 
        SET status = :status 
        WHERE driverId = :driverId
    """)
    suspend fun updateKycStatus(driverId: String, status: String)

    @Query("""
        UPDATE driver_kyc 
        SET availabilityStatus = :availability 
        WHERE driverId = :driverId
    """)
    suspend fun updateAvailabilityStatus(driverId: String, availability: String)

    @Query("""
        UPDATE driver_kyc 
        SET availabilityStatus = :availability, 
            assignedOrderId = :orderId 
        WHERE driverId = :driverId
    """)
    suspend fun updateDriverAvailability(driverId: String, availability: String, orderId: String?)

    @Query("""
        UPDATE driver_kyc 
        SET assignedOrderId = :orderId, 
            availabilityStatus = 'BUSY' 
        WHERE driverId = :driverId
    """)
    suspend fun assignDriverToOrder(driverId: String, orderId: String)

    @Query("""
        UPDATE driver_kyc 
        SET assignedOrderId = NULL, 
            availabilityStatus = 'AVAILABLE' 
        WHERE driverId = :driverId
    """)
    suspend fun unassignDriver(driverId: String)
}

