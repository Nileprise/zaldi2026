package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLog
import com.example.data.model.Booking
import com.example.data.model.Complaint
import com.example.data.model.Driver
import com.example.data.model.PricingRule
import com.example.data.model.PromoCode
import com.example.data.model.VehicleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Booking::class,
        Driver::class,
        PricingRule::class,
        PromoCode::class,
        Complaint::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZaldiDatabase : RoomDatabase() {
    abstract fun zaldiDao(): ZaldiDao

    companion object {
        @Volatile
        private var INSTANCE: ZaldiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ZaldiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZaldiDatabase::class.java,
                    "zaldi_platform.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.zaldiDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ZaldiDao) {
            // Seed Pricing Rules
            val pricingRules = listOf(
                PricingRule(
                    vehicleType = VehicleType.BIKE.name,
                    baseFare = 40.0,
                    perKmFare = 10.0,
                    minFare = 40.0,
                    waitingChargePerMin = 2.0
                ),
                PricingRule(
                    vehicleType = VehicleType.AUTO.name,
                    baseFare = 60.0,
                    perKmFare = 14.0,
                    minFare = 60.0,
                    waitingChargePerMin = 3.0
                ),
                PricingRule(
                    vehicleType = VehicleType.MINI_TRUCK.name,
                    baseFare = 150.0,
                    perKmFare = 25.0,
                    minFare = 150.0,
                    waitingChargePerMin = 4.0
                ),
                PricingRule(
                    vehicleType = VehicleType.TRUCK.name,
                    baseFare = 300.0,
                    perKmFare = 40.0,
                    minFare = 300.0,
                    waitingChargePerMin = 6.0
                ),
                PricingRule(
                    vehicleType = VehicleType.LARGE_TRUCK.name,
                    baseFare = 600.0,
                    perKmFare = 65.0,
                    minFare = 600.0,
                    waitingChargePerMin = 10.0
                )
            )
            dao.insertPricingRules(pricingRules)

            // Seed Promo Codes
            val promos = listOf(
                PromoCode(
                    code = "ZALDI50",
                    flatDiscount = 50.0,
                    minOrderValue = 100.0,
                    maxDiscount = 50.0,
                    description = "Flat ₹50 OFF on any freight booking"
                ),
                PromoCode(
                    code = "FIRSTMOVE",
                    discountPercent = 20,
                    minOrderValue = 150.0,
                    maxDiscount = 120.0,
                    description = "20% OFF for your first logistics delivery"
                ),
                PromoCode(
                    code = "FREESHIP",
                    flatDiscount = 100.0,
                    minOrderValue = 400.0,
                    maxDiscount = 100.0,
                    description = "Flat ₹100 OFF on Truck orders above ₹400"
                )
            )
            dao.insertPromoCodes(promos)

            // Seed Drivers
            val initialDrivers = listOf(
                Driver(
                    id = "DRV-101",
                    name = "Venkatesh Rao",
                    phone = "+91 98450 11223",
                    rating = 4.9,
                    tripsCount = 384,
                    vehicleType = VehicleType.MINI_TRUCK.name,
                    vehicleModel = "Tata Ace Gold 7ft",
                    vehiclePlate = "KA-01-MJ-8921",
                    kycStatus = Driver.KYC_VERIFIED,
                    isOnline = true,
                    currentLat = 12.9690,
                    currentLng = 77.5920,
                    walletBalance = 2450.0,
                    todayEarnings = 1120.0
                ),
                Driver(
                    id = "DRV-102",
                    name = "Mohammed Imran",
                    phone = "+91 97412 88391",
                    rating = 4.8,
                    tripsCount = 512,
                    vehicleType = VehicleType.AUTO.name,
                    vehicleModel = "Bajaj Maxima Cargo",
                    vehiclePlate = "KA-03-TR-4012",
                    kycStatus = Driver.KYC_VERIFIED,
                    isOnline = true,
                    currentLat = 12.9730,
                    currentLng = 77.5990,
                    walletBalance = 1890.0,
                    todayEarnings = 890.0
                ),
                Driver(
                    id = "DRV-103",
                    name = "Suresh Patil",
                    phone = "+91 99001 77652",
                    rating = 4.9,
                    tripsCount = 278,
                    vehicleType = VehicleType.BIKE.name,
                    vehicleModel = "Hero Super Splendor 125",
                    vehiclePlate = "KA-05-EX-2390",
                    kycStatus = Driver.KYC_VERIFIED,
                    isOnline = true,
                    currentLat = 12.9650,
                    currentLng = 77.6010,
                    walletBalance = 1200.0,
                    todayEarnings = 640.0
                ),
                Driver(
                    id = "DRV-104",
                    name = "Anand Kumar",
                    phone = "+91 98863 44521",
                    rating = 4.7,
                    tripsCount = 189,
                    vehicleType = VehicleType.TRUCK.name,
                    vehicleModel = "Eicher Pro 2049 14ft",
                    vehiclePlate = "KA-04-HV-7731",
                    kycStatus = Driver.KYC_VERIFIED,
                    isOnline = true,
                    currentLat = 12.9800,
                    currentLng = 77.5850,
                    walletBalance = 4500.0,
                    todayEarnings = 2200.0
                ),
                Driver(
                    id = "DRV-105",
                    name = "Gurpreet Singh",
                    phone = "+91 96112 33499",
                    rating = 4.9,
                    tripsCount = 640,
                    vehicleType = VehicleType.LARGE_TRUCK.name,
                    vehicleModel = "Tata Signa 19ft Container",
                    vehiclePlate = "KA-51-CT-9018",
                    kycStatus = Driver.KYC_VERIFIED,
                    isOnline = true,
                    currentLat = 12.9910,
                    currentLng = 77.6100,
                    walletBalance = 7800.0,
                    todayEarnings = 3400.0
                ),
                Driver(
                    id = "DRV-106",
                    name = "Karthik Gowda",
                    phone = "+91 97390 12845",
                    rating = 4.6,
                    tripsCount = 92,
                    vehicleType = VehicleType.MINI_TRUCK.name,
                    vehicleModel = "Mahindra Bolero Maxi Truck",
                    vehiclePlate = "KA-02-ML-6510",
                    kycStatus = Driver.KYC_PENDING,
                    isOnline = false,
                    currentLat = 12.9550,
                    currentLng = 77.5800,
                    walletBalance = 800.0,
                    todayEarnings = 0.0
                )
            )
            dao.insertDrivers(initialDrivers)

            // Seed initial past completed booking so history and admin metrics look great
            val pastBooking = Booking(
                id = 1,
                bookingCode = "ZD-6219",
                customerId = "cust_default",
                customerName = "Rajesh Sharma",
                customerPhone = "+91 98765 43210",
                driverId = "DRV-101",
                driverName = "Venkatesh Rao",
                driverPhone = "+91 98450 11223",
                driverVehicleNo = "KA-01-MJ-8921",
                driverRating = 4.9,
                vehicleType = VehicleType.MINI_TRUCK.name,
                pickupTitle = "Grand Wholesale Market",
                pickupAddress = "Yard 7, APMC Yard, Commercial Zone",
                pickupLat = 12.9420,
                pickupLng = 77.5750,
                dropTitle = "Greenwood Heights Society",
                dropAddress = "Block D, Koramangala 4th Block",
                dropLat = 12.9340,
                dropLng = 77.6180,
                distanceKm = 4.8,
                estimatedDurationMin = 20,
                baseFare = 150.0,
                distanceFare = 120.0,
                helpersFare = 100.0,
                discountFare = 50.0,
                totalFare = 320.0,
                goodsType = "Appliances & Cartons",
                goodsWeightKg = 250,
                helperCount = 1,
                status = Booking.STATUS_DELIVERED,
                pickupOtp = "8412",
                paymentMethod = "UPI",
                paymentStatus = Booking.PAYMENT_PAID,
                rating = 5,
                review = "Prompt arrival, very careful loading and unloading. Highly recommended!",
                createdAt = System.currentTimeMillis() - 86400000L,
                driverCurrentLat = 12.9340,
                driverCurrentLng = 77.6180,
                driverProgressPercent = 1.0f
            )
            dao.insertBooking(pastBooking)

            // Seed initial Audit Log
            dao.insertAuditLog(
                AuditLog(
                    action = "PLATFORM_INIT",
                    details = "Zaldi Logistics Platform engine initialized with 6 active fleet units",
                    category = "SECURITY"
                )
            )
        }
    }
}
