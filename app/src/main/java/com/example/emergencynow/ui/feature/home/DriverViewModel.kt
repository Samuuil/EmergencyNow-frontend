package com.example.emergencynow.ui.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.usecase.ambulance.GetAmbulanceByDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.MarkAmbulanceAvailableUseCase
import com.example.emergencynow.domain.usecase.ambulance.UnassignAmbulanceDriverUseCase
import com.example.emergencynow.domain.usecase.call.GetCallByIdUseCase
import com.example.emergencynow.domain.usecase.call.UpdateCallStatusUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalRouteUseCase
import com.example.emergencynow.domain.usecase.hospital.GetHospitalsForCallUseCase
import com.example.emergencynow.domain.usecase.hospital.SelectHospitalUseCase
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.CallOffer
import com.example.emergencynow.ui.util.DriverNotificationHelper
import com.example.emergencynow.ui.util.NetworkConfig
import com.example.emergencynow.ui.util.PolylineDecoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverUiState(
    val assignedAmbulanceId: String? = null,
    val assignedAmbulancePlate: String? = null,
    val isSocketConnected: Boolean = false,
    val incomingCallOffer: CallOffer? = null,
    val activeCallId: String? = null,
    val patientEgn: String? = null,
    val emergencyLocation: LatLng? = null,
    val driverLocation: LatLng? = null,
    val activeRoutePolyline: List<LatLng> = emptyList(),
    val activeRouteDistance: Int = 0,
    val activeRouteDuration: Int = 0,
    val activeRouteSteps: List<String> = emptyList(),
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
    val error: String? = null,
)

