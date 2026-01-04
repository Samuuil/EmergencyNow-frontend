package com.example.emergencynow.ui.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.usecase.ambulance.AssignAmbulanceDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.GetAmbulanceByDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.GetAvailableAmbulancesUseCase
import com.example.emergencynow.domain.usecase.call.UpdateCallStatusUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalRouteUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalsForCallUseCase
import com.example.emergencynow.domain.usecase.hospital.SelectHospitalUseCase
import com.example.emergencynow.domain.usecase.user.GetUserRoleUseCase
import com.example.emergencynow.domain.repository.CallRepository
import com.example.emergencynow.domain.repository.UserRepository
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.CallOffer
import com.example.emergencynow.ui.util.DriverSocketManager
import com.example.emergencynow.ui.util.UserSocketManager
import com.example.emergencynow.data.service.AmbulanceService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userLocation: LatLng? = null,
    val isDriver: Boolean = false,
    val isDoctor: Boolean = false,
    val assignedAmbulanceId: String? = null,
    val assignedAmbulancePlate: String? = null,
    val isSocketConnected: Boolean = false,
    val incomingCallOffer: CallOffer? = null,
    val activeCallId: String? = null,
    val patientEgn: String? = null,
    val emergencyLocation: LatLng? = null,
    val activeRoutePolyline: List<LatLng> = emptyList(),
    val activeRouteDistance: Int = 0,
    val activeRouteDuration: Int = 0,
    val activeRouteSteps: List<String> = emptyList(),
    val driverCurrentLocation: LatLng? = null,
    val callStatus: CallStatus = CallStatus.EN_ROUTE,
    val showHospitalSelection: Boolean = false,
    val availableHospitals: List<HospitalDto> = emptyList(),
    val isLoadingHospitals: Boolean = false,
    val isSelectingHospital: Boolean = false,
    val selectedHospitalName: String? = null,
    val hospitalLocation: LatLng? = null,
    val hospitalRoutePolyline: List<LatLng> = emptyList(),
    val hospitalRouteDistance: Int = 0,
    val hospitalRouteDuration: Int = 0,
    val hospitalRouteSteps: List<String> = emptyList(),
    val ambulanceLocation: LatLng? = null,
    val userCallStatus: String? = null
)

enum class CallStatus {
    EN_ROUTE,
    ARRIVED,
    NAVIGATING_TO_HOSPITAL
}

