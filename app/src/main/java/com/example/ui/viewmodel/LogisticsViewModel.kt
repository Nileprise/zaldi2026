package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookingOrder
import com.example.data.model.DriverAvailability
import com.example.data.model.DriverKyc
import com.example.data.model.DriverLocationData
import com.example.data.model.KycStatus
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCatalog
import com.example.data.repository.LogisticsRepository
import com.example.service.LocationManager
import com.example.util.DirectionsResult
import com.example.util.DistanceCalculator
import com.example.util.GoogleMapsRoutingService
import com.example.util.PlaceModel
import com.example.util.RouteDistanceInfo
import com.example.util.RoutingProfile
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

// MVI UI Events for handling Android-specific actions without leaking Context
sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class StartForegroundService(val start: Boolean) : UiEvent
    data class ShowNotification(val order: BookingOrder, val driverName: String) : UiEvent
    data class NavigateTo(val route: String) : UiEvent
}

sealed class BookingValidationResult {
    object Valid : BookingValidationResult()
    data class Invalid(
        val message: String,
        val isPickupError: Boolean = false,
        val isDropoffError: Boolean = false
    ) : BookingValidationResult()
}

@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val repository: LogisticsRepository,
    private val locationManager: LocationManager // Injected via DI
) : ViewModel() {

    // UI Event Channel (Replaces Context callbacks)
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // Auth & Identity State
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // In production, these come from a SessionManager or AuthRepository
    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Location & Routing State
    private val _pickupPlace = MutableStateFlow<PlaceModel?>(null)
    val pickupPlace: StateFlow<PlaceModel?> = _pickupPlace.asStateFlow()

    private val _dropoffPlace = MutableStateFlow<PlaceModel?>(null)
    val dropoffPlace: StateFlow<PlaceModel?> = _dropoffPlace.asStateFlow()

    private val _pickupAddress = MutableStateFlow("")
    val pickupAddress: StateFlow<String> = _pickupAddress.asStateFlow()

    private val _dropoffAddress = MutableStateFlow("")
    val dropoffAddress: StateFlow<String> = _dropoffAddress.asStateFlow()

    // Booking Details
    private val _selectedVehicleId = MutableStateFlow("tata")
    val selectedVehicleId: StateFlow<String> = _selectedVehicleId.asStateFlow()

    private val _directionsResult = MutableStateFlow<DirectionsResult?>(null)
    val directionsResult: StateFlow<DirectionsResult?> = _directionsResult.asStateFlow()

    private val _routeDistanceInfo = MutableStateFlow<RouteDistanceInfo?>(null)
    val routeDistanceInfo: StateFlow<RouteDistanceInfo?> = _routeDistanceInfo.asStateFlow()

    private val _selectedGoodsType = MutableStateFlow("Electronics & Gadgets")
    val selectedGoodsType: StateFlow<String> = _selectedGoodsType.asStateFlow()

    private val _isHelperRequired = MutableStateFlow(false)
    val isHelperRequired: StateFlow<Boolean> = _isHelperRequired.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("Online UPI")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // Driver specific state
    private val _isDriverOnline = MutableStateFlow(false)
    val isDriverOnline: StateFlow<Boolean> = _isDriverOnline.asStateFlow()

    // Live GPS telemetry from LocationManager
    val driverLocation: StateFlow<DriverLocationData?> = locationManager.currentLocation
    val isLocationServiceRunning: StateFlow<Boolean> = locationManager.isServiceRunning

    private val _driverIncomingRequest = MutableStateFlow<BookingOrder?>(null)
    val driverIncomingRequest: StateFlow<BookingOrder?> = _driverIncomingRequest.asStateFlow()

    private val _pricingMultiplier = MutableStateFlow(1.0f)

    private val _validationState = MutableStateFlow<BookingValidationResult>(BookingValidationResult.Valid)
    val validationState: StateFlow<BookingValidationResult> = _validationState.asStateFlow()

    // ============================================================================
    // Database Reactive Streams
    // ============================================================================

    val activeOrder: StateFlow<BookingOrder?> = repository.activeOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allOrders: StateFlow<List<BookingOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrivers: StateFlow<List<DriverKyc>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOrders: StateFlow<List<BookingOrder>> = repository.pendingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ============================================================================
    // Core Functions
    // ============================================================================

    fun setAuthSession(phone: String, name: String, role: UserRole) {
        _userPhone.update { phone }
        _userName.update { name }
        _currentRole.update { role }
        _isLoggedIn.update { true }
    }

    fun logout() {
        _isLoggedIn.update { false }
        _userPhone.update { "" }
        _userName.update { "" }
    }

    private fun currentRoutingProfile(): RoutingProfile {
        val tier = VehicleCatalog.tiers.find { it.id == _selectedVehicleId.value } ?: VehicleCatalog.tiers.first()
        return tier.routingProfile
    }

    private fun recalculateDirections() {
        val pickup = _pickupPlace.value ?: return
        val dropoff = _dropoffPlace.value ?: return

        viewModelScope.launch {
            val baseDirections = GoogleMapsRoutingService.calculateDirections(
                pickup = pickup,
                dropoff = dropoff,
                profile = currentRoutingProfile()
            )

            _directionsResult.update { baseDirections }
            _routeDistanceInfo.update {
                RouteDistanceInfo(
                    distanceKm = baseDirections.actualRoadKm,
                    estimatedDurationMinutes = baseDirections.etaMinutes,
                    viaRoad = baseDirections.viaRoad,
                    routeSummary = baseDirections.routeSummary
                )
            }
        }
    }

    fun setPickupPlace(place: PlaceModel) {
        _pickupPlace.update { place }
        _pickupAddress.update { place.name }
        _validationState.update { BookingValidationResult.Valid }
        recalculateDirections()
    }

    fun setDropoffPlace(place: PlaceModel) {
        _dropoffPlace.update { place }
        _dropoffAddress.update { place.name }
        _validationState.update { BookingValidationResult.Valid }
        recalculateDirections()
    }

    fun setVehicle(vehicleId: String) {
        _selectedVehicleId.update { vehicleId }
        recalculateDirections()
    }

    fun toggleHelper() {
        _isHelperRequired.update { !it }
    }

    fun calculateEstimatedFare(
        vehicleId: String = _selectedVehicleId.value,
        distanceKm: Double? = _directionsResult.value?.actualRoadKm
    ): Double {
        if (distanceKm == null) return 0.0
        val tier = VehicleCatalog.tiers.find { it.id == vehicleId } ?: VehicleCatalog.tiers.first()
        val helperCost = if (_isHelperRequired.value) 80.0 else 0.0
        val baseCalculated = (tier.baseFare + (distanceKm * tier.perKmRate) + helperCost) * _pricingMultiplier.value
        return (baseCalculated * 10.0).roundToInt() / 10.0
    }

    fun validateBooking(): BookingValidationResult {
        val pickup = _pickupAddress.value.trim()
        val dropoff = _dropoffAddress.value.trim()
        val pPlace = _pickupPlace.value
        val dPlace = _dropoffPlace.value

        if (pPlace == null || pickup.isEmpty()) {
            return BookingValidationResult.Invalid("Valid pickup location required.", isPickupError = true).also { _validationState.value = it }
        }
        if (dPlace == null || dropoff.isEmpty()) {
            return BookingValidationResult.Invalid("Valid drop-off destination required.", isDropoffError = true).also { _validationState.value = it }
        }
        if (pPlace.placeId == dPlace.placeId) {
            return BookingValidationResult.Invalid("Pickup and Drop-off cannot be identical.", isPickupError = true, isDropoffError = true).also { _validationState.value = it }
        }

        return BookingValidationResult.Valid.also { _validationState.value = it }
    }

    fun bookRide(onSuccess: (BookingOrder) -> Unit = {}) {
        if (validateBooking() !is BookingValidationResult.Valid) return

        viewModelScope.launch {
            val tier = VehicleCatalog.tiers.find { it.id == _selectedVehicleId.value }!!
            val directions = _directionsResult.value!!
            val actualKm = directions.actualRoadKm
            val fare = calculateEstimatedFare(tier.id, actualKm)
            
            // Production grade secure random generation
            val orderId = "AKH-${UUID.randomUUID().toString().take(6).uppercase()}"
            val secureOtp = String.format("%04d", SecureRandom().nextInt(10000))

            val order = BookingOrder(
                id = orderId,
                customerPhone = _userPhone.value,
                customerName = _userName.value,
                pickupAddress = _pickupAddress.value,
                dropoffAddress = _dropoffAddress.value,
                pickupPlaceId = _pickupPlace.value!!.placeId,
                dropoffPlaceId = _dropoffPlace.value!!.placeId,
                pickupLat = _pickupPlace.value!!.latitude,
                pickupLng = _pickupPlace.value!!.longitude,
                dropoffLat = _dropoffPlace.value!!.latitude,
                dropoffLng = _dropoffPlace.value!!.longitude,
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
                status = OrderStatus.PENDING.name, // Strict Enum Usage
                startOtp = secureOtp,
                etaMinutes = directions.etaMinutes
            )

            repository.createOrder(order)
            onSuccess(order)
        }
    }

    fun toggleDriverOnline() {
        val newState = !_isDriverOnline.value
        _isDriverOnline.update { newState }
        viewModelScope.launch {
            // Emits an event for the UI/Activity to start the actual Android Service
            _uiEvent.send(UiEvent.StartForegroundService(newState))
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus.name)
        }
    }

    fun assignDriverToDelivery(order: BookingOrder, driver: DriverKyc) {
        viewModelScope.launch {
            repository.assignDriverToOrder(order, driver)
            
            val assignedOrder = order.copy(
                status = OrderStatus.DRIVER_ASSIGNED.name,
                assignedDriverId = driver.driverId,
                driverName = driver.name,
                driverPhone = driver.phone,
                driverVehicleNumber = driver.vehicleNumber
            )

            // Trigger Notification UI Event instead of using Context directly
            _uiEvent.send(UiEvent.ShowNotification(assignedOrder, driver.name))
        }
    }
}
