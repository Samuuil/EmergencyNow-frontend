package com.example.emergencynow.ui.feature.call

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.data.model.request.CreateCallRequest
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
                val result = createCallUseCase(request)
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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCallScreen(
    onBack: () -> Unit,
    onCallCreated: (String) -> Unit,
    viewModel: EmergencyCallViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.callCreated) {
        if (uiState.callCreated && uiState.callId != null) {
            onCallCreated(uiState.callId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Call") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Emergency Assistance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Describe your emergency (optional):",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Medical emergency, accident, etc.") },
                minLines = 3,
                maxLines = 5
            )

            if (uiState.latitude != null && uiState.longitude != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Location acquired",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Lat: %.6f, Lon: %.6f".format(uiState.latitude, uiState.longitude),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.createCall() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.latitude != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Call Emergency Services", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
