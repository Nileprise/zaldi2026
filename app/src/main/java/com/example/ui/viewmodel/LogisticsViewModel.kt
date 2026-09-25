package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BookingOrder
import com.example.data.model.DriverKyc
import com.example.data.model.DriverLocationData
import com.example.data.model.UserRole
import com.example.data.model.VehicleCatalog
import com.example.data.repository.LogisticsRepository
import com.example.service.LocationManager
import com.example.util.DeliveryNotificationHelper
import com.example.util.DistanceCalculator
import com.example.util.RouteDistanceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class LogisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LogisticsRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LogisticsRepository(db)
    }

    // Role state
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Auth state
    private val _isLoggedIn = MutableStateFlow(true) // Starts logged in for seamless demo/preview
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userPhone = MutableStateFlow("9999999999")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow("Akhil Sharma")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Booking input fields
    private val _pickupAddress = MutableStateFlow("Indiranagar 100ft Rd, Bengaluru")
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()

    private val _dropoffAddress = MutableStateFlow("Koramangala 4th Block, Bengaluru")
    val dropoffAddress: StateFlow<String> = _dropoffAddress.asStateFlow()

    // Exact calculated distance and route information
    private var _customDistanceKm: Double? = null
    private val _routeDistanceInfo = MutableStateFlow(
        DistanceCalculator.calculateExactDistance(
            _pickupAddress.value,
            _dropoffAddress.value
        )
    )
    val routeDistanceInfo: StateFlow<RouteDistanceInfo> = _routeDistanceInfo.asStateFlow()

    private fun updateCalculatedDistance() {
        val custom = _customDistanceKm
        if (custom != null) {
            val estMin = DistanceCalculator.calculateEstimatedMinutes(custom)
            _routeDistanceInfo.value = RouteDistanceInfo(
                distanceKm = custom,
                estimatedDurationMinutes = estMin,
                viaRoad = "Adjusted Exact Corridor ($custom km)",
                routeSummary = "$custom km • ~$estMin mins"
            )
        } else {
            _routeDistanceInfo.value = DistanceCalculator.calculateExactDistance(
                _pickupAddress.value,
                _dropoffAddress.value
            )
        }
    }

    fun setCustomDistance(km: Double) {
        val clamped = (kotlin.math.max(1.0, km) * 10.0).roundToInt() / 10.0
        _customDistanceKm = clamped
        updateCalculatedDistance()
    }

    fun resetDistanceToAuto() {
        _customDistanceKm = null
        updateCalculatedDistance()
    }

    private val _selectedVehicleId = MutableStateFlow("tata")
    val selectedVehicleId: StateFlow<String> = _selectedVehicleId.asStateFlow()

    private val _selectedGoodsType = MutableStateFlow("Electronics & Gadgets")
    val selectedGoodsType: StateFlow<String> = _selectedGoodsType.asStateFlow()

    private val _isHelperRequired = MutableStateFlow(true)
    val isHelperRequired: StateFlow<Boolean> = _isHelperRequired.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("Cash on Delivery (COD)")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // Driver specific state
    private val _isDriverOnline = MutableStateFlow(true)
    val isDriverOnline: StateFlow<Boolean> = _isDriverOnline.asStateFlow()

    // Live heads-up delivery alert state (in-app banner)
    private val _activeDeliveryAlert = MutableStateFlow<DeliveryAlertData?>(null)
    val activeDeliveryAlert: StateFlow<DeliveryAlertData?> = _activeDeliveryAlert.asStateFlow()

    fun dismissDeliveryAlert() {
        _activeDeliveryAlert.value = null
    }

    // Live GPS telemetry from background service
    val driverLocation: StateFlow<DriverLocationData?> = LocationManager.currentLocation
    val isLocationServiceRunning: StateFlow<Boolean> = LocationManager.isServiceRunning

    private val _driverIncomingRequest = MutableStateFlow<BookingOrder?>(null)
    val driverIncomingRequest: StateFlow<BookingOrder?> = _driverIncomingRequest.asStateFlow()

    // Admin pricing multiplier (Dynamic pricing engine)
    private val _pricingMultiplier = MutableStateFlow(1.0f)
    val pricingMultiplier: StateFlow<Float> = _pricingMultiplier.asStateFlow()

    // Orders from DB
    val activeOrder: StateFlow<BookingOrder?> = repository.activeOrder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allOrders: StateFlow<List<BookingOrder>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allDrivers: StateFlow<List<DriverKyc>> = repository.allDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingOrders: StateFlow<List<BookingOrder>> = repository.pendingOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val approvedDrivers: StateFlow<List<DriverKyc>> = repository.approvedDrivers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setRole(role: UserRole) {
        _currentRole.value = role
        when (role) {
            UserRole.CUSTOMER -> {
                _userName.value = "Akhil Sharma"
                _userPhone.value = "9999999999"
            }
            UserRole.DRIVER -> {
                _userName.value = "Ravi Kumar"
                _userPhone.value = "8888888888"
            }
            UserRole.ADMIN -> {
                _userName.value = "Admin Operations"
                _userPhone.value = "7777777777"
            }
        }
    }

    fun login(phone: String, role: UserRole) {
        _userPhone.value = phone
        setRole(role)
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun setPickup(address: String) {
        _pickupAddress.value = address
        _customDistanceKm = null
        updateCalculatedDistance()
    }

    fun setDropoff(address: String) {
        _dropoffAddress.value = address
        _customDistanceKm = null
        updateCalculatedDistance()
    }

    fun setVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }

    fun setGoodsType(type: String) {
        _selectedGoodsType.value = type
    }

    fun toggleHelper() {
        _isHelperRequired.value = !_isHelperRequired.value
    }

    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun toggleDriverOnline(context: Context) {
        val newState = !_isDriverOnline.value
        _isDriverOnline.value = newState
        if (newState) {
            LocationManager.startLocationService(context)
        } else {
            LocationManager.stopLocationService(context)
        }
    }

    fun startLocationService(context: Context) {
        LocationManager.startLocationService(context)
    }

    fun stopLocationService(context: Context) {
        LocationManager.stopLocationService(context)
    }

    fun setPricingMultiplier(multiplier: Float) {
        _pricingMultiplier.value = multiplier
    }

    fun calculateEstimatedFare(
        vehicleId: String,
        distanceKm: Double = _routeDistanceInfo.value.distanceKm
    ): Double {
        val tier = VehicleCatalog.tiers.find { it.id == vehicleId } ?: VehicleCatalog.tiers[0]
        val helperCost = if (_isHelperRequired.value) 80.0 else 0.0
        val baseCalculated = (tier.baseFare + (distanceKm * tier.perKmRate) + helperCost) * _pricingMultiplier.value
        return (baseCalculated * 10.0).roundToInt() / 10.0
    }

    fun bookRide(
        onSuccess: (BookingOrder) -> Unit = {}
    ) {
        viewModelScope.launch {
            val tier = VehicleCatalog.tiers.find { it.id == _selectedVehicleId.value } ?: VehicleCatalog.tiers[2]
            val routeInfo = _routeDistanceInfo.value
            val distance = routeInfo.distanceKm
            val fare = calculateEstimatedFare(tier.id, distance)
            val randomId = "AKH-" + Random.nextInt(10000, 99999)
            val randomOtp = (Random.nextInt(1000, 9000) + 1000).toString()

            val order = BookingOrder(
                id = randomId,
                customerPhone = _userPhone.value,
                customerName = _userName.value,
                pickupAddress = _pickupAddress.value.ifBlank { "Indiranagar 100ft Rd, Bengaluru" },
                dropoffAddress = _dropoffAddress.value.ifBlank { "Koramangala 4th Block, Bengaluru" },
                vehicleTierId = tier.id,
                vehicleName = tier.name,
                goodsType = _selectedGoodsType.value,
                helperRequired = _isHelperRequired.value,
                helperFee = if (_isHelperRequired.value) 80.0 else 0.0,
                fare = fare,
                distanceKm = distance,
                paymentMethod = _selectedPaymentMethod.value,
                status = "DRIVER_ASSIGNED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = randomOtp,
                etaMinutes = routeInfo.estimatedDurationMinutes
            )
            repository.createOrder(order)
            _driverIncomingRequest.value = order
            onSuccess(order)
        }
    }

    fun cancelActiveRide(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
        }
    }

    fun completeActiveRide(orderId: String) {
        viewModelScope.launch {
            repository.completeOrder(orderId)
        }
    }

    fun acceptDriverRide(order: BookingOrder) {
        viewModelScope.launch {
            val acceptedOrder = order.copy(status = "IN_TRANSIT")
            repository.createOrder(acceptedOrder)
            _driverIncomingRequest.value = null
        }
    }

    fun updateDriverDeliveryStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun completeDriverDelivery(orderId: String) {
        viewModelScope.launch {
            repository.completeOrder(orderId)
        }
    }

    fun simulateIncomingRequest() {
        val sampleTrips = listOf(
            Triple(
                "Indiranagar 100ft Rd, Bengaluru",
                "Koramangala 4th Block, Bengaluru",
                "Commercial Plywood & Glass Sheets"
            ),
            Triple(
                "Whitefield EPIP Zone, Bengaluru",
                "MG Road Retail Corridor, Bengaluru",
                "Packaged Consumer Electronics (12 Boxes)"
            ),
            Triple(
                "Peenya Industrial Area Stage 2, Bengaluru",
                "Rajajinagar Industrial Town, Bengaluru",
                "Precision CNC Machine Tooling Parts"
            ),
            Triple(
                "HSR Layout Sector 2, Bengaluru",
                "Electronic City Phase 1, Bengaluru",
                "Office Relocation & Workstation Furniture"
            )
        )
        val idx = Random.nextInt(sampleTrips.size)
        val trip = sampleTrips[idx]
        val distance = 5.0 + Random.nextInt(8)
        val randomFare = (350.0 + (distance * 28.0)).toInt().toDouble()
        val randomOtp = (Random.nextInt(1000, 9000) + 1000).toString()

        val sampleOrder = BookingOrder(
            id = "AKH-" + Random.nextInt(10000, 99999),
            customerPhone = "+91 98451 " + Random.nextInt(10000, 99999),
            customerName = listOf("Ananya Sharma", "Vikram Malhotra", "Karthik Iyer", "Sunita Rao").random(),
            pickupAddress = trip.first,
            dropoffAddress = trip.second,
            vehicleTierId = "tata_ace",
            vehicleName = "Tata Ace (Chota Hathi)",
            goodsType = trip.third,
            helperRequired = true,
            helperFee = 80.0,
            fare = randomFare,
            distanceKm = distance,
            paymentMethod = listOf("Online UPI", "Cash on Delivery", "Corporate Invoice").random(),
            status = "DRIVER_ASSIGNED",
            driverName = "Ravi Kumar",
            driverPhone = "+91 98452 11094",
            driverRating = 4.88,
            driverVehicleNumber = "KA 05 MX 2190",
            startOtp = randomOtp,
            etaMinutes = Random.nextInt(8, 16)
        )
        _driverIncomingRequest.value = sampleOrder
    }

    fun declineDriverRide() {
        _driverIncomingRequest.value = null
    }

    fun updateKycStatus(driverId: String, status: String) {
        viewModelScope.launch {
            repository.updateKycStatus(driverId, status)
        }
    }

    fun setDriverAvailability(driverId: String, availability: String) {
        viewModelScope.launch {
            repository.updateDriverAvailability(driverId, availability)
        }
    }

    fun assignDriverToDelivery(
        order: BookingOrder,
        driver: DriverKyc,
        context: Context? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.assignDriverToOrder(order, driver)

            val assignedOrder = order.copy(
                status = "DRIVER_ASSIGNED",
                assignedDriverId = driver.driverId,
                driverName = driver.name,
                driverPhone = driver.phone,
                driverVehicleNumber = driver.vehicleNumber
            )

            // Trigger local notification to alert the driver of new delivery request
            val ctx = context ?: getApplication<Application>().applicationContext
            DeliveryNotificationHelper.notifyDriverAssignment(
                context = ctx,
                order = assignedOrder,
                driverName = driver.name
            )

            _activeDeliveryAlert.value = DeliveryAlertData(
                order = assignedOrder,
                driverName = driver.name
            )

            // If the assigned driver is the active demo driver (Ravi Kumar), update live prompt
            if (driver.driverId == "DRV-101") {
                _driverIncomingRequest.value = assignedOrder
            }

            onComplete()
        }
    }

    fun unassignDriverFromDelivery(orderId: String, driverId: String) {
        viewModelScope.launch {
            repository.unassignDriver(orderId, driverId)
            if (driverId == "DRV-101" && _driverIncomingRequest.value?.id == orderId) {
                _driverIncomingRequest.value = null
            }
        }
    }

    fun autoDispatchPendingOrders(context: Context? = null) {
        viewModelScope.launch {
            val pending = pendingOrders.value
            val available = allDrivers.value.filter { it.status == "APPROVED" && it.availabilityStatus == "AVAILABLE" }
            val ctx = context ?: getApplication<Application>().applicationContext

            var idx = 0
            for (order in pending) {
                if (idx < available.size) {
                    val driver = available[idx]
                    repository.assignDriverToOrder(order, driver)
                    val assignedOrder = order.copy(
                        status = "DRIVER_ASSIGNED",
                        assignedDriverId = driver.driverId,
                        driverName = driver.name,
                        driverPhone = driver.phone,
                        driverVehicleNumber = driver.vehicleNumber
                    )
                    DeliveryNotificationHelper.notifyDriverAssignment(
                        context = ctx,
                        order = assignedOrder,
                        driverName = driver.name
                    )
                    _activeDeliveryAlert.value = DeliveryAlertData(
                        order = assignedOrder,
                        driverName = driver.name
                    )
                    if (driver.driverId == "DRV-101") {
                        _driverIncomingRequest.value = assignedOrder
                    }
                    idx++
                }
            }
        }
    }

    fun sendTestDriverNotification(context: Context) {
        val activeOrFirst = allOrders.value.firstOrNull() ?: BookingOrder(
            id = "AKH-49210",
            customerPhone = "+91 99999 11111",
            customerName = "Priya Sharma",
            pickupAddress = "Indiranagar 100ft Rd, Bengaluru",
            dropoffAddress = "Koramangala 4th Block, Bengaluru",
            vehicleTierId = "tata",
            vehicleName = "Tata Ace",
            goodsType = "Electronics & Machine Tooling",
            helperRequired = true,
            helperFee = 80.0,
            fare = 420.0,
            distanceKm = 6.4,
            paymentMethod = "Online UPI",
            status = "DRIVER_ASSIGNED"
        )
        DeliveryNotificationHelper.notifyDriverAssignment(
            context = context,
            order = activeOrFirst,
            driverName = "Ravi Kumar"
        )
        _activeDeliveryAlert.value = DeliveryAlertData(
            order = activeOrFirst,
            driverName = "Ravi Kumar"
        )
    }
}

data class DeliveryAlertData(
    val order: BookingOrder,
    val driverName: String,
    val timestamp: Long = System.currentTimeMillis()
)
