package com.example.emergencynow.ui.feature.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.error.EmergencyError
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.usecase.profile.GetProfileByEgnUseCase
import com.example.emergencynow.ui.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PatientProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notInRecords: Boolean = false,
    val profile: Profile? = null
)

class PatientProfileViewModel(
    private val getProfileByEgnUseCase: GetProfileByEgnUseCase,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState: StateFlow<PatientProfileUiState> = _uiState.asStateFlow()

    fun loadPatientProfile(egn: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, notInRecords = false)
            try {
                val result = getProfileByEgnUseCase(egn)
                result.fold(
                    onSuccess = { profile ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            profile = profile
                        )
                    },
                    onFailure = { error ->
                        val notInRecords = error is EmergencyError.Generic &&
                            (error.isNotFound() || error.isErrorCode("USER_NOT_FOUND") || error.isErrorCode("PROFILE_NOT_FOUND"))
                        val message = error.message ?: "Failed to load patient profile"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = message,
                            notInRecords = notInRecords
                        )
                        if (!notInRecords) {
                            notificationManager.showError(message)
                        }
                    }
                )
            } catch (e: Exception) {
                val message = e.message ?: "An unexpected error occurred"
                _uiState.value = _uiState.value.copy(isLoading = false, error = message)
                notificationManager.showError(message)
            }
        }
    }
}