class DriverViewModel(
    private val getAmbulanceByDriverUseCase: GetAmbulanceByDriverUseCase,
    private val unassignAmbulanceDriverUseCase: UnassignAmbulanceDriverUseCase,
    private val updateCallStatusUseCase: UpdateCallStatusUseCase,
    private val getHospitalsForCallUseCase: GetHospitalsForCallUseCase,
    private val selectHospitalUseCase: SelectHospitalUseCase,
    private val getHospitalRouteUseCase: GetHospitalRouteUseCase,
    private val getCallByIdUseCase: GetCallByIdUseCase,
    private val markAmbulanceAvailableUseCase: MarkAmbulanceAvailableUseCase,
    private val driverNotificationHelper: DriverNotificationHelper,
    private val authStorage: AuthStorage,
) : ViewModel() {

    private val driverSocket = DriverSocketManager()

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun loadData(userId: String) {
        viewModelScope.launch { loadAmbulanceData(userId) }
    }

    fun refresh() {
        val userId = com.example.emergencynow.ui.util.AuthSession.userId ?: return
        viewModelScope.launch { loadAmbulanceData(userId) }
    }

    private suspend fun loadAmbulanceData(userId: String) {
        try {
            val ambulance = getAmbulanceByDriverUseCase(userId).getOrNull()
            _uiState.value = _uiState.value.copy(
                assignedAmbulanceId = ambulance?.id,
                assignedAmbulancePlate = ambulance?.licensePlate,
            )
            ambulance?.id?.let { connectToWebSocket(it) }
        } catch (e: Exception) {
            Log.e("DriverViewModel", "Failed to load ambulance data", e)
        }
    }

    fun updateDriverLocation(location: LatLng) {
        _uiState.value = _uiState.value.copy(driverLocation = location)
    }

    fun sendLocationUpdate(callId: String, latitude: Double, longitude: Double) {
        driverSocket.sendLocationUpdate(callId, latitude, longitude)
    }

    fun retryConnection() {
        Log.d("DriverViewModel", "Retry connection requested")
        val accessToken = authStorage.accessToken
        val ambulanceId = _uiState.value.assignedAmbulanceId
        if (!accessToken.isNullOrEmpty() && ambulanceId != null) {
            driverSocket.disconnect()
            viewModelScope.launch {
                delay(500)
                connectToWebSocket(ambulanceId)
            }
        } else {
            val errorMsg = when {
                accessToken.isNullOrEmpty() -> "Cannot connect: Access token is missing. Please log out and log in again."
                ambulanceId == null -> "Cannot connect: No ambulance assigned. Please select an ambulance first."
                else -> "Cannot connect: Unknown error"
            }
            _uiState.value = _uiState.value.copy(error = errorMsg)
        }
    }

    private fun connectToWebSocket(ambulanceId: String) {
        val accessToken = authStorage.accessToken ?: return
        Log.d("DriverViewModel", "Connecting driver socket - ambulanceId: $ambulanceId, base: ${NetworkConfig.currentBase()}")

        driverSocket.onCallOffer = { offer ->
            Log.d("DriverViewModel", "Call offer received: ${offer.callId}")
            driverNotificationHelper.showCallOfferNotification(offer)
            _uiState.value = _uiState.value.copy(
                incomingCallOffer = offer,
                emergencyLocation = LatLng(offer.latitude, offer.longitude)
            )
        }

        driverSocket.onConnectionChange = { connected ->
            Log.d("DriverViewModel", "Socket connection changed: $connected")
            _uiState.value = _uiState.value.copy(
                isSocketConnected = connected,
                error = if (!connected) "Connection failed. Check logs for details." else null
            )
        }

        driverSocket.onCallRoute = { route ->
            Log.d("DriverViewModel", "Call route received: ${route.callId}")
            _uiState.value = _uiState.value.copy(
                activeCallId = route.callId,
                activeRoutePolyline = PolylineDecoder.decode(route.polyline),
                activeRouteDistance = route.distance,
                activeRouteDuration = route.duration,
                activeRouteSteps = route.steps
            )
            fetchPatientEgn(route.callId)
        }

        driverSocket.onRouteUpdate = { route ->
            _uiState.value = _uiState.value.copy(
                activeRoutePolyline = PolylineDecoder.decode(route.polyline),
                activeRouteDistance = route.distance,
                activeRouteDuration = route.duration,
                activeRouteSteps = route.steps
            )
        }

        if (driverSocket.isConnected()) driverSocket.disconnect()

        driverSocket.connect(accessToken)

        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (_uiState.value.assignedAmbulanceId != null) {
                    val actual = driverSocket.isConnected()
                    if (actual != _uiState.value.isSocketConnected) {
                        _uiState.value = _uiState.value.copy(isSocketConnected = actual)
                    }
                }
            }
        }
    }

    fun acceptCall(callId: String) {
        driverSocket.acceptCall(callId)
        _uiState.value = _uiState.value.copy(incomingCallOffer = null, activeCallId = callId)
        fetchPatientEgn(callId)
    }

    fun declineCall(callId: String) {
        driverSocket.declineCall(callId)
        _uiState.value = _uiState.value.copy(incomingCallOffer = null)
    }

    private fun fetchPatientEgn(callId: String) {
        viewModelScope.launch {
            try {
                getCallByIdUseCase(callId).fold(
                    onSuccess = { callResponse ->
                        _uiState.value = _uiState.value.copy(patientEgn = callResponse.userEgn)
                    },
                    onFailure = { error ->
                        Log.e("DriverViewModel", "Failed to fetch call details: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching patient EGN", e)
            }
        }
    }

    fun updateCallStatus(status: CallStatus) {
        viewModelScope.launch {
            try {
                val callId = _uiState.value.activeCallId ?: return@launch
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
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to update call status", e)
            }
        }
    }

    private suspend fun loadHospitals(callId: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoadingHospitals = true, showHospitalSelection = true)
            val location = _uiState.value.emergencyLocation ?: _uiState.value.driverLocation
            if (location == null) {
                _uiState.value = _uiState.value.copy(
                    error = "Location not available for hospital suggestions",
                    isLoadingHospitals = false
                )
                return
            }
            val hospitals = getHospitalsForCallUseCase(
                callId = callId,
                latitude = location.latitude,
                longitude = location.longitude
            ).getOrThrow()
            _uiState.value = _uiState.value.copy(availableHospitals = hospitals, isLoadingHospitals = false)
        } catch (e: Exception) {
            Log.e("DriverViewModel", "Failed to load hospitals", e)
            _uiState.value = _uiState.value.copy(
                isLoadingHospitals = false,
                error = "Failed to load hospitals: ${e.message}"
            )
        }
    }

    fun selectHospital(hospitalId: String) {
        viewModelScope.launch {
            try {
                val callId = _uiState.value.activeCallId ?: return@launch
                val location = _uiState.value.driverLocation ?: _uiState.value.emergencyLocation
                if (location == null) {
                    _uiState.value = _uiState.value.copy(error = "Location not available to select hospital")
                    return@launch
                }
                _uiState.value = _uiState.value.copy(isSelectingHospital = true)
                selectHospitalUseCase(
                    callId = callId,
                    hospitalId = hospitalId,
                    latitude = location.latitude,
                    longitude = location.longitude
                ).getOrThrow()

                val hospital = _uiState.value.availableHospitals.find { it.id == hospitalId }
                if (hospital != null) {
                    _uiState.value = _uiState.value.copy(
                        selectedHospitalName = hospital.name,
                        hospitalLocation = LatLng(hospital.latitude, hospital.longitude),
                        showHospitalSelection = false,
                        callStatus = CallStatus.NAVIGATING_TO_HOSPITAL,
                        activeRoutePolyline = emptyList(),
                        activeRouteDistance = 0,
                        activeRouteDuration = 0
                    )
                    loadHospitalRoute(callId)
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to select hospital", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isSelectingHospital = false)
            }
        }
    }

    private suspend fun loadHospitalRoute(callId: String) {
        try {
            val route = getHospitalRouteUseCase(callId).getOrThrow()
            if (route.polyline.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "No route available to hospital",
                    hospitalRoutePolyline = emptyList()
                )
                return
            }
            val decoded = PolylineDecoder.decode(route.polyline)
            _uiState.value = _uiState.value.copy(
                hospitalRoutePolyline = decoded,
                hospitalRouteDistance = route.distance,
                hospitalRouteDuration = route.duration,
                hospitalRouteSteps = route.steps
            )
        } catch (e: Exception) {
            Log.e("DriverViewModel", "Failed to load hospital route", e)
            _uiState.value = _uiState.value.copy(error = "Failed to load hospital route: ${e.message}")
        }
    }

    fun completeCall() {
        val callId = _uiState.value.activeCallId ?: return
        val ambulanceId = _uiState.value.assignedAmbulanceId ?: return
        viewModelScope.launch {
            try {
                markAmbulanceAvailableUseCase(ambulanceId)
                driverSocket.completeCall(callId)
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to complete call", e)
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

    fun unassignAmbulance() {
        viewModelScope.launch {
            try {
                val ambulanceId = _uiState.value.assignedAmbulanceId ?: return@launch
                if (_uiState.value.activeCallId != null) return@launch
                unassignAmbulanceDriverUseCase(ambulanceId).getOrThrow()
                driverSocket.disconnect()
                _uiState.value = _uiState.value.copy(
                    assignedAmbulanceId = null,
                    assignedAmbulancePlate = null,
                    isSocketConnected = false
                )
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to unassign ambulance", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        driverSocket.disconnect()
    }
}
