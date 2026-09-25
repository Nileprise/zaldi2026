package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DriverKyc
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Query("SELECT * FROM driver_kyc ORDER BY submissionDate DESC")
    fun getAllDrivers(): Flow<List<DriverKyc>>

    @Query("SELECT * FROM driver_kyc WHERE driverId = :driverId LIMIT 1")
    fun getDriverById(driverId: String): Flow<DriverKyc?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverKyc)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverKyc>)

    @Update
    suspend fun updateDriver(driver: DriverKyc)

    @Query("UPDATE driver_kyc SET status = :status WHERE driverId = :driverId")
    suspend fun updateKycStatus(driverId: String, status: String)

    @Query("SELECT * FROM driver_kyc WHERE status = 'APPROVED' ORDER BY name ASC")
    fun getApprovedDrivers(): Flow<List<DriverKyc>>

    @Query("SELECT * FROM driver_kyc WHERE availabilityStatus = :availability")
    fun getDriversByAvailability(availability: String): Flow<List<DriverKyc>>

    @Query("UPDATE driver_kyc SET availabilityStatus = :availability WHERE driverId = :driverId")
    suspend fun updateAvailabilityStatus(driverId: String, availability: String)

    @Query("UPDATE driver_kyc SET availabilityStatus = :availability, assignedOrderId = :orderId WHERE driverId = :driverId")
    suspend fun updateDriverAvailability(driverId: String, availability: String, orderId: String?)

    @Query("UPDATE driver_kyc SET assignedOrderId = :orderId, availabilityStatus = 'BUSY' WHERE driverId = :driverId")
    suspend fun assignDriverToOrder(driverId: String, orderId: String)

    @Query("UPDATE driver_kyc SET assignedOrderId = NULL, availabilityStatus = 'AVAILABLE' WHERE driverId = :driverId")
    suspend fun unassignDriver(driverId: String)
}
