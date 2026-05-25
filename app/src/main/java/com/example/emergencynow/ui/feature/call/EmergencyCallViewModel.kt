package com.example.emergencynow.ui.feature.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.NotificationManager
import com.example.emergencynow.ui.util.PhoneNumberNormalizer
import com.example.emergencynow.ui.util.parseJwt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PatientIdentification {
    SELF,
    IDENTIFIED,
    NOT_IDENTIFIED,
}

data class EmergencyCallUiState(
    val isLoading: Boolean = false,
    val callCreated: Boolean = false,
    val callId: String? = null,
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val selectedContactName: String? = null,
    val selectedContactPhoneNumber: String? = null,
    val patientIdentification: PatientIdentification? = null,
)

class EmergencyCallViewModel(
    private val createCallUseCase: CreateCallUseCase,
    private val authStorage: AuthStorage,
    private val notificationManager: NotificationManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyCallUiState())
    val uiState: StateFlow<EmergencyCallUiState> = _uiState.asStateFlow()

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(latitude = latitude, longitude = longitude)
    }

    fun setSelectedContact(name: String, phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            selectedContactName = name,
            selectedContactPhoneNumber = phoneNumber,
        )
    }

    fun clearSelectedContact() {
        _uiState.value = _uiState.value.copy(
            selectedContactName = null,
            selectedContactPhoneNumber = null,
        )
    }

    fun createCall() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.latitude == null || state.longitude == null) {
                notificationManager.showError("Location not available")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true)
            try {
                val normalizedPhone = state.selectedContactPhoneNumber
                    ?.let { PhoneNumberNormalizer.toE164(it) }
                val request = CreateCallRequest(
                    description = state.description.ifEmpty { "Emergency" },
                    latitude = state.latitude,
                    longitude = state.longitude,
                    patientPhoneNumber = normalizedPhone,
                )
                var userId = AuthSession.userId
                if (userId.isNullOrEmpty()) {
                    userId = authStorage.accessToken?.let { parseJwt(it)?.sub }
                    if (userId != null) AuthSession.userId = userId
                }
                val result = createCallUseCase(request, userId ?: "")
                result.fold(
                    onSuccess = { call ->
                        val identification = when {
                            normalizedPhone == null -> PatientIdentification.SELF
                            call.patientEgn != null -> PatientIdentification.IDENTIFIED
                            else -> PatientIdentification.NOT_IDENTIFIED
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            callCreated = true,
                            callId = call.id,
                            patientIdentification = identification,
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        notificationManager.showError(error.message ?: "Failed to create call")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                notificationManager.showError(e.message ?: "Unknown error")
            }
        }
    }
}
