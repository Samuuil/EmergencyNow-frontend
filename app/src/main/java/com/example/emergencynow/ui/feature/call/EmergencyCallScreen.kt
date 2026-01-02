package com.example.emergencynow.ui.feature.call

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.usecase.call.CreateCallUseCase
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.shadow
import com.example.emergencynow.ui.components.decorations.WelcomeScreenBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.theme.EmergencyRed
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
    private val createCallUseCase: CreateCallUseCase,
    private val userRepository: com.example.emergencynow.domain.repository.UserRepository
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
                val egnResult = userRepository.getMyEgn()
                val userEgn = egnResult.getOrElse { error ->
                    android.util.Log.e("EmergencyCallViewModel", "Failed to fetch user EGN: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch user information. Please try again."
                    )
                    return@launch
                }
                
                val request = CreateCallRequest(
                    description = state.description.ifEmpty { "Emergency" },
                    latitude = state.latitude,
                    longitude = state.longitude,
                    userEgn = userEgn
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

    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeScreenBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Emergency Call",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Emergency Assistance",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandBlueDark,
                    lineHeight = 40.sp
                )
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = "Describe your emergency (optional):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandBlueDark.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = BrandBlueDark.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(CurvePaleBlue)
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = BrandBlueDark,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(BrandBlueDark),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { innerTextField ->
                            if (uiState.description.isEmpty()) {
                                Text(
                                    "Medical emergency, accident, etc.",
                                    fontSize = 16.sp,
                                    color = BrandBlueDark.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                if (uiState.latitude != null && uiState.longitude != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CurvePaleBlue
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlueDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Location acquired",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Text(
                                    text = "Lat: %.6f, Lon: %.6f".format(
                                        uiState.latitude,
                                        uiState.longitude
                                    ),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = BrandBlueDark.copy(alpha = 0.8f)
                                )
                            }
                            
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Located",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                if (uiState.error != null) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { viewModel.createCall() },
                    enabled = !uiState.isLoading && uiState.latitude != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Call Emergency Services",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
