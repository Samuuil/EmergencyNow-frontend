package com.example.emergencynow.ui.feature.dispatcher

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.usecase.dispatcher.AssignAmbulanceUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetAvailableAmbulancesForDispatcherUseCase
import com.example.emergencynow.domain.usecase.dispatcher.GetDispatcherCallsUseCase
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.DispatcherNotificationHelper
import com.example.emergencynow.ui.util.DispatcherSocketManager
import com.example.emergencynow.ui.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DispatcherUiState(
    val isSocketConnected: Boolean = false,
    val calls: Map<String, DispatcherCallOffer> = emptyMap(),
    val ambulances: List<DispatcherAmbulanceSummary> = emptyList(),
    val pendingAssignments: Set<String> = emptySet(),
    val isAssigning: Boolean = false,
    val lastAssignError: String? = null,
)

class DispatcherViewModel(
    private val authStorage: AuthStorage,
    private val getDispatcherCallsUseCase: GetDispatcherCallsUseCase,
    private val getAvailableAmbulancesUseCase: GetAvailableAmbulancesForDispatcherUseCase,
    private val assignAmbulanceUseCase: AssignAmbulanceUseCase,
    private val dispatcherNotificationHelper: DispatcherNotificationHelper,
    private val notificationManager: NotificationManager,
) : ViewModel() {

    companion object {
        private const val TAG = "DispatcherViewModel"
    }

    private val socket = DispatcherSocketManager()

    private val _uiState = MutableStateFlow(DispatcherUiState())
    val uiState: StateFlow<DispatcherUiState> = _uiState.asStateFlow()

    fun connectSocket() {
        if (socket.isConnected()) return
        val token = authStorage.accessToken ?: run {
            Log.w(TAG, "Cannot connect — missing access token")
            return
        }

        socket.onConnectionChange = { connected ->
            _uiState.value = _uiState.value.copy(isSocketConnected = connected)
            if (connected) refreshFromRest()
        }

        socket.onCallAssigned = { call, ambulances ->
            val current = _uiState.value
            val isNew = !current.calls.containsKey(call.callId)
            _uiState.value = current.copy(
                calls = current.calls + (call.callId to call),
                ambulances = if (ambulances.isNotEmpty()) ambulances else current.ambulances,
            )
            if (isNew) {
                dispatcherNotificationHelper.showCallAssignedNotification(call)
            }
        }

        socket.onCallReleased = { callId, reason ->
            val current = _uiState.value
            _uiState.value = current.copy(
                calls = current.calls - callId,
                pendingAssignments = current.pendingAssignments - callId,
            )
            notificationManager.showError("Call reassigned ($reason)")
        }

        socket.onCallCancelled = { callId ->
            val current = _uiState.value
            _uiState.value = current.copy(
                calls = current.calls - callId,
                pendingAssignments = current.pendingAssignments - callId,
            )
            notificationManager.showError("Call cancelled by user")
        }

        socket.onDriverAccepted = { callId, _ ->
            val current = _uiState.value
            _uiState.value = current.copy(
                calls = current.calls - callId,
                pendingAssignments = current.pendingAssignments - callId,
            )
            notificationManager.showSuccess("Ambulance dispatched")
        }

        socket.onDriverRejected = { callId, _, ambulances ->
            val current = _uiState.value
            _uiState.value = current.copy(
                pendingAssignments = current.pendingAssignments - callId,
                ambulances = if (ambulances.isNotEmpty()) ambulances else current.ambulances,
            )
            notificationManager.showError("Driver declined — pick another ambulance")
        }

        socket.onAmbulanceUnavailable = { callId, _, ambulances ->
            val current = _uiState.value
            _uiState.value = current.copy(
                pendingAssignments = current.pendingAssignments - callId,
                ambulances = if (ambulances.isNotEmpty()) ambulances else current.ambulances,
            )
            notificationManager.showError("Ambulance unavailable — pick another")
        }

        socket.onAmbulanceListUpdated = { ambulances ->
            _uiState.value = _uiState.value.copy(ambulances = ambulances)
        }

        socket.connect(token)
    }

    fun disconnectSocket() {
        socket.disconnect()
        _uiState.value = _uiState.value.copy(isSocketConnected = false)
    }

    fun requestAmbulanceRefresh() {
        socket.requestAmbulanceRefresh()
    }

    fun assignAmbulance(callId: String, ambulanceId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAssigning = true, lastAssignError = null)
            val result = assignAmbulanceUseCase(callId, ambulanceId)
            result.fold(
                onSuccess = {
                    val current = _uiState.value
                    _uiState.value = current.copy(
                        isAssigning = false,
                        pendingAssignments = current.pendingAssignments + callId,
                    )
                    onDone()
                },
                onFailure = { e ->
                    val message = e.message ?: "Failed to assign ambulance"
                    _uiState.value = _uiState.value.copy(
                        isAssigning = false,
                        lastAssignError = message,
                    )
                    notificationManager.showError(message)
                },
            )
        }
    }

    fun callById(callId: String): DispatcherCallOffer? = _uiState.value.calls[callId]

    private fun refreshFromRest() {
        viewModelScope.launch {
            try {
                getDispatcherCallsUseCase().getOrNull()?.let { rest ->
                    val current = _uiState.value
                    val merged = current.calls.toMutableMap()
                    for (offer in rest) merged.putIfAbsent(offer.callId, offer)
                    _uiState.value = current.copy(calls = merged.toMap())
                }
            } catch (e: Exception) {
                Log.e(TAG, "REST calls fallback failed: ${e.message}", e)
            }
            try {
                getAvailableAmbulancesUseCase().getOrNull()?.let { rest ->
                    if (_uiState.value.ambulances.isEmpty()) {
                        _uiState.value = _uiState.value.copy(ambulances = rest)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "REST ambulances fallback failed: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
    }
}
