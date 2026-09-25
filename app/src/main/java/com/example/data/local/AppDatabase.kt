package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookingOrder
import com.example.data.model.DriverKyc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BookingOrder::class, DriverKyc::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun driverDao(): DriverDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akhil_logistics_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).driverDao()
                            val orderDao = getDatabase(context).orderDao()
                            seedInitialData(dao, orderDao)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(driverDao: DriverDao, orderDao: OrderDao) {
            val seedDrivers = listOf(
                DriverKyc(
                    driverId = "DRV-101",
                    name = "Ravi Kumar",
                    phone = "+91 98452 11094",
                    vehicleTier = "Tata Ace",
                    vehicleNumber = "KA 05 MX 2190",
                    licenseNumber = "KA0520210048392",
                    aadhaarNumber = "XXXX-XXXX-4819",
                    status = "APPROVED",
                    submissionDate = "2026-09-15",
                    rating = 4.88,
                    completedTrips = 156,
                    earningsToday = 1840.0,
                    pendingPayout = 6420.0,
                    availabilityStatus = "AVAILABLE",
                    assignedOrderId = null,
                    currentArea = "Indiranagar 100ft Rd"
                ),
                DriverKyc(
                    driverId = "DRV-102",
                    name = "Suresh Babu",
                    phone = "+91 97412 88301",
                    vehicleTier = "3-Wheeler Auto",
                    vehicleNumber = "KA 01 EK 7712",
                    licenseNumber = "KA0120220084729",
                    aadhaarNumber = "XXXX-XXXX-6623",
                    status = "APPROVED",
                    submissionDate = "2026-09-23",
                    rating = 4.75,
                    completedTrips = 89,
                    earningsToday = 1120.0,
                    pendingPayout = 3450.0,
                    availabilityStatus = "AVAILABLE",
                    assignedOrderId = null,
                    currentArea = "Koramangala 5th Block"
                ),
                DriverKyc(
                    driverId = "DRV-103",
                    name = "Mohammed Aslam",
                    phone = "+91 99014 33215",
                    vehicleTier = "Pickup Truck (8ft)",
                    vehicleNumber = "KA 51 D 9034",
                    licenseNumber = "KA5120190019283",
                    aadhaarNumber = "XXXX-XXXX-9021",
                    status = "APPROVED",
                    submissionDate = "2026-09-10",
                    rating = 4.92,
                    completedTrips = 312,
                    earningsToday = 2650.0,
                    pendingPayout = 9800.0,
                    availabilityStatus = "AVAILABLE",
                    assignedOrderId = null,
                    currentArea = "Whitefield Main Rd"
                ),
                DriverKyc(
                    driverId = "DRV-104",
                    name = "Vignesh Murugan",
                    phone = "+91 96201 54932",
                    vehicleTier = "2-Wheeler (Bike)",
                    vehicleNumber = "KA 03 HM 4810",
                    licenseNumber = "KA0320230067182",
                    aadhaarNumber = "XXXX-XXXX-3341",
                    status = "APPROVED",
                    submissionDate = "2026-09-24",
                    rating = 4.65,
                    completedTrips = 44,
                    earningsToday = 680.0,
                    pendingPayout = 1920.0,
                    availabilityStatus = "OFFLINE",
                    assignedOrderId = null,
                    currentArea = "HSR Layout Sector 4"
                ),
                DriverKyc(
                    driverId = "DRV-105",
                    name = "Deepak Gowda",
                    phone = "+91 99805 77123",
                    vehicleTier = "Tata 407 (14ft)",
                    vehicleNumber = "KA 04 EL 5590",
                    licenseNumber = "KA0420180034912",
                    aadhaarNumber = "XXXX-XXXX-8812",
                    status = "APPROVED",
                    submissionDate = "2026-09-18",
                    rating = 4.82,
                    completedTrips = 204,
                    earningsToday = 2100.0,
                    pendingPayout = 7500.0,
                    availabilityStatus = "AVAILABLE",
                    assignedOrderId = null,
                    currentArea = "Peenya Industrial Area"
                )
            )
            driverDao.insertDrivers(seedDrivers)

            // Seed an active initial trip for smooth evaluation
            val initialOrder = BookingOrder(
                id = "AKH-77291",
                customerPhone = "+91 99999 99999",
                customerName = "Akhil Sharma",
                pickupAddress = "Indiranagar 100ft Rd, Bengaluru",
                dropoffAddress = "Koramangala 4th Block, Bengaluru",
                vehicleTierId = "tata",
                vehicleName = "Tata Ace (Mini Truck)",
                goodsType = "Electronics & Gadgets",
                helperRequired = true,
                helperFee = 80.0,
                fare = 379.0,
                distanceKm = 6.4,
                paymentMethod = "Cash on Delivery (COD)",
                status = "DRIVER_ASSIGNED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = "4821",
                timestamp = System.currentTimeMillis() - 3600000,
                etaMinutes = 11,
                assignedDriverId = "DRV-101"
            )
            orderDao.insertOrder(initialOrder)

            // Seed pending delivery requests waiting for driver mapping
            val pendingOrder1 = BookingOrder(
                id = "AKH-88312",
                customerPhone = "+91 98453 66102",
                customerName = "Ananya Sharma",
                pickupAddress = "Chickpet Wholesale Market, Bengaluru",
                dropoffAddress = "Commercial Street, Shivajinagar, Bengaluru",
                vehicleTierId = "auto",
                vehicleName = "3-Wheeler Auto",
                goodsType = "Commercial Silk & Textiles (20 Bundles)",
                helperRequired = false,
                helperFee = 0.0,
                fare = 240.0,
                distanceKm = 4.2,
                paymentMethod = "Online UPI",
                status = "PENDING",
                driverName = "",
                driverPhone = "",
                driverRating = 0.0,
                driverVehicleNumber = "",
                startOtp = "8341",
                timestamp = System.currentTimeMillis() - 900000,
                etaMinutes = 14,
                assignedDriverId = null
            )
            val pendingOrder2 = BookingOrder(
                id = "AKH-91045",
                customerPhone = "+91 97412 11983",
                customerName = "Vikram Malhotra",
                pickupAddress = "Peenya Industrial Area Stage 1, Bengaluru",
                dropoffAddress = "Yeshwanthpur APMC Yard, Bengaluru",
                vehicleTierId = "pickup_8ft",
                vehicleName = "Pickup Truck (8ft)",
                goodsType = "Precision Machined Steel Castings",
                helperRequired = true,
                helperFee = 80.0,
                fare = 580.0,
                distanceKm = 8.5,
                paymentMethod = "Corporate Account",
                status = "PENDING",
                driverName = "",
                driverPhone = "",
                driverRating = 0.0,
                driverVehicleNumber = "",
                startOtp = "4019",
                timestamp = System.currentTimeMillis() - 450000,
                etaMinutes = 18,
                assignedDriverId = null
            )
            val pendingOrder3 = BookingOrder(
                id = "AKH-94218",
                customerPhone = "+91 96204 88391",
                customerName = "Deepa Narayanan",
                pickupAddress = "BTM Layout 2nd Stage, Bengaluru",
                dropoffAddress = "Electronic City Phase 2, Bengaluru",
                vehicleTierId = "tata",
                vehicleName = "Tata Ace (Mini Truck)",
                goodsType = "Server Racks & IT Networking Equipment",
                helperRequired = true,
                helperFee = 80.0,
                fare = 460.0,
                distanceKm = 10.2,
                paymentMethod = "Online UPI",
                status = "PENDING",
                driverName = "",
                driverPhone = "",
                driverRating = 0.0,
                driverVehicleNumber = "",
                startOtp = "6274",
                timestamp = System.currentTimeMillis() - 180000,
                etaMinutes = 22,
                assignedDriverId = null
            )
            orderDao.insertOrder(pendingOrder1)
            orderDao.insertOrder(pendingOrder2)
            orderDao.insertOrder(pendingOrder3)

            // Seed completed past deliveries for driver history
            val completedOrder1 = BookingOrder(
                id = "AKH-61842",
                customerPhone = "+91 98451 22891",
                customerName = "Priya Sundaram",
                pickupAddress = "Whitefield Main Rd, Bengaluru",
                dropoffAddress = "MG Road Brigade Towers, Bengaluru",
                vehicleTierId = "tata",
                vehicleName = "Tata Ace (Mini Truck)",
                goodsType = "Home Decor & Glassware",
                helperRequired = true,
                helperFee = 80.0,
                fare = 520.0,
                distanceKm = 14.2,
                paymentMethod = "Online UPI",
                status = "DELIVERED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = "5921",
                timestamp = System.currentTimeMillis() - (4 * 3600000), // 4 hrs ago today
                etaMinutes = 0
            )
            val completedOrder2 = BookingOrder(
                id = "AKH-55209",
                customerPhone = "+91 97410 44920",
                customerName = "Rajesh Verma",
                pickupAddress = "Peenya Industrial Area Stage 2, Bengaluru",
                dropoffAddress = "Rajajinagar 1st Block, Bengaluru",
                vehicleTierId = "tata",
                vehicleName = "Tata Ace (Mini Truck)",
                goodsType = "Machine Tooling & Steel Parts",
                helperRequired = false,
                helperFee = 0.0,
                fare = 680.0,
                distanceKm = 9.8,
                paymentMethod = "Corporate Account",
                status = "DELIVERED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = "3810",
                timestamp = System.currentTimeMillis() - 86400000, // Yesterday
                etaMinutes = 0
            )
            val completedOrder3 = BookingOrder(
                id = "AKH-48310",
                customerPhone = "+91 96208 77150",
                customerName = "Kavita Nair",
                pickupAddress = "HSR Layout Sector 3, Bengaluru",
                dropoffAddress = "Electronic City Phase 1, Bengaluru",
                vehicleTierId = "tata",
                vehicleName = "Tata Ace (Mini Truck)",
                goodsType = "Office Ergonomic Chairs (6 units)",
                helperRequired = true,
                helperFee = 80.0,
                fare = 440.0,
                distanceKm = 11.5,
                paymentMethod = "Cash on Delivery",
                status = "DELIVERED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = "7249",
                timestamp = System.currentTimeMillis() - (2 * 86400000), // 2 days ago
                etaMinutes = 0
            )
            orderDao.insertOrder(completedOrder1)
            orderDao.insertOrder(completedOrder2)
            orderDao.insertOrder(completedOrder3)
        }
    }
}
