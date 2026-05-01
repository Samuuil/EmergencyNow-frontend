package com.example.emergencynow.ui.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.PolylineDecoder
import com.example.emergencynow.ui.util.UserSocketManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CallTrackingUiState(
    val activeCallId: String? = null,
    val ambulanceLocation: LatLng? = null,
    val activeRoutePolyline: List<LatLng> = emptyList(),
    val activeRouteDistance: Int = 0,
    val activeRouteDuration: Int = 0,
    val activeRouteSteps: List<String> = emptyList(),
    val userCallStatus: CallStatus? = null,
    val isSocketConnected: Boolean = false,
)

class CallTrackingViewModel(
    private val authStorage: AuthStorage,
) : ViewModel() {

    private val userSocket = UserSocketManager()

    private val _uiState = MutableStateFlow(CallTrackingUiState())
    val uiState: StateFlow<CallTrackingUiState> = _uiState.asStateFlow()

    fun setActiveCallId(callId: String) {
        _uiState.value = _uiState.value.copy(activeCallId = callId, userCallStatus = CallStatus.PENDING)
        if (!_uiState.value.isSocketConnected) connectSocket()
    }

    fun connectSocket() {
        if (_uiState.value.isSocketConnected) return
        val accessToken = authStorage.accessToken ?: return
        Log.d("CallTrackingViewModel", "Connecting user WebSocket")

        userSocket.onCallDispatched = { dispatched ->
            _uiState.value = _uiState.value.copy(
                activeCallId = dispatched.callId,
                ambulanceLocation = LatLng(dispatched.ambulanceLatitude, dispatched.ambulanceLongitude),
                activeRoutePolyline = PolylineDecoder.decode(dispatched.polyline),
                activeRouteDistance = dispatched.distance,
                activeRouteDuration = dispatched.duration,
                activeRouteSteps = dispatched.steps,
                userCallStatus = CallStatus.DISPATCHED
            )
        }

        userSocket.onAmbulanceLocation = { update ->
            _uiState.value = _uiState.value.copy(
                ambulanceLocation = LatLng(update.latitude, update.longitude),
                activeRoutePolyline = update.polyline?.let { PolylineDecoder.decode(it) }
                    ?: _uiState.value.activeRoutePolyline,
                activeRouteDistance = update.distance ?: _uiState.value.activeRouteDistance,
                activeRouteDuration = update.duration ?: _uiState.value.activeRouteDuration,
                activeRouteSteps = if (update.steps.isNotEmpty()) update.steps else _uiState.value.activeRouteSteps
            )
        }

        userSocket.onCallStatus = { statusUpdate ->
            val status = CallStatus.fromWire(statusUpdate.status)
            _uiState.value = _uiState.value.copy(userCallStatus = status)
            when (status) {
                CallStatus.ARRIVED -> _uiState.value = _uiState.value.copy(
                    ambulanceLocation = null,
                    activeRoutePolyline = emptyList(),
                    activeRouteDistance = 0,
                    activeRouteDuration = 0
                )
                CallStatus.COMPLETED, CallStatus.CANCELLED -> _uiState.value = _uiState.value.copy(
                    activeCallId = null,
                    ambulanceLocation = null,
                    activeRoutePolyline = emptyList(),
                    activeRouteDistance = 0,
                    activeRouteDuration = 0,
                    userCallStatus = null
                )
                else -> {}
            }
        }

        userSocket.onConnectionChange = { connected ->
            _uiState.value = _uiState.value.copy(isSocketConnected = connected)
        }

        userSocket.connect(accessToken)
    }

    fun clearCallState() {
        _uiState.value = _uiState.value.copy(
            activeCallId = null,
            ambulanceLocation = null,
            activeRoutePolyline = emptyList(),
            activeRouteDistance = 0,
            activeRouteDuration = 0,
            userCallStatus = null
        )
    }

    fun hasActiveCall(): Boolean = _uiState.value.activeCallId != null

    fun disconnectSocket() {
        userSocket.disconnect()
        _uiState.value = _uiState.value.copy(isSocketConnected = false)
    }

    override fun onCleared() {
        super.onCleared()
        userSocket.disconnect()
    }
}
