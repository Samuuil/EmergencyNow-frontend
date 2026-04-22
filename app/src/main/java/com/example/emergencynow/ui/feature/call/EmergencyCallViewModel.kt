package com.example.emergencynow.ui.feature.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmergencyCallUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val callCreated: Boolean = false,
    val callId: String? = null,
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

class EmergencyCallViewModel(
    private val createCallUseCase: CreateCallUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyCallUiState())
    val uiState: StateFlow<EmergencyCallUiState> = _uiState.asStateFlow()

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(latitude = latitude, longitude = longitude)
    }

    fun createCall() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.latitude == null || state.longitude == null) {
                _uiState.value = state.copy(error = "Location not available")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val request = CreateCallRequest(
                    description = state.description.ifEmpty { "Emergency" },
                    latitude = state.latitude,
                    longitude = state.longitude
                )
                val result = createCallUseCase(request, AuthSession.userId ?: "")
                result.fold(
                    onSuccess = { call ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            callCreated = true,
                            callId = call.id
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create call"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
