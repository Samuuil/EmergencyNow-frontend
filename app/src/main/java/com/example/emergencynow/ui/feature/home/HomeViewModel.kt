package com.example.emergencynow.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.repository.LocationRepository
import com.example.emergencynow.ui.util.AuthSession
import com.example.emergencynow.ui.util.AuthStorage
import com.example.emergencynow.ui.util.NotificationManager
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
    val userLocation: LatLng? = null,
    val isDriver: Boolean = false,
    val isDoctor: Boolean = false,
    val isDispatcher: Boolean = false,
)

class HomeViewModel(
    private val authStorage: AuthStorage,
    private val locationRepository: LocationRepository,
    private val notificationManager: NotificationManager,
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            val accessToken = authStorage.accessToken
            if (accessToken.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                notificationManager.showError("Missing authentication credentials")
                return@launch
            }

            val payload = parseJwt(accessToken)
            val userId = payload?.sub
            if (userId != null) AuthSession.userId = userId
            val role = payload?.role

            _uiState.value = _uiState.value.copy(
                isDriver = role == "DRIVER",
                isDoctor = role == "DOCTOR",
                isDispatcher = role == "DISPATCHER",
                isLoading = false,
            )
        }
    }

    fun updateUserLocation(location: LatLng) {
        _uiState.value = _uiState.value.copy(userLocation = location)
    }
}
