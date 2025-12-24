package com.example.emergencynow.ui.feature.ambulance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.model.response.AmbulanceDto
import com.example.emergencynow.domain.usecase.ambulance.AssignAmbulanceDriverUseCase
import com.example.emergencynow.domain.usecase.ambulance.GetAvailableAmbulancesUseCase
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AmbulanceSelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableAmbulances: List<AmbulanceDto> = emptyList(),
    val selectedAmbulanceId: String? = null,
    val isAssigning: Boolean = false
)

class AmbulanceSelectionViewModel(
    private val getAvailableAmbulancesUseCase: GetAvailableAmbulancesUseCase,
    private val assignAmbulanceDriverUseCase: AssignAmbulanceDriverUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmbulanceSelectionUiState())
    val uiState: StateFlow<AmbulanceSelectionUiState> = _uiState.asStateFlow()

    init {
        loadAvailableAmbulances()
    }

    fun loadAvailableAmbulances() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = getAvailableAmbulancesUseCase()
                result.fold(
                    onSuccess = { ambulances ->
                        _uiState.value = _uiState.value.copy(
                            availableAmbulances = ambulances,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        Log.e("AmbulanceSelection", "Failed to load ambulances", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to load ambulances",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("AmbulanceSelection", "Error loading ambulances", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An unexpected error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun selectAmbulance(ambulanceId: String) {
        _uiState.value = _uiState.value.copy(selectedAmbulanceId = ambulanceId)
    }

    fun assignAmbulance(onSuccess: () -> Unit) {
        val selectedId = _uiState.value.selectedAmbulanceId ?: return
        val userId = AuthSession.userId ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAssigning = true, error = null)
                
                val result = assignAmbulanceDriverUseCase(userId, selectedId)
                result.fold(
                    onSuccess = {
                        Log.d("AmbulanceSelection", "Successfully assigned ambulance $selectedId to driver $userId")
                        onSuccess()
                    },
                    onFailure = { exception ->
                        Log.e("AmbulanceSelection", "Failed to assign ambulance", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to assign ambulance",
                            isAssigning = false
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("AmbulanceSelection", "Error assigning ambulance", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An unexpected error occurred",
                    isAssigning = false
                )
            }
        }
    }

    fun retry() {
        loadAvailableAmbulances()
    }
}
