package com.example.emergencynow.ui.feature.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import org.koin.androidx.compose.koinViewModel
import com.example.emergencynow.ui.util.createAmbulanceMarker
import com.example.emergencynow.ui.util.createUserLocationMarker
import com.example.emergencynow.R

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallTrackingScreen(
    onBackToHome: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(uiState.activeCallId, uiState.userCallStatus, uiState.isSocketConnected) {
        android.util.Log.d("CallTrackingScreen", "=== CALL TRACKING STATE ===")
        android.util.Log.d("CallTrackingScreen", "activeCallId: ${uiState.activeCallId}")
        android.util.Log.d("CallTrackingScreen", "userCallStatus: ${uiState.userCallStatus}")
        android.util.Log.d("CallTrackingScreen", "isSocketConnected: ${uiState.isSocketConnected}")
        android.util.Log.d("CallTrackingScreen", "isDriver: ${uiState.isDriver}")
        android.util.Log.d("CallTrackingScreen", "ambulanceLocation: ${uiState.ambulanceLocation}")
        android.util.Log.d("CallTrackingScreen", "===========================")
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("CallTrackingScreen", "CallTrackingScreen launched")
        android.util.Log.d("CallTrackingScreen", "Ensuring user WebSocket is connected for tracking")
        if (!uiState.isDriver && !uiState.isSocketConnected) {
            android.util.Log.d("CallTrackingScreen", "WebSocket not connected, calling loadUserData()")
            viewModel.loadUserData()
        } else {
            android.util.Log.d("CallTrackingScreen", "WebSocket already connected or user is driver")
        }
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)
                viewModel.updateUserLocation(latLng)

                if (uiState.userLocation == null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    2000L
                ).apply {
                    setMinUpdateIntervalMillis(1000L)
                }.build()
                
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(uiState.activeRoutePolyline, uiState.ambulanceLocation, uiState.userCallStatus) {
        if (uiState.userCallStatus != "pending" && uiState.activeRoutePolyline.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            uiState.activeRoutePolyline.forEach { builder.include(it) }
            uiState.userLocation?.let { builder.include(it) }
            uiState.ambulanceLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } else {
            uiState.userLocation?.let { userLocation ->
                cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 15f)
            }
        }
    }

    LaunchedEffect(uiState.activeCallId, uiState.userCallStatus) {
        android.util.Log.d("CallTrackingScreen", "Status check - activeCallId: ${uiState.activeCallId}, userCallStatus: ${uiState.userCallStatus}")

        if (uiState.activeCallId == null) {
            android.util.Log.d("CallTrackingScreen", "No active call - redirecting to home")
            onBackToHome()
        } else if (uiState.userCallStatus == "arrived" || uiState.userCallStatus == "completed" || uiState.userCallStatus == "cancelled") {
            android.util.Log.d("CallTrackingScreen", "Call status is ${uiState.userCallStatus} - redirecting to home")
            onBackToHome()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Emergency Call Tracking",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.clearCallState()
                            onBackToHome()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                uiState.userLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Your Location",
                        icon = createUserLocationMarker(context, R.drawable.user)
                    )
                }

                if (uiState.userCallStatus != "pending" && uiState.ambulanceLocation != null) {
                    Marker(
                        state = MarkerState(position = uiState.ambulanceLocation!!),
                        title = "Ambulance",
                        icon = createAmbulanceMarker(context, R.drawable.ambulance)
                    )
                }

                if (uiState.userCallStatus != "pending" && uiState.activeRoutePolyline.isNotEmpty()) {
                    Polyline(
                        points = uiState.activeRoutePolyline,
                        color = Color.Blue,
                        width = 10f
                    )
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (uiState.userCallStatus) {
                        "pending" -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Waiting for acceptance...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Your emergency call is being dispatched to the nearest ambulance",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        "dispatched", "en_route" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.LocalHospital,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Ambulance on the way",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            
                            if (uiState.activeRouteDistance > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    "Estimated arrival: ${uiState.activeRouteDuration / 60} min (${uiState.activeRouteDistance}m)",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        else -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
