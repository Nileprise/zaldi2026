package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ZaldiDatabase
import com.example.data.model.AuditLog
import com.example.data.model.Booking
import com.example.data.model.Complaint
import com.example.data.model.Driver
import com.example.data.model.LocationPoint
import com.example.data.model.PricingRule
import com.example.data.model.PromoCode
import com.example.data.model.VehicleType
import com.example.data.repository.FareBreakdown
import com.example.data.repository.ZaldiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppRole(val label: String) {
    CUSTOMER("Customer"),
    DRIVER("Driver Partner"),
    ADMIN("Admin Dispatch")
}

enum class CustomerSubScreen {
    HOME,
    TRACKING,
    PAYMENT,
    RATING,
    HISTORY
}

enum class DriverSubScreen {
    DASHBOARD,
    ACTIVE_TRIP,
    KYC,
    EARNINGS,
    HISTORY
}

enum class AdminTab(val title: String) {
    OVERVIEW_MAP("Live Map"),
    DRIVERS("Drivers"),
    CUSTOMERS("Customers"),
    BOOKINGS("Bookings"),
    PRICING("Pricing"),
    PROMOS("Promocodes"),
    COMPLAINTS("Complaints"),
    REPORTS("Reports"),
    AUDIT_LOGS("Audit Logs")
}

class ZaldiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ZaldiDatabase.getDatabase(application, viewModelScope)
    private val repository = ZaldiRepository(database.zaldiDao(), viewModelScope)

    // Roles and navigation
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _customerSubScreen = MutableStateFlow(CustomerSubScreen.HOME)
    val customerSubScreen: StateFlow<CustomerSubScreen> = _customerSubScreen.asStateFlow()

    private val _driverSubScreen = MutableStateFlow(DriverSubScreen.DASHBOARD)
    val driverSubScreen: StateFlow<DriverSubScreen> = _driverSubScreen.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.OVERVIEW_MAP)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Customer Booking Form state
    private val _selectedPickup = MutableStateFlow(LocationPoint.PRESET_LOCATIONS[0])
    val selectedPickup: StateFlow<LocationPoint> = _selectedPickup.asStateFlow()

    private val _selectedDrop = MutableStateFlow(LocationPoint.PRESET_LOCATIONS[1])
    val selectedDrop: StateFlow<LocationPoint> = _selectedDrop.asStateFlow()

    private val _selectedVehicle = MutableStateFlow(VehicleType.MINI_TRUCK)
    val selectedVehicle: StateFlow<VehicleType> = _selectedVehicle.asStateFlow()

    private val _selectedGoodsType = MutableStateFlow("Commercial Cargo & Boxes")
    val selectedGoodsType: StateFlow<String> = _selectedGoodsType.asStateFlow()

    private val _goodsWeightKg = MutableStateFlow(180)
    val goodsWeightKg: StateFlow<Int> = _goodsWeightKg.asStateFlow()

    private val _helperCount = MutableStateFlow(1)
    val helperCount: StateFlow<Int> = _helperCount.asStateFlow()

    private val _promoCodeInput = MutableStateFlow("ZALDI50")
    val promoCodeInput: StateFlow<String> = _promoCodeInput.asStateFlow()

    private val _fareBreakdown = MutableStateFlow(
        FareBreakdown(
            baseFare = 150.0,
            distanceFare = 135.0,
            helperFare = 120.0,
            subtotal = 405.0,
            discount = 50.0,
            finalFare = 355.0,
            appliedPromo = "ZALDI50"
        )
    )
    val fareBreakdown: StateFlow<FareBreakdown> = _fareBreakdown.asStateFlow()

    // Active driver selected in Driver Mode
    private val _currentDriverId = MutableStateFlow("DRV-101")
    val currentDriverId: StateFlow<String> = _currentDriverId.asStateFlow()

    // Status snackbar / feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Database reactive streams
    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCustomerBooking: StateFlow<Booking?> = repository.activeCustomerBooking
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allDrivers: StateFlow<List<Driver>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onlineDrivers: StateFlow<List<Driver>> = repository.onlineDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pricingRules: StateFlow<List<PricingRule>> = repository.allPricingRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promoCodes: StateFlow<List<PromoCode>> = repository.allPromoCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val complaints: StateFlow<List<Complaint>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Recalculate fare whenever pickup, drop, vehicle, or helper count changes
        refreshFare()
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun setCustomerSubScreen(screen: CustomerSubScreen) {
        _customerSubScreen.value = screen
    }

    fun setDriverSubScreen(screen: DriverSubScreen) {
        _driverSubScreen.value = screen
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun setSelectedPickup(location: LocationPoint) {
        _selectedPickup.value = location
        refreshFare()
    }

    fun setSelectedDrop(location: LocationPoint) {
        _selectedDrop.value = location
        refreshFare()
    }

    fun setSelectedVehicle(vehicle: VehicleType) {
        _selectedVehicle.value = vehicle
        refreshFare()
    }

    fun setSelectedGoodsType(type: String, weight: Int) {
        _selectedGoodsType.value = type
        _goodsWeightKg.value = weight
    }

    fun setHelperCount(count: Int) {
        _helperCount.value = count
        refreshFare()
    }

    fun setPromoCode(code: String) {
        _promoCodeInput.value = code
        refreshFare()
    }

    fun setCurrentDriverId(id: String) {
        _currentDriverId.value = id
    }

    fun refreshFare() {
        viewModelScope.launch(Dispatchers.IO) {
            val dist = LocationPoint.calculateDistanceKm(
                _selectedPickup.value.latitude, _selectedPickup.value.longitude,
                _selectedDrop.value.latitude, _selectedDrop.value.longitude
            )
            val fare = repository.calculateFare(
                vehicleType = _selectedVehicle.value,
                distanceKm = dist,
                helperCount = _helperCount.value,
                promoCodeString = _promoCodeInput.value
            )
            _fareBreakdown.value = fare
        }
    }

    fun createBooking() {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.createBooking(
                pickup = _selectedPickup.value,
                drop = _selectedDrop.value,
                vehicleType = _selectedVehicle.value,
                goodsType = _selectedGoodsType.value,
                goodsWeightKg = _goodsWeightKg.value,
                helperCount = _helperCount.value,
                promoCode = _promoCodeInput.value
            )
            _customerSubScreen.value = CustomerSubScreen.TRACKING
            showToast("Searching for nearest Zaldi driver...")
        }
    }

    fun cancelActiveBooking(bookingId: Long, reason: String = "Changed delivery plan") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cancelBooking(bookingId, reason)
            _customerSubScreen.value = CustomerSubScreen.HOME
            showToast("Booking cancelled")
        }
    }

    fun verifyPickupOtp(bookingId: Long, otpInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.verifyOtpAndStartTrip(bookingId, otpInput)
            if (success) {
                showToast("OTP Verified! Trip started.")
            } else {
                showToast("Invalid OTP. Please check with customer.")
            }
        }
    }

    fun completeDelivery(bookingId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeDelivery(bookingId)
            showToast("Delivery marked completed!")
        }
    }

    fun processPayment(bookingId: Long, method: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.processPayment(bookingId, method)
            _customerSubScreen.value = CustomerSubScreen.RATING
            showToast("Payment received successfully!")
        }
    }

    fun submitRating(bookingId: Long, rating: Int, review: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitRating(bookingId, rating, review)
            _customerSubScreen.value = CustomerSubScreen.HISTORY
            showToast("Thank you for your rating!")
        }
    }

    fun toggleDriverDuty(driverId: String, isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleDriverDuty(driverId, isOnline)
            showToast("Driver is now ${if (isOnline) "ONLINE" else "OFFLINE"}")
        }
    }

    fun updateDriverKyc(driverId: String, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDriverKyc(driverId, newStatus)
            showToast("Driver KYC set to $newStatus")
        }
    }

    fun updatePricingRule(rule: PricingRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePricingRule(rule)
            refreshFare()
            showToast("Pricing updated for ${rule.vehicleType}")
        }
    }

    fun addPromoCode(promo: PromoCode) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPromoCode(promo)
            showToast("Promo ${promo.code} added")
        }
    }

    fun togglePromoStatus(code: String, isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePromoStatus(code, isActive)
            refreshFare()
        }
    }

    fun submitComplaint(bookingCode: String, role: String, subject: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitComplaint(bookingCode, role, subject, description)
            showToast("Support ticket created")
        }
    }

    fun resolveComplaint(id: Long, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resolveComplaint(id, notes)
            showToast("Ticket #$id resolved")
        }
    }

    // Interactive Demo Runner: runs an end-to-end delivery cycle smoothly
    fun runAutoDemo() {
        viewModelScope.launch(Dispatchers.IO) {
            showToast("Starting Quick Demo simulation...")
            setRole(AppRole.CUSTOMER)
            // 1. Create booking
            val bookingId = repository.createBooking(
                pickup = LocationPoint.PRESET_LOCATIONS[0],
                drop = LocationPoint.PRESET_LOCATIONS[1],
                vehicleType = VehicleType.MINI_TRUCK,
                goodsType = "Smart Electronics & Monitors",
                goodsWeightKg = 150,
                helperCount = 1,
                promoCode = "ZALDI50"
            )
            _customerSubScreen.value = CustomerSubScreen.TRACKING

            delay(3500)
            showToast("Driver assigned! Partner arriving at pickup...")

            delay(4000)
            showToast("Driver arrived at pickup location.")

            delay(2500)
            val booking = database.zaldiDao().getBookingByIdOnce(bookingId)
            val otp = booking?.pickupOtp ?: "1234"
            repository.verifyOtpAndStartTrip(bookingId, otp)
            showToast("OTP verified! Parcel in transit to destination...")

            // Follow in transit
            delay(10000)
            repository.completeDelivery(bookingId)
            showToast("Parcel delivered! Proceeding to payment...")

            delay(2000)
            _customerSubScreen.value = CustomerSubScreen.PAYMENT
        }
    }
}
