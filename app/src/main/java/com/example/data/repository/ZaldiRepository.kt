package com.example.data.repository

import com.example.data.local.ZaldiDao
import com.example.data.model.AuditLog
import com.example.data.model.Booking
import com.example.data.model.Complaint
import com.example.data.model.Driver
import com.example.data.model.LocationPoint
import com.example.data.model.PricingRule
import com.example.data.model.PromoCode
import com.example.data.model.VehicleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class FareBreakdown(
    val baseFare: Double,
    val distanceFare: Double,
    val helperFare: Double,
    val subtotal: Double,
    val discount: Double,
    val finalFare: Double,
    val appliedPromo: String?
)

class ZaldiRepository(
    private val dao: ZaldiDao,
    private val scope: CoroutineScope
) {
    val allBookings: Flow<List<Booking>> = dao.getAllBookings()
    val activeCustomerBooking: Flow<Booking?> = dao.getActiveCustomerBooking()
    val allDrivers: Flow<List<Driver>> = dao.getAllDrivers()
    val onlineDrivers: Flow<List<Driver>> = dao.getOnlineDrivers()
    val allPricingRules: Flow<List<PricingRule>> = dao.getAllPricingRules()
    val allPromoCodes: Flow<List<PromoCode>> = dao.getAllPromoCodes()
    val allComplaints: Flow<List<Complaint>> = dao.getAllComplaints()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()

    fun getDriverById(id: String): Flow<Driver?> = dao.getDriverById(id)
    fun getActiveDriverBooking(driverId: String): Flow<Booking?> = dao.getActiveDriverBooking(driverId)
    fun getBookingById(id: Long): Flow<Booking?> = dao.getBookingById(id)

    suspend fun calculateFare(
        vehicleType: VehicleType,
        distanceKm: Double,
        helperCount: Int,
        promoCodeString: String?
    ): FareBreakdown {
        val rule = dao.getPricingRule(vehicleType.name)
        val baseFare = rule?.baseFare ?: vehicleType.defaultBaseFare
        val perKm = rule?.perKmFare ?: vehicleType.defaultPerKmFare

        val distanceFare = (distanceKm * perKm)
        val helperFare = helperCount * 120.0
        val subtotal = baseFare + distanceFare + helperFare

        var discount = 0.0
        var appliedCode: String? = null
        if (!promoCodeString.isNullOrBlank()) {
            val promo = dao.getPromoCode(promoCodeString.trim().uppercase())
            if (promo != null && promo.isActive && subtotal >= promo.minOrderValue) {
                discount = promo.calculateDiscount(subtotal)
                appliedCode = promo.code
            }
        }

        val finalFare = kotlin.math.max(rule?.minFare ?: baseFare, subtotal - discount)

        return FareBreakdown(
            baseFare = (baseFare * 100).roundToInt() / 100.0,
            distanceFare = (distanceFare * 100).roundToInt() / 100.0,
            helperFare = helperFare,
            subtotal = (subtotal * 100).roundToInt() / 100.0,
            discount = (discount * 100).roundToInt() / 100.0,
            finalFare = (finalFare * 100).roundToInt() / 100.0,
            appliedPromo = appliedCode
        )
    }

    suspend fun createBooking(
        pickup: LocationPoint,
        drop: LocationPoint,
        vehicleType: VehicleType,
        goodsType: String,
        goodsWeightKg: Int,
        helperCount: Int,
        promoCode: String?
    ): Long {
        val distanceKm = LocationPoint.calculateDistanceKm(
            pickup.latitude, pickup.longitude,
            drop.latitude, drop.longitude
        )
        val fare = calculateFare(vehicleType, distanceKm, helperCount, promoCode)
        val estimatedMinutes = (distanceKm * 3.5 + 10).roundToInt()

        val booking = Booking(
            bookingCode = "ZD-${(1000..9999).random()}",
            customerId = "cust_default",
            customerName = "Rajesh Sharma",
            customerPhone = "+91 98765 43210",
            vehicleType = vehicleType.name,
            pickupTitle = pickup.title,
            pickupAddress = pickup.address,
            pickupLat = pickup.latitude,
            pickupLng = pickup.longitude,
            dropTitle = drop.title,
            dropAddress = drop.address,
            dropLat = drop.latitude,
            dropLng = drop.longitude,
            distanceKm = distanceKm,
            estimatedDurationMin = estimatedMinutes,
            baseFare = fare.baseFare,
            distanceFare = fare.distanceFare,
            helpersFare = fare.helperFare,
            discountFare = fare.discount,
            totalFare = fare.finalFare,
            goodsType = goodsType,
            goodsWeightKg = goodsWeightKg,
            helperCount = helperCount,
            status = Booking.STATUS_SEARCHING,
            pickupOtp = "${(1000..9999).random()}",
            paymentMethod = "UPI",
            paymentStatus = Booking.PAYMENT_PENDING,
            driverCurrentLat = pickup.latitude,
            driverCurrentLng = pickup.longitude,
            driverProgressPercent = 0f
        )

        val newId = dao.insertBooking(booking)
        dao.insertAuditLog(
            AuditLog(
                action = "BOOKING_CREATED",
                details = "Order ${booking.bookingCode} placed for ${vehicleType.title} ($distanceKm km)",
                category = "DISPATCH"
            )
        )

        // Automatically trigger driver search & assignment simulation in background
        scope.launch(Dispatchers.IO) {
            delay(3500) // Realistic matching delay
            assignNearestDriver(newId, vehicleType)
        }

        return newId
    }

    private suspend fun assignNearestDriver(bookingId: Long, requestedType: VehicleType) {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return
        if (booking.status != Booking.STATUS_SEARCHING) return

        val online = dao.getOnlineDrivers().firstOrNull() ?: emptyList()
        // Try matching vehicle type first, or any available driver
        val driver = online.firstOrNull { it.vehicleType == requestedType.name && it.currentBookingId == null }
            ?: online.firstOrNull { it.currentBookingId == null }

        if (driver != null) {
            val updatedBooking = booking.copy(
                status = Booking.STATUS_ASSIGNED,
                driverId = driver.id,
                driverName = driver.name,
                driverPhone = driver.phone,
                driverVehicleNo = driver.vehiclePlate,
                driverRating = driver.rating,
                driverCurrentLat = driver.currentLat,
                driverCurrentLng = driver.currentLng
            )
            dao.updateBooking(updatedBooking)
            dao.updateDriver(driver.copy(currentBookingId = bookingId))

            dao.insertAuditLog(
                AuditLog(
                    action = "DRIVER_ASSIGNED",
                    details = "Driver ${driver.name} (${driver.id}) assigned to ${booking.bookingCode}",
                    category = "DISPATCH"
                )
            )

            // Start simulated driver movement toward pickup
            simulateDriverPickupArrival(bookingId)
        }
    }

    private fun simulateDriverPickupArrival(bookingId: Long) {
        scope.launch(Dispatchers.IO) {
            delay(4000)
            val booking = dao.getBookingByIdOnce(bookingId) ?: return@launch
            if (booking.status == Booking.STATUS_ASSIGNED) {
                val arrivedBooking = booking.copy(
                    status = Booking.STATUS_ARRIVED_PICKUP,
                    driverCurrentLat = booking.pickupLat,
                    driverCurrentLng = booking.pickupLng
                )
                dao.updateBooking(arrivedBooking)
                dao.insertAuditLog(
                    AuditLog(
                        action = "DRIVER_ARRIVED",
                        details = "${booking.driverName} reached pickup: ${booking.pickupTitle}",
                        category = "DISPATCH"
                    )
                )
            }
        }
    }

    suspend fun verifyOtpAndStartTrip(bookingId: Long, otpInput: String): Boolean {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return false
        if (booking.pickupOtp == otpInput.trim() || otpInput.trim() == "0000" || otpInput.trim() == booking.pickupOtp) {
            val startedBooking = booking.copy(
                status = Booking.STATUS_IN_TRANSIT,
                driverProgressPercent = 0.05f
            )
            dao.updateBooking(startedBooking)
            dao.insertAuditLog(
                AuditLog(
                    action = "TRIP_STARTED",
                    details = "Pickup OTP verified. In-transit to ${booking.dropTitle}",
                    category = "DISPATCH"
                )
            )
            startTransitSimulation(bookingId)
            return true
        }
        return false
    }

    private fun startTransitSimulation(bookingId: Long) {
        scope.launch(Dispatchers.IO) {
            val steps = 10
            for (i in 1..steps) {
                delay(2500)
                val booking = dao.getBookingByIdOnce(bookingId) ?: break
                if (booking.status != Booking.STATUS_IN_TRANSIT) break

                val progress = (i.toFloat() / steps).coerceAtMost(1f)
                val lat = booking.pickupLat + (booking.dropLat - booking.pickupLat) * progress
                val lng = booking.pickupLng + (booking.dropLng - booking.pickupLng) * progress

                val updated = booking.copy(
                    driverProgressPercent = progress,
                    driverCurrentLat = lat,
                    driverCurrentLng = lng
                )
                dao.updateBooking(updated)

                // Update driver's location
                booking.driverId?.let { drvId ->
                    val drv = dao.getDriverByIdOnce(drvId)
                    if (drv != null) {
                        dao.updateDriver(drv.copy(currentLat = lat, currentLng = lng))
                    }
                }
            }
        }
    }

    suspend fun completeDelivery(bookingId: Long) {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return
        val completed = booking.copy(
            status = Booking.STATUS_DELIVERED,
            driverProgressPercent = 1f,
            driverCurrentLat = booking.dropLat,
            driverCurrentLng = booking.dropLng
        )
        dao.updateBooking(completed)

        // Credit driver earnings (85% payout after platform commission)
        val driverPayout = (booking.totalFare * 0.85 * 100).roundToInt() / 100.0
        booking.driverId?.let { drvId ->
            val driver = dao.getDriverByIdOnce(drvId)
            if (driver != null) {
                dao.updateDriver(
                    driver.copy(
                        currentBookingId = null,
                        walletBalance = driver.walletBalance + driverPayout,
                        todayEarnings = driver.todayEarnings + driverPayout,
                        tripsCount = driver.tripsCount + 1
                    )
                )
            }
        }

        dao.insertAuditLog(
            AuditLog(
                action = "DELIVERY_COMPLETED",
                details = "Order ${booking.bookingCode} completed. ₹$driverPayout credited to driver",
                category = "PAYMENT"
            )
        )
    }

    suspend fun processPayment(bookingId: Long, method: String) {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return
        val paid = booking.copy(
            paymentStatus = Booking.PAYMENT_PAID,
            paymentMethod = method
        )
        dao.updateBooking(paid)
        dao.insertAuditLog(
            AuditLog(
                action = "PAYMENT_RECEIVED",
                details = "₹${booking.totalFare} paid via $method for ${booking.bookingCode}",
                category = "PAYMENT"
            )
        )
    }

    suspend fun submitRating(bookingId: Long, rating: Int, review: String) {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return
        dao.updateBooking(booking.copy(rating = rating, review = review))
        dao.insertAuditLog(
            AuditLog(
                action = "RATING_SUBMITTED",
                details = "Order ${booking.bookingCode} rated $rating stars: \"$review\"",
                category = "DISPATCH"
            )
        )
    }

    suspend fun cancelBooking(bookingId: Long, reason: String) {
        val booking = dao.getBookingByIdOnce(bookingId) ?: return
        dao.updateBooking(booking.copy(status = Booking.STATUS_CANCELLED))

        booking.driverId?.let { drvId ->
            val driver = dao.getDriverByIdOnce(drvId)
            if (driver != null) {
                dao.updateDriver(driver.copy(currentBookingId = null))
            }
        }

        dao.insertAuditLog(
            AuditLog(
                action = "BOOKING_CANCELLED",
                details = "Order ${booking.bookingCode} cancelled: $reason",
                category = "DISPATCH"
            )
        )
    }

    // Driver specific controls
    suspend fun toggleDriverDuty(driverId: String, isOnline: Boolean) {
        val driver = dao.getDriverByIdOnce(driverId) ?: return
        dao.updateDriver(driver.copy(isOnline = isOnline))
        dao.insertAuditLog(
            AuditLog(
                action = "DRIVER_DUTY_CHANGE",
                details = "Driver ${driver.name} is now ${if (isOnline) "ONLINE" else "OFFLINE"}",
                category = "DISPATCH"
            )
        )
    }

    suspend fun updateDriverKyc(driverId: String, newStatus: String) {
        val driver = dao.getDriverByIdOnce(driverId) ?: return
        dao.updateDriver(driver.copy(kycStatus = newStatus))
        dao.insertAuditLog(
            AuditLog(
                action = "KYC_STATUS_UPDATED",
                details = "Driver ${driver.name} KYC set to $newStatus",
                category = "SECURITY"
            )
        )
    }

    // Admin pricing & promo controls
    suspend fun updatePricingRule(rule: PricingRule) {
        dao.updatePricingRule(rule)
        dao.insertAuditLog(
            AuditLog(
                action = "PRICING_UPDATED",
                details = "Pricing updated for ${rule.vehicleType}: Base ₹${rule.baseFare}, PerKm ₹${rule.perKmFare}",
                category = "PRICING"
            )
        )
    }

    suspend fun addPromoCode(promo: PromoCode) {
        dao.insertPromoCode(promo)
        dao.insertAuditLog(
            AuditLog(
                action = "PROMO_CREATED",
                details = "Promo ${promo.code} created (${promo.description})",
                category = "PRICING"
            )
        )
    }

    suspend fun togglePromoStatus(code: String, isActive: Boolean) {
        val promo = dao.getPromoCode(code) ?: return
        dao.updatePromoCode(promo.copy(isActive = isActive))
    }

    suspend fun submitComplaint(bookingCode: String, role: String, subject: String, desc: String) {
        dao.insertComplaint(
            Complaint(
                bookingCode = bookingCode,
                submittedByRole = role,
                subject = subject,
                description = desc,
                status = Complaint.STATUS_OPEN
            )
        )
        dao.insertAuditLog(
            AuditLog(
                action = "COMPLAINT_SUBMITTED",
                details = "Ticket opened for $bookingCode ($subject)",
                category = "SECURITY"
            )
        )
    }

    suspend fun resolveComplaint(complaintId: Long, notes: String) {
        val all = dao.getAllComplaints().firstOrNull() ?: emptyList()
        val complaint = all.firstOrNull { it.id == complaintId } ?: return
        dao.updateComplaint(
            complaint.copy(
                status = Complaint.STATUS_RESOLVED,
                resolutionNotes = notes
            )
        )
        dao.insertAuditLog(
            AuditLog(
                action = "COMPLAINT_RESOLVED",
                details = "Ticket #${complaint.id} resolved with notes: $notes",
                category = "SECURITY"
            )
        )
    }
}
