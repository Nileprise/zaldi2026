package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLog
import com.example.data.model.Booking
import com.example.data.model.Complaint
import com.example.data.model.Driver
import com.example.data.model.PricingRule
import com.example.data.model.PromoCode
import kotlinx.coroutines.flow.Flow

@Dao
interface ZaldiDao {

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: Long): Flow<Booking?>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingByIdOnce(id: Long): Booking?

    @Query("SELECT * FROM bookings WHERE status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveCustomerBooking(): Flow<Booking?>

    @Query("SELECT * FROM bookings WHERE driverId = :driverId AND status NOT IN ('DELIVERED', 'CANCELLED') LIMIT 1")
    fun getActiveDriverBooking(driverId: String): Flow<Booking?>

    @Query("SELECT * FROM bookings WHERE status = 'SEARCHING' ORDER BY createdAt ASC LIMIT 1")
    fun getPendingDispatchBooking(): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    // Drivers
    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE isOnline = 1")
    fun getOnlineDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE id = :id")
    fun getDriverById(id: String): Flow<Driver?>

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getDriverByIdOnce(id: String): Driver?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<Driver>)

    @Update
    suspend fun updateDriver(driver: Driver)

    // Pricing
    @Query("SELECT * FROM pricing_rules")
    fun getAllPricingRules(): Flow<List<PricingRule>>

    @Query("SELECT * FROM pricing_rules WHERE vehicleType = :type LIMIT 1")
    suspend fun getPricingRule(type: String): PricingRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricingRules(rules: List<PricingRule>)

    @Update
    suspend fun updatePricingRule(rule: PricingRule)

    // Promo codes
    @Query("SELECT * FROM promo_codes")
    fun getAllPromoCodes(): Flow<List<PromoCode>>

    @Query("SELECT * FROM promo_codes WHERE code = :code LIMIT 1")
    suspend fun getPromoCode(code: String): PromoCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCodes(promos: List<PromoCode>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCode(promo: PromoCode)

    @Update
    suspend fun updatePromoCode(promo: PromoCode)

    // Complaints
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<Complaint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint): Long

    @Update
    suspend fun updateComplaint(complaint: Complaint)

    // Audit logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}
