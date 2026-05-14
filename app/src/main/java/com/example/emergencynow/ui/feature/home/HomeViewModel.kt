package com.example.emergencynow.ui.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.repository.LocationRepository
import com.example.emergencynow.domain.usecase.user.GetUserRoleUseCase
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.parseJwt
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userLocation: LatLng? = null,
    val isDriver: Boolean = false,
    val isDoctor: Boolean = false,
    val isDispatcher: Boolean = false,
)

class HomeViewModel(
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val authStorage: AuthStorage,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null

    fun startLocationUpdates() {
        if (locationJob?.isActive == true) return
        locationJob = viewModelScope.launch {
            locationRepository.locationUpdates().collect { latLng ->
                _uiState.update { it.copy(userLocation = latLng) }
            }
        }
    }

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val accessToken = authStorage.accessToken
                if (accessToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "Missing authentication credentials",
                        isLoading = false
                    )
                    return@launch
                }

                // Parse JWT for userId and role
                val payload = parseJwt(accessToken)
                val userId = payload?.sub
                if (userId != null) AuthSession.userId = userId
                val jwtRole = payload?.role

                Log.d("HomeViewModel", "JWT parsed: userId=$userId, role=$jwtRole")

                // Try API call, fall back to JWT role
                val role = if (!userId.isNullOrEmpty()) {
                    try {
                        getUserRoleUseCase(userId).getOrThrow()
                    } catch (e: Exception) {
                        Log.w("HomeViewModel", "API role fetch failed, using JWT role: ${e.message}")
                        jwtRole
                    }
                } else {
                    jwtRole
                }

                Log.d("HomeViewModel", "Final role: $role, isDriver=${role == "DRIVER"}")

                _uiState.value = _uiState.value.copy(
                    isDriver = role == "DRIVER",
                    isDoctor = role == "DOCTOR",
                    isDispatcher = role == "DISPATCHER",
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load user data", e)
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun updateUserLocation(location: LatLng) {
        _uiState.value = _uiState.value.copy(userLocation = location)
    }
}
