package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.DriverDao
import com.example.data.local.OrderDao
import com.example.data.model.BookingOrder
import com.example.data.model.DriverAvailability
import com.example.data.model.DriverKyc
import com.example.data.model.KycStatus
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Modernized Repository pattern.
 * Uses @Inject for Dependency Injection (e.g., via Hilt or Dagger).
 */
class LogisticsRepository @Inject constructor(
    private val database: AppDatabase, // Required for complex multi-table transactions
    private val orderDao: OrderDao,
    private val driverDao: DriverDao
) {
    // ------------------------------------------------------------------------
    // Reactive Streams (Flows)
    // Suffixing with 'Stream' is a modern convention to denote continuous emissions
    // ------------------------------------------------------------------------
    
    val allOrdersStream: Flow<List<BookingOrder>> = orderDao.getAllOrdersStream()
    val activeOrderStream: Flow<BookingOrder?> = orderDao.getActiveOrderStream()
    val pendingOrdersStream: Flow<List<BookingOrder>> = orderDao.getPendingOrdersStream()
    
    val allDriversStream: Flow<List<DriverKyc>> = driverDao.getAllDriversStream()
    val approvedDriversStream: Flow<List<DriverKyc>> = driverDao.getApprovedDriversStream()

    fun getDriversByAvailabilityStream(availability: DriverAvailability): Flow<List<DriverKyc>> {
        // Enums are natively supported by Room or easily mapped using name
        return driverDao.getDriversByAvailabilityStream(availability.name)
    }

    fun getOrderByIdStream(orderId: String): Flow<BookingOrder?> {
        return orderDao.getOrderByIdStream(orderId)
    }

    // ------------------------------------------------------------------------
    // Single Source of Truth Writes
    // ------------------------------------------------------------------------

    suspend fun createOrder(order: BookingOrder) {
        orderDao.upsertOrder(order) // Updated to Upsert based on previous modernizations
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
    }

    suspend fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    suspend fun completeOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.DELIVERED)
    }

    suspend fun updateKycStatus(driverId: String, status: KycStatus) {
        driverDao.updateKycStatus(driverId, status.name)
    }

    suspend fun insertDriver(driver: DriverKyc) {
        driverDao.upsertDriver(driver)
    }

    suspend fun updateDriverAvailability(
        driverId: String, 
        availability: DriverAvailability, 
        orderId: String? = null
    ) {
        driverDao.updateDriverAvailability(driverId, availability.name, orderId)
    }

    // ------------------------------------------------------------------------
    // Complex Multi-Table Transactions
    // ------------------------------------------------------------------------

    /**
     * Safely assigns a driver to an order.
     * Uses withTransaction to guarantee ATOMICITY. Both tables MUST update successfully.
     * If the app crashes halfway through, all changes roll back safely.
     */
    suspend fun assignDriverToOrder(order: BookingOrder, driver: DriverKyc) {
        database.withTransaction {
            // 1. Assign driver in Room database orders table
            orderDao.assignDriverToOrder(
                orderId = order.id,
                driverId = driver.driverId,
                driverName = driver.name,
                driverPhone = driver.phone,
                driverVehicleNumber = driver.vehicleNumber
            )
            
            // 2. Mark driver as BUSY and map assigned order in driver_kyc table
            driverDao.assignDriverToOrder(
                driverId = driver.driverId, 
                orderId = order.id
            )
        }
    }

    /**
     * Safely unassigns a driver from an order.
     */
    suspend fun unassignDriver(orderId: String, driverId: String) {
        database.withTransaction {
            orderDao.unassignDriverFromOrder(orderId)
            driverDao.unassignDriver(driverId)
        }
    }
}
