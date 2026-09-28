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
import com.example.data.model.VehicleTier
import com.example.data.repository.LogisticsRepository
import com.example.service.LocationManager
import com.example.util.DeliveryNotificationHelper
import com.example.util.DirectionsResult
import com.example.util.DistanceCalculator
import com.example.util.GoogleMapsRoutingService
import com.example.util.PlaceModel
import com.example.util.RouteDistanceInfo
import com.example.util.RoutingProfile
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class BookingValidationResult {
    object Valid : BookingValidationResult()
    data class Invalid(
        val message: String,
        val isPickupError: Boolean = false,
        val isDropoffError: Boolean = false
    ) : BookingValidationResult()
}

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
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userPhone = MutableStateFlow("9999999999")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow("Akhil Sharma")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Place Models & Geocoding State
    private val _pickupPlace = MutableStateFlow(GoogleMapsRoutingService.placeCatalog[0]) // Indiranagar
    val pickupPlace: StateFlow<PlaceModel> = _pickupPlace.asStateFlow()

    private val _dropoffPlace = MutableStateFlow(GoogleMapsRoutingService.placeCatalog[1]) // Koramangala
    val dropoffPlace: StateFlow<PlaceModel> = _dropoffPlace.asStateFlow()

    // String address states
    private val _pickupAddress = MutableStateFlow(_pickupPlace.value.name)
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()

    private val _dropoffAddress = MutableStateFlow(_dropoffPlace.value.name)
    val dropoffAddress: StateFlow<String> = _dropoffAddress.asStateFlow()

    // Vehicle Tier state
    private val _selectedVehicleId = MutableStateFlow("tata")
    val selectedVehicleId: StateFlow<String> = _selectedVehicleId.asStateFlow()

    // Mode-specific Directions & Routing Result (Directions API & Distance Matrix)
    private val _directionsResult = MutableStateFlow(
        GoogleMapsRoutingService.calculateDirections(
            pickup = _pickupPlace.value,
            dropoff = _dropoffPlace.value,
            profile = RoutingProfile.TATA_ACE_COMMERCIAL
        )
    )
    val directionsResult: StateFlow<DirectionsResult> = _directionsResult.asStateFlow()

    // Distance calculation compatibility
    private var _customDistanceKm: Double? = null
    private val _routeDistanceInfo = MutableStateFlow(
        RouteDistanceInfo(
            distanceKm = _directionsResult.value.actualRoadKm,
            estimatedDurationMinutes = _directionsResult.value.etaMinutes,
            viaRoad = _directionsResult.value.viaRoad,
            routeSummary = _directionsResult.value.routeSummary
        )
    )
    val routeDistanceInfo: StateFlow<RouteDistanceInfo> = _routeDistanceInfo.asStateFlow()

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

    // Dynamic pricing multiplier
    private val _pricingMultiplier = MutableStateFlow(1.0f)
    val pricingMultiplier: StateFlow<Float> = _pricingMultiplier.asStateFlow()

    // Validation State
    private val _validationState = MutableStateFlow<BookingValidationResult>(BookingValidationResult.Valid)
    val validationState: StateFlow<BookingValidationResult> = _validationState.asStateFlow()

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

    private fun currentRoutingProfile(): RoutingProfile {
        val tier = VehicleCatalog.tiers.find { it.id == _selectedVehicleId.value } ?: VehicleCatalog.tiers[2]
        return tier.routingProfile
    }

    private fun recalculateDirections() {
        val profile = currentRoutingProfile()
        val custom = _customDistanceKm

        val baseDirections = GoogleMapsRoutingService.calculateDirections(
            pickup = _pickupPlace.value,
            dropoff = _dropoffPlace.value,
            profile = profile
        )

        if (custom != null) {
            val estMin = DistanceCalculator.calculateEstimatedMinutes(custom)
            _directionsResult.value = baseDirections.copy(
                actualRoadKm = custom,
                etaMinutes = estMin,
                routeSummary = "$custom km • ~$estMin mins (${baseDirections.viaRoad})"
            )
            _routeDistanceInfo.value = RouteDistanceInfo(
                distanceKm = custom,
                estimatedDurationMinutes = estMin,
                viaRoad = baseDirections.viaRoad,
                routeSummary = "$custom km • ~$estMin mins"
            )
        } else {
            _directionsResult.value = baseDirections
            _routeDistanceInfo.value = RouteDistanceInfo(
                distanceKm = baseDirections.actualRoadKm,
                estimatedDurationMinutes = baseDirections.etaMinutes,
                viaRoad = baseDirections.viaRoad,
                routeSummary = baseDirections.routeSummary
            )
        }
    }

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

    /**
     * Autocomplete Place search
     */
    fun searchPlaces(query: String): List<PlaceModel> {
        return GoogleMapsRoutingService.searchPlaces(query)
    }

    /**
     * Sets Pickup via PlaceModel (Google Maps Autocomplete / Geocoded)
     */
    fun setPickupPlace(place: PlaceModel) {
        _pickupPlace.value = place
        _pickupAddress.value = place.name
        _customDistanceKm = null
        _validationState.value = BookingValidationResult.Valid
        recalculateDirections()
    }

    /**
     * Sets Dropoff via PlaceModel (Google Maps Autocomplete / Geocoded)
     */
    fun setDropoffPlace(place: PlaceModel) {
        _dropoffPlace.value = place
        _dropoffAddress.value = place.name
        _customDistanceKm = null
        _validationState.value = BookingValidationResult.Valid
        recalculateDirections()
    }

    fun setPickup(address: String) {
        _pickupAddress.value = address
        _pickupPlace.value = GoogleMapsRoutingService.geocode(address)
        _customDistanceKm = null
        _validationState.value = BookingValidationResult.Valid
        recalculateDirections()
    }

    fun setDropoff(address: String) {
        _dropoffAddress.value = address
        _dropoffPlace.value = GoogleMapsRoutingService.geocode(address)
        _customDistanceKm = null
        _validationState.value = BookingValidationResult.Valid
        recalculateDirections()
    }

    fun setVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
        recalculateDirections() // Re-routes according to vehicle agility (2-wheeler vs 3-wheeler vs truck)
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

    fun setCustomDistance(km: Double) {
        val clamped = (kotlin.math.max(1.0, km) * 10.0).roundToInt() / 10.0
        _customDistanceKm = clamped
        recalculateDirections()
    }

    fun resetDistanceToAuto() {
        _customDistanceKm = null
        recalculateDirections()
    }

    fun setPricingMultiplier(multiplier: Float) {
        _pricingMultiplier.value = multiplier
    }

    /**
     * Calculates automatic transparent fare based on actual road KM & vehicle tier
     */
    fun calculateEstimatedFare(
        vehicleId: String = _selectedVehicleId.value,
        distanceKm: Double = _directionsResult.value.actualRoadKm
    ): Double {
        val tier = VehicleCatalog.tiers.find { it.id == vehicleId } ?: VehicleCatalog.tiers[0]
        val helperCost = if (_isHelperRequired.value) 80.0 else 0.0
        val baseCalculated = (tier.baseFare + (distanceKm * tier.perKmRate) + helperCost) * _pricingMultiplier.value
        return (baseCalculated * 10.0).roundToInt() / 10.0
    }

    /**
     * Validates that pickup and dropoff locations are properly selected and distinct
     */
    fun validateBooking(): BookingValidationResult {
        val pickup = _pickupAddress.value.trim()
        val dropoff = _dropoffAddress.value.trim()

        if (pickup.isEmpty() || pickup.length < 3) {
            val err = BookingValidationResult.Invalid("Please enter or select a valid pickup location.", isPickupError = true)
            _validationState.value = err
            return err
        }

        if (dropoff.isEmpty() || dropoff.length < 3) {
            val err = BookingValidationResult.Invalid("Please enter or select a valid drop-off destination.", isDropoffError = true)
            _validationState.value = err
            return err
        }

        // Check if pickup and dropoff are effectively identical
        val pPlace = _pickupPlace.value
        val dPlace = _dropoffPlace.value
        val isIdenticalPlace = (pPlace.placeId == dPlace.placeId) ||
                (pickup.equals(dropoff, ignoreCase = true)) ||
                (GoogleMapsRoutingService.haversineDistance(pPlace.latitude, pPlace.longitude, dPlace.latitude, dPlace.longitude) < 0.1)

        if (isIdenticalPlace) {
            val err = BookingValidationResult.Invalid(
                "Pickup and Drop-off locations cannot be identical. Please select different points.",
                isPickupError = true,
                isDropoffError = true
            )
            _validationState.value = err
            return err
        }

        val valid = BookingValidationResult.Valid
        _validationState.value = valid
        return valid
    }

    /**
     * Books ride and saves full Place IDs, Lat/Lng coordinates, Actual Road KM, and Route info to backend Room DB
     */
    fun bookRide(
        onSuccess: (BookingOrder) -> Unit = {}
    ) {
        val validation = validateBooking()
        if (validation !is BookingValidationResult.Valid) {
            return
        }

        viewModelScope.launch {
            val tier = VehicleCatalog.tiers.find { it.id == _selectedVehicleId.value } ?: VehicleCatalog.tiers[2]
            val directions = _directionsResult.value
            val actualKm = directions.actualRoadKm
            val fare = calculateEstimatedFare(tier.id, actualKm)
            val randomId = "AKH-" + Random.nextInt(10000, 99999)
            val randomOtp = (Random.nextInt(1000, 9000) + 1000).toString()

            val pPlace = _pickupPlace.value
            val dPlace = _dropoffPlace.value

            val order = BookingOrder(
                id = randomId,
                customerPhone = _userPhone.value,
                customerName = _userName.value,
                pickupAddress = _pickupAddress.value.ifBlank { pPlace.name },
                dropoffAddress = _dropoffAddress.value.ifBlank { dPlace.name },
                pickupPlaceId = pPlace.placeId,
                dropoffPlaceId = dPlace.placeId,
                pickupLat = pPlace.latitude,
                pickupLng = pPlace.longitude,
                dropoffLat = dPlace.latitude,
                dropoffLng = dPlace.longitude,
                vehicleTierId = tier.id,
                vehicleName = tier.name,
                goodsType = _selectedGoodsType.value,
                helperRequired = _isHelperRequired.value,
                helperFee = if (_isHelperRequired.value) 80.0 else 0.0,
                fare = fare,
                distanceKm = actualKm,
                actualRoadKm = actualKm,
                routeSummary = directions.routeSummary,
                routingProfile = tier.routingProfile.name,
                paymentMethod = _selectedPaymentMethod.value,
                status = "DRIVER_ASSIGNED",
                driverName = "Ravi Kumar",
                driverPhone = "+91 98452 11094",
                driverRating = 4.88,
                driverVehicleNumber = "KA 05 MX 2190",
                startOtp = randomOtp,
                etaMinutes = directions.etaMinutes,
                assignedDriverId = "DRV-101"
            )

            repository.createOrder(order)
            _driverIncomingRequest.value = order
            onSuccess(order)
        }
    }

    /**
     * Snap driver GPS telemetry to road using Roads API algorithm
     */
    fun getSnappedDriverLocation(rawGps: LatLng?): LatLng {
        if (rawGps == null) {
            val waypoints = _directionsResult.value.waypoints
            return if (waypoints.size > 2) waypoints[waypoints.size / 2] else LatLng(12.9710, 77.6350)
        }
        val waypoints = _directionsResult.value.waypoints
        return GoogleMapsRoutingService.snapToRoad(rawGps, waypoints)
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
            vehicleTierId = "tata",
            vehicleName = "Tata Ace (Chota Hathi)",
            goodsType = trip.third,
            helperRequired = true,
            helperFee = 80.0,
            fare = randomFare,
            distanceKm = distance,
            actualRoadKm = distance,
            routeSummary = "$distance km • ~20 mins",
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
