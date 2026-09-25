package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<BookingOrder>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<BookingOrder?>

    @Query("SELECT * FROM orders WHERE status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY timestamp DESC LIMIT 1")
    fun getActiveOrder(): Flow<BookingOrder?>

    @Query("SELECT * FROM orders WHERE status IN ('PENDING', 'SEARCHING') ORDER BY timestamp DESC")
    fun getPendingOrders(): Flow<List<BookingOrder>>

    @Query("SELECT * FROM orders WHERE assignedDriverId = :driverId AND status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY timestamp DESC LIMIT 1")
    fun getActiveOrderByDriver(driverId: String): Flow<BookingOrder?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: BookingOrder)

    @Update
    suspend fun updateOrder(order: BookingOrder)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET status = 'DRIVER_ASSIGNED', assignedDriverId = :driverId, driverName = :driverName, driverPhone = :driverPhone, driverVehicleNumber = :driverVehicleNumber WHERE id = :orderId")
    suspend fun assignDriverToOrder(
        orderId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        driverVehicleNumber: String
    )

    @Query("UPDATE orders SET status = 'PENDING', assignedDriverId = NULL WHERE id = :orderId")
    suspend fun unassignDriverFromOrder(orderId: String)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: String)

    @Query("DELETE FROM orders WHERE id LIKE 'AKH-77291' OR id LIKE 'AKH-88312' OR id LIKE 'AKH-91045' OR id LIKE 'AKH-94218' OR id LIKE 'AKH-61842' OR id LIKE 'AKH-55209' OR id LIKE 'AKH-48310'")
    suspend fun clearDemoOrders()
}