class HomeViewModel(
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val getAmbulanceByDriverUseCase: GetAmbulanceByDriverUseCase,
    private val getAvailableAmbulancesUseCase: GetAvailableAmbulancesUseCase,
    private val assignAmbulanceDriverUseCase: AssignAmbulanceDriverUseCase,
    private val unassignAmbulanceDriverUseCase: com.example.emergencynow.domain.usecase.ambulance.UnassignAmbulanceDriverUseCase,
    private val updateCallStatusUseCase: UpdateCallStatusUseCase,
    private val getHospitalsForCallUseCase: GetHospitalsForCallUseCase,
    private val selectHospitalUseCase: SelectHospitalUseCase,
    private val getHospitalRouteUseCase: GetHospitalRouteUseCase,
    private val ambulanceService: AmbulanceService,
    private val callRepository: CallRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val accessToken = AuthSession.accessToken
                val userId = AuthSession.userId
                
                if (!accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                    val role = getUserRoleUseCase(userId).getOrThrow()
                    val isDriver = role == "DRIVER"
                    val isDoctor = role == "DOCTOR"
                    
                    _uiState.value = _uiState.value.copy(isDriver = isDriver, isDoctor = isDoctor)
                    
                    if (isDriver) {
                        loadAmbulanceData(userId)
                    } else {
                        Log.d("HomeViewModel", "User detected - connecting to WebSocket NOW")
                        connectUserToWebSocket()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Missing authentication credentials",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load user data", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun refreshData() {
        if (_uiState.value.isDriver) {
            val userId = AuthSession.userId
            if (!userId.isNullOrEmpty()) {
                viewModelScope.launch {
                    loadAmbulanceData(userId)
                }
            }
        }
    }

    fun retryConnection() {
        Log.d("HomeViewModel", "════════════════════════════════════════")
        Log.d("HomeViewModel", "Retry connection requested by user")
        Log.d("HomeViewModel", "isDriver: ${_uiState.value.isDriver}")
        Log.d("HomeViewModel", "assignedAmbulanceId: ${_uiState.value.assignedAmbulanceId}")
        Log.d("HomeViewModel", "Current isSocketConnected: ${_uiState.value.isSocketConnected}")
        Log.d("HomeViewModel", "════════════════════════════════════════")
        
        val accessToken = AuthSession.accessToken
        val ambulanceId = _uiState.value.assignedAmbulanceId
        
        if (!accessToken.isNullOrEmpty() && ambulanceId != null) {
            DriverSocketManager.disconnect()
            
            viewModelScope.launch {
                delay(500)
                Log.d("HomeViewModel", "Attempting reconnection after cleanup...")
                connectToWebSocket(ambulanceId)
            }
        } else {
            Log.e("HomeViewModel", "Cannot retry - missing accessToken or ambulanceId")
            Log.e("HomeViewModel", "   accessToken: ${if (accessToken.isNullOrEmpty()) "MISSING" else "present (${accessToken.length} chars)"}")
            Log.e("HomeViewModel", "   ambulanceId: ${ambulanceId ?: "MISSING"}")
            
            val errorMsg = when {
                accessToken.isNullOrEmpty() -> "Cannot connect: Access token is missing. Please log out and log in again."
                ambulanceId == null -> "Cannot connect: No ambulance assigned. Please select an ambulance first."
                else -> "Cannot connect: Unknown error"
            }
            
            _uiState.value = _uiState.value.copy(
                error = errorMsg
            )
        }
    }

    private suspend fun loadAmbulanceData(userId: String) {
        try {
            val ambulance = getAmbulanceByDriverUseCase(userId).getOrNull()
            _uiState.value = _uiState.value.copy(
                assignedAmbulanceId = ambulance?.id,
                assignedAmbulancePlate = ambulance?.licensePlate,
                isLoading = false
            )

            ambulance?.id?.let { ambulanceId ->
                connectToWebSocket(ambulanceId)
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load ambulance data", e)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun connectToWebSocket(ambulanceId: String) {
        val accessToken = AuthSession.accessToken
        if (!accessToken.isNullOrEmpty()) {
            Log.d("HomeViewModel", "════════════════════════════════════════")
            Log.d("HomeViewModel", "Setting up driver socket connection...")
            Log.d("HomeViewModel", "Ambulance ID: $ambulanceId")
            Log.d("HomeViewModel", "Token present: ${!accessToken.isNullOrEmpty()}")
            Log.d("HomeViewModel", "Token length: ${accessToken.length}")
            Log.d("HomeViewModel", "Base URL: ${com.example.emergencynow.ui.util.NetworkConfig.currentBase()}")
            Log.d("HomeViewModel", "════════════════════════════════════════")
            
            DriverSocketManager.onCallOffer = { offer ->
                Log.d("HomeViewModel", "Call offer received: ${offer.callId}")
                _uiState.value = _uiState.value.copy(
                    incomingCallOffer = offer,
                    emergencyLocation = LatLng(offer.latitude, offer.longitude)
                )
            }
            
            DriverSocketManager.onConnectionChange = { connected ->
                Log.d("HomeViewModel", "Driver socket connection changed: $connected")
                _uiState.value = _uiState.value.copy(
                    isSocketConnected = connected,
                    error = if (!connected) "Connection failed. Check logs for details." else null
                )
            }
            
            DriverSocketManager.onCallRoute = { route ->
                Log.d("HomeViewModel", "Call route received: ${route.callId}")
                _uiState.value = _uiState.value.copy(
                    activeCallId = route.callId,
                    activeRoutePolyline = decodePolyline(route.polyline),
                    activeRouteDistance = route.distance,
                    activeRouteDuration = route.duration,
                    activeRouteSteps = route.steps
                )
                fetchPatientEgn(route.callId)
            }
            
            DriverSocketManager.onRouteUpdate = { route ->
                Log.d("HomeViewModel", "Route update received: ${route.callId}")
                _uiState.value = _uiState.value.copy(
                    activeRoutePolyline = decodePolyline(route.polyline),
                    activeRouteDistance = route.distance,
                    activeRouteDuration = route.duration,
                    activeRouteSteps = route.steps
                )
            }
            
            if (DriverSocketManager.isConnected()) {
                Log.d("HomeViewModel", "Disconnecting existing socket before reconnecting...")
                DriverSocketManager.disconnect()
            }
            
            Log.d("HomeViewModel", "Connecting driver socket...")
            DriverSocketManager.connect(accessToken)
            
            viewModelScope.launch {
                while (true) {
                    delay(5000)
                    if (_uiState.value.isDriver && _uiState.value.assignedAmbulanceId != null) {
                        val actuallyConnected = DriverSocketManager.isConnected()
                        if (actuallyConnected != _uiState.value.isSocketConnected) {
                            Log.w("HomeViewModel", "Connection state mismatch detected - fixing: actuallyConnected=$actuallyConnected, uiState=${_uiState.value.isSocketConnected}")
                            _uiState.value = _uiState.value.copy(isSocketConnected = actuallyConnected)
                        }
                    }
                }
            }
        }
    }

    fun updateUserLocation(location: LatLng) {
        _uiState.value = _uiState.value.copy(userLocation = location)
    }

    fun acceptCall(callId: String) {
        DriverSocketManager.acceptCall(callId)
        _uiState.value = _uiState.value.copy(
            incomingCallOffer = null,
            activeCallId = callId
        )
        fetchPatientEgn(callId)
    }
    
    private fun fetchPatientEgn(callId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Fetching patient EGN for call: $callId")
                
                val callResult = callRepository.getCallById(callId)
                callResult.fold(
                    onSuccess = { callResponse ->
                        Log.d("HomeViewModel", "Call response received - userEgn: ${callResponse.userEgn}")
                        if (callResponse.userEgn != null) {
                            _uiState.value = _uiState.value.copy(patientEgn = callResponse.userEgn)
                            Log.d("HomeViewModel", "Patient EGN set in UI state: ${callResponse.userEgn}")
                        } else {
                            Log.w("HomeViewModel", "User EGN is null in call response")
                            _uiState.value = _uiState.value.copy(patientEgn = null)
                        }
                    },
                    onFailure = { error ->
                        Log.e("HomeViewModel", "Failed to fetch call details: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching patient EGN: ${e.message}", e)
            }
        }
    }

    fun declineCall(callId: String) {
        DriverSocketManager.declineCall(callId)
        _uiState.value = _uiState.value.copy(incomingCallOffer = null)
    }

    fun updateCallStatus(status: CallStatus) {
        viewModelScope.launch {
            try {
                val callId = _uiState.value.activeCallId
                if (callId != null) {
                    val statusString = when (status) {
                        CallStatus.EN_ROUTE -> "en_route"
                        CallStatus.ARRIVED -> "arrived"
                        CallStatus.NAVIGATING_TO_HOSPITAL -> "navigating_to_hospital"
                    }
                    updateCallStatusUseCase(callId, statusString)
                    _uiState.value = _uiState.value.copy(callStatus = status)
                    
                    if (status == CallStatus.ARRIVED) {
                        _uiState.value = _uiState.value.copy(
                            activeRoutePolyline = emptyList(),
                            activeRouteDistance = 0,
                            activeRouteDuration = 0
                        )
                        loadHospitals(callId)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to update call status", e)
            }
        }
    }

    private suspend fun loadHospitals(callId: String) {
        try {
            Log.d("HomeViewModel", "loadHospitals called for callId: $callId")
            _uiState.value = _uiState.value.copy(
                isLoadingHospitals = true,
                showHospitalSelection = true
            )

            val location = _uiState.value.emergencyLocation ?: _uiState.value.userLocation
            if (location == null) {
                Log.e("HomeViewModel", "No location available for hospital suggestions")
                _uiState.value = _uiState.value.copy(
                    error = "Location not available for hospital suggestions",
                    isLoadingHospitals = false
                )
                return
            }

            Log.d("HomeViewModel", "Fetching hospitals for location: ${location.latitude},${location.longitude}")
            val hospitals = getHospitalsForCallUseCase(
                callId = callId,
                latitude = location.latitude,
                longitude = location.longitude
            ).getOrThrow()
            Log.d("HomeViewModel", "Received ${hospitals.size} hospitals")
            _uiState.value = _uiState.value.copy(
                availableHospitals = hospitals,
                isLoadingHospitals = false
            )
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load hospitals: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoadingHospitals = false,
                error = "Failed to load hospitals: ${e.message}"
            )
        }
    }

    fun selectHospital(hospitalId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "selectHospital called with hospitalId: $hospitalId")
                val callId = _uiState.value.activeCallId
                if (callId == null) {
                    Log.e("HomeViewModel", "No active callId")
                    return@launch
                }
                val location = _uiState.value.userLocation ?: _uiState.value.emergencyLocation
                if (location == null) {
                    Log.e("HomeViewModel", "No location available")
                    _uiState.value = _uiState.value.copy(error = "Location not available to select hospital")
                    return@launch
                }
                Log.d("HomeViewModel", "Selecting hospital - callId: $callId, location: ${location.latitude},${location.longitude}")
                _uiState.value = _uiState.value.copy(isSelectingHospital = true)
                
                selectHospitalUseCase(
                    callId = callId,
                    hospitalId = hospitalId,
                    latitude = location.latitude,
                    longitude = location.longitude
                ).getOrThrow()
                
                Log.d("HomeViewModel", "Hospital selection successful")
                val hospital = _uiState.value.availableHospitals.find { it.id == hospitalId }
                if (hospital != null) {
                    Log.d("HomeViewModel", "Found hospital: ${hospital.name} at ${hospital.latitude}, ${hospital.longitude}")
                    _uiState.value = _uiState.value.copy(
                        selectedHospitalName = hospital.name,
                        hospitalLocation = LatLng(hospital.latitude, hospital.longitude),
                        showHospitalSelection = false,
                        callStatus = CallStatus.NAVIGATING_TO_HOSPITAL,
                        activeRoutePolyline = emptyList(),
                        activeRouteDistance = 0,
                        activeRouteDuration = 0
                    )

                    Log.d("HomeViewModel", "════════════════════════════════════════")
                    Log.d("HomeViewModel", "FETCHING HOSPITAL ROUTE NOW...")
                    Log.d("HomeViewModel", "════════════════════════════════════════")
                    loadHospitalRoute(callId)
                    Log.d("HomeViewModel", "Hospital route fetch completed")
                    Log.d("HomeViewModel", "   Final hospitalRoutePolyline size: ${_uiState.value.hospitalRoutePolyline.size} points")
                } else {
                    Log.e("HomeViewModel", "Hospital not found in available list")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to select hospital: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isSelectingHospital = false)
            }
        }
    }

    private suspend fun loadHospitalRoute(callId: String) {
        try {
            Log.d("HomeViewModel", "━━━━━━━━ LOADING HOSPITAL ROUTE ━━━━━━━━")
            Log.d("HomeViewModel", "Loading hospital route for callId: $callId")
            val route = getHospitalRouteUseCase(callId).getOrThrow()
            Log.d("HomeViewModel", "Hospital route received from backend")
            Log.d("HomeViewModel", "   Distance: ${route.distance}m")
            Log.d("HomeViewModel", "   Duration: ${route.duration}s")
            Log.d("HomeViewModel", "   Steps count: ${route.steps.size}")

            if (route.polyline.isNullOrEmpty()) {
                Log.e("HomeViewModel", "BACKEND RETURNED NULL OR EMPTY POLYLINE!")
                Log.e("HomeViewModel", "   This means the backend couldn't generate a route")
                Log.e("HomeViewModel", "   Possible reasons: invalid coordinates, no route available, Google Maps API failure")
                _uiState.value = _uiState.value.copy(
                    error = "No route available to hospital",
                    hospitalRoutePolyline = emptyList()
                )
                return
            }
            
            Log.d("HomeViewModel", "   Polyline length: ${route.polyline.length} characters")
            Log.d("HomeViewModel", "   First 50 chars of polyline: ${route.polyline.take(50)}")
            
            val decodedPoints = decodePolyline(route.polyline)
            Log.d("HomeViewModel", "Decoded ${decodedPoints.size} points from polyline")
            
            if (decodedPoints.isNotEmpty()) {
                Log.d("HomeViewModel", "   First point: ${decodedPoints.first()}")
                Log.d("HomeViewModel", "   Last point: ${decodedPoints.last()}")
            } else {
                Log.e("HomeViewModel", "POLYLINE DECODED TO ZERO POINTS!")
            }
            
            _uiState.value = _uiState.value.copy(
                hospitalRoutePolyline = decodedPoints,
                hospitalRouteDistance = route.distance,
                hospitalRouteDuration = route.duration,
                hospitalRouteSteps = route.steps
            )
            
            Log.d("HomeViewModel", "Hospital route state updated")
            Log.d("HomeViewModel", "   Current hospitalRoutePolyline size in state: ${_uiState.value.hospitalRoutePolyline.size}")
            Log.d("HomeViewModel", "   Current hospitalRouteSteps size in state: ${_uiState.value.hospitalRouteSteps.size}")
            Log.d("HomeViewModel", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        } catch (e: Exception) {
            Log.e("HomeViewModel", "FAILED TO LOAD HOSPITAL ROUTE")
            Log.e("HomeViewModel", "Error type: ${e.javaClass.simpleName}")
            Log.e("HomeViewModel", "Error message: ${e.message}", e)
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(error = "Failed to load hospital route: ${e.message}")
        }
    }

    fun completeCall() {
        val callId = _uiState.value.activeCallId
        val ambulanceId = _uiState.value.assignedAmbulanceId
        if (callId != null && ambulanceId != null) {
            viewModelScope.launch {
                try {
                    Log.d("HomeViewModel", "Completing call $callId and marking ambulance $ambulanceId as available")
                    ambulanceService.markAmbulanceAsAvailable(ambulanceId)
                    DriverSocketManager.completeCall(callId)
                    Log.d("HomeViewModel", "Ambulance $ambulanceId marked as available successfully")
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Failed to mark ambulance as available: ${e.message}", e)
                } finally {
                    _uiState.value = _uiState.value.copy(
                        activeCallId = null,
                        emergencyLocation = null,
                        activeRoutePolyline = emptyList(),
                        hospitalRoutePolyline = emptyList(),
                        selectedHospitalName = null,
                        hospitalLocation = null,
                        callStatus = CallStatus.EN_ROUTE
                    )
                }
            }
        }
    }

    fun unassignAmbulance() {
        viewModelScope.launch {
            try {
                val ambulanceId = _uiState.value.assignedAmbulanceId
                if (ambulanceId != null && _uiState.value.activeCallId == null) {
                    unassignAmbulanceDriverUseCase(ambulanceId).getOrThrow()
                    DriverSocketManager.disconnect()
                    _uiState.value = _uiState.value.copy(
                        assignedAmbulanceId = null,
                        assignedAmbulancePlate = null,
                        isSocketConnected = false
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to unassign ambulance", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    private fun connectUserToWebSocket() {
        val accessToken = AuthSession.accessToken
        if (!accessToken.isNullOrEmpty()) {
            Log.d("HomeViewModel", "Connecting user to WebSocket for live tracking")
            
            UserSocketManager.onCallDispatched = { dispatched ->
                Log.d("HomeViewModel", "========================================")
                Log.d("HomeViewModel", "CALL DISPATCHED EVENT RECEIVED!")
                Log.d("HomeViewModel", "CallID: ${dispatched.callId}")
                Log.d("HomeViewModel", "AmbulanceID: ${dispatched.ambulanceId}")
                Log.d("HomeViewModel", "Location: ${dispatched.ambulanceLatitude}, ${dispatched.ambulanceLongitude}")
                Log.d("HomeViewModel", "Route distance: ${dispatched.distance}m, duration: ${dispatched.duration}s")
                Log.d("HomeViewModel", "Current activeCallId: ${_uiState.value.activeCallId}")
                Log.d("HomeViewModel", "Current userCallStatus: ${_uiState.value.userCallStatus}")
                Log.d("HomeViewModel", "========================================")
                
                _uiState.value = _uiState.value.copy(
                    activeCallId = dispatched.callId,
                    ambulanceLocation = LatLng(dispatched.ambulanceLatitude, dispatched.ambulanceLongitude),
                    activeRoutePolyline = decodePolyline(dispatched.polyline),
                    activeRouteDistance = dispatched.distance,
                    activeRouteDuration = dispatched.duration,
                    userCallStatus = "dispatched"
                )
                
                Log.d("HomeViewModel", "State updated!")
                Log.d("HomeViewModel", "   ambulanceLocation: ${_uiState.value.ambulanceLocation}")
                Log.d("HomeViewModel", "   userCallStatus: ${_uiState.value.userCallStatus}")
                Log.d("HomeViewModel", "   activeRoutePolyline size: ${_uiState.value.activeRoutePolyline.size}")
                Log.d("HomeViewModel", "========================================")
            }
            
            UserSocketManager.onAmbulanceLocation = { update ->
                Log.d("HomeViewModel", "Ambulance location update: ${update.latitude}, ${update.longitude}")
                _uiState.value = _uiState.value.copy(
                    ambulanceLocation = LatLng(update.latitude, update.longitude),
                    activeRoutePolyline = update.polyline?.let { decodePolyline(it) } ?: _uiState.value.activeRoutePolyline,
                    activeRouteDistance = update.distance ?: _uiState.value.activeRouteDistance,
                    activeRouteDuration = update.duration ?: _uiState.value.activeRouteDuration
                )
            }
            
            UserSocketManager.onCallStatus = { statusUpdate ->
                Log.d("HomeViewModel", "========================================")
                Log.d("HomeViewModel", "CALL STATUS EVENT RECEIVED!")
                Log.d("HomeViewModel", "CallID: ${statusUpdate.callId}")
                Log.d("HomeViewModel", "New Status: ${statusUpdate.status}")
                Log.d("HomeViewModel", "Previous userCallStatus: ${_uiState.value.userCallStatus}")
                Log.d("HomeViewModel", "========================================")
                
                val normalizedStatus = statusUpdate.status.lowercase().replace("_", "")
                _uiState.value = _uiState.value.copy(userCallStatus = normalizedStatus)
                Log.d("HomeViewModel", "Updated userCallStatus to: $normalizedStatus")
                
                when (normalizedStatus) {
                    "dispatched", "enroute" -> {
                        Log.d("HomeViewModel", "Status is dispatched/en_route - ambulance is on the way")
                    }
                    "arrived" -> {
                        Log.d("HomeViewModel", "Ambulance ARRIVED - clearing ambulance marker and route")
                        _uiState.value = _uiState.value.copy(
                            ambulanceLocation = null,
                            activeRoutePolyline = emptyList(),
                            activeRouteDistance = 0,
                            activeRouteDuration = 0
                        )
                    }
                    "completed", "cancelled" -> {
                        Log.d("HomeViewModel", "Call COMPLETED/CANCELLED - clearing all call state and redirecting to home")
                        _uiState.value = _uiState.value.copy(
                            activeCallId = null,
                            ambulanceLocation = null,
                            activeRoutePolyline = emptyList(),
                            activeRouteDistance = 0,
                            activeRouteDuration = 0,
                            userCallStatus = null,
                            emergencyLocation = null,
                            hospitalRoutePolyline = emptyList(),
                            selectedHospitalName = null,
                            hospitalLocation = null
                        )
                    }
                }
            }
            
            UserSocketManager.onConnectionChange = { connected ->
                Log.d("HomeViewModel", "========================================")
                Log.d("HomeViewModel", if (connected) "User WebSocket CONNECTED" else "User WebSocket DISCONNECTED")
                Log.d("HomeViewModel", "isSocketConnected: $connected")
                Log.d("HomeViewModel", "========================================")
                _uiState.value = _uiState.value.copy(isSocketConnected = connected)
            }
            
            Log.d("HomeViewModel", "Calling UserSocketManager.connect()...")
            UserSocketManager.connect(accessToken)
            Log.d("HomeViewModel", "UserSocketManager.connect() called - waiting for connection...")
        }
    }

    fun setActiveCallId(callId: String) {
        Log.d("HomeViewModel", "========================================")
        Log.d("HomeViewModel", "setActiveCallId called")
        Log.d("HomeViewModel", "CallID: $callId")
        Log.d("HomeViewModel", "isDriver: ${_uiState.value.isDriver}")
        Log.d("HomeViewModel", "isSocketConnected: ${_uiState.value.isSocketConnected}")
        Log.d("HomeViewModel", "========================================")
        
        _uiState.value = _uiState.value.copy(
            activeCallId = callId,
            userCallStatus = "pending"
        )
        
        Log.d("HomeViewModel", "State updated - userCallStatus set to 'pending'")

        if (!_uiState.value.isDriver && !_uiState.value.isSocketConnected) {
            Log.d("HomeViewModel", "User WebSocket not connected - connecting now for call tracking")
            connectUserToWebSocket()
        } else if (_uiState.value.isSocketConnected) {
            Log.d("HomeViewModel", "WebSocket already connected - ready to receive events")
        }
    }

    fun clearCallState() {
        Log.d("HomeViewModel", "Clearing call state - user manually exited tracking")
        _uiState.value = _uiState.value.copy(
            activeCallId = null,
            emergencyLocation = null,
            ambulanceLocation = null,
            activeRoutePolyline = emptyList(),
            activeRouteDistance = 0,
            activeRouteDuration = 0,
            hospitalRoutePolyline = emptyList(),
            selectedHospitalName = null,
            hospitalLocation = null,
            callStatus = CallStatus.EN_ROUTE,
            userCallStatus = null
        )
    }

    fun hasActiveCall(): Boolean {
        return _uiState.value.activeCallId != null
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.isDriver) {
            DriverSocketManager.disconnect()
        } else {
            UserSocketManager.disconnect()
        }
    }
}
