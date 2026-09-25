package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BookingOrder
import com.example.data.model.DriverKyc
import kotlinx.coroutines.flow.Flow

class LogisticsRepository(private val database: AppDatabase) {
    private val orderDao = database.orderDao()
    private val driverDao = database.driverDao()

    val allOrders: Flow<List<BookingOrder>> = orderDao.getAllOrders()
    val activeOrder: Flow<BookingOrder?> = orderDao.getActiveOrder()
    val allDrivers: Flow<List<DriverKyc>> = driverDao.getAllDrivers()
    val pendingOrders: Flow<List<BookingOrder>> = orderDao.getPendingOrders()
    val approvedDrivers: Flow<List<DriverKyc>> = driverDao.getApprovedDrivers()

    fun getDriversByAvailability(availability: String): Flow<List<DriverKyc>> =
        driverDao.getDriversByAvailability(availability)

    fun getOrderById(orderId: String): Flow<BookingOrder?> = orderDao.getOrderById(orderId)

    suspend fun createOrder(order: BookingOrder) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun cancelOrder(orderId: String) {
        orderDao.updateOrderStatus(orderId, "CANCELLED")
    }

    suspend fun completeOrder(orderId: String) {
        orderDao.updateOrderStatus(orderId, "DELIVERED")
    }

    suspend fun updateKycStatus(driverId: String, status: String) {
        driverDao.updateKycStatus(driverId, status)
    }

    suspend fun insertDriver(driver: DriverKyc) {
        driverDao.insertDriver(driver)
    }

    suspend fun updateDriverAvailability(driverId: String, availability: String, orderId: String? = null) {
        driverDao.updateDriverAvailability(driverId, availability, orderId)
    }

    suspend fun assignDriverToOrder(
        order: BookingOrder,
        driver: DriverKyc
    ) {
        // 1. Assign driver in Room database orders table
        orderDao.assignDriverToOrder(
            orderId = order.id,
            driverId = driver.driverId,
            driverName = driver.name,
            driverPhone = driver.phone,
            driverVehicleNumber = driver.vehicleNumber
        )
        // 2. Mark driver as BUSY and map assigned order in driver_kyc table
        driverDao.assignDriverToOrder(driver.driverId, order.id)
    }

    suspend fun unassignDriver(orderId: String, driverId: String) {
        orderDao.unassignDriverFromOrder(orderId)
        driverDao.unassignDriver(driverId)
    }
}
