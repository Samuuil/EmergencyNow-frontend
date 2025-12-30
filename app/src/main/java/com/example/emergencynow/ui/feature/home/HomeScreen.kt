package com.example.emergencynow.ui.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.get
import com.example.emergencynow.domain.usecase.profile.GetProfileByEgnUseCase
import com.example.emergencynow.domain.model.entity.Profile

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMakeEmergencyCall: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectAmbulance: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onPatientLookup: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var showPatientProfile by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshData()
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

    LaunchedEffect(uiState.hospitalRoutePolyline) {
        val points = uiState.hospitalRoutePolyline
        if (points.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            points.forEach { builder.include(it) }
            uiState.userLocation?.let { builder.include(it) }
            uiState.hospitalLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    LaunchedEffect(uiState.activeRoutePolyline, uiState.ambulanceLocation) {
        if (!uiState.isDriver && uiState.activeRoutePolyline.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            uiState.activeRoutePolyline.forEach { builder.include(it) }
            uiState.userLocation?.let { builder.include(it) }
            uiState.ambulanceLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    LaunchedEffect(uiState.activeCallId, uiState.userLocation) {
        if (uiState.isDriver && uiState.activeCallId != null && uiState.userLocation != null) {
            while (uiState.activeCallId != null) {
                val location = uiState.userLocation
                val callId = uiState.activeCallId
                if (location != null && callId != null) {
                    com.example.emergencynow.ui.util.DriverSocketManager.sendLocationUpdate(
                        callId = callId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
                delay(2000)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            uiState.userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = if (uiState.isDriver) "Your Ambulance" else "Your Location",
                    icon = if (uiState.isDriver) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    }
                )
            }

            if (uiState.isDriver && uiState.emergencyLocation != null) {
                Marker(
                    state = MarkerState(position = uiState.emergencyLocation!!),
                    title = "Emergency",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            // Only show ambulance marker for users when call is dispatched/en_route (not pending)
            if (!uiState.isDriver && uiState.ambulanceLocation != null && uiState.userCallStatus != "pending") {
                Marker(
                    state = MarkerState(position = uiState.ambulanceLocation!!),
                    title = "Ambulance",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }

            if (uiState.hospitalLocation != null) {
                Marker(
                    state = MarkerState(position = uiState.hospitalLocation!!),
                    title = uiState.selectedHospitalName ?: "Hospital",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            if (uiState.activeRoutePolyline.isNotEmpty() && uiState.callStatus != CallStatus.NAVIGATING_TO_HOSPITAL) {
                Polyline(
                    points = uiState.activeRoutePolyline,
                    color = Color.Blue,
                    width = 10f
                )
            }

            if (uiState.hospitalRoutePolyline.isNotEmpty()) {
                Polyline(
                    points = uiState.hospitalRoutePolyline,
                    color = Color.Green,
                    width = 10f
                )
            }
        }

        if (uiState.isDriver) {
            if (uiState.activeCallId != null) {
                    // On-call: show primary instruction + ETA
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    StepIcon(uiState.activeRouteSteps.firstOrNull())
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        uiState.activeRouteSteps.firstOrNull() ?: "Drive to destination",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        maxLines = 2,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Patient profile icon button
                                    IconButton(
                                        onClick = { 
                                            Log.d("HomeScreen", "🔍 Profile icon clicked!")
                                            Log.d("HomeScreen", "   patientEgn: ${uiState.patientEgn}")
                                            if (uiState.patientEgn != null) {
                                                showPatientProfile = true
                                            } else {
                                                Log.w("HomeScreen", "⚠️ Patient EGN is not available yet")
                                            }
                                        },
                                        modifier = Modifier.size(48.dp),
                                        enabled = uiState.patientEgn != null
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "View Patient Profile",
                                            tint = if (uiState.patientEgn != null) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                Color.Gray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = if (expanded) "Collapse" else "Expand"
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            if (uiState.activeRouteDuration > 0 || uiState.activeRouteDistance > 0) {
                                Text(
                                    "ETA: ${uiState.activeRouteDuration / 60} min • ${uiState.activeRouteDistance} m",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (expanded && uiState.activeRouteSteps.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "All Steps (${uiState.activeRouteSteps.size} total):",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    uiState.activeRouteSteps.forEachIndexed { index, step ->
                                        StepItem(index + 1, step)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Not on call: show availability status with new design
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .padding(top = 32.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3E8FF).copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.isSocketConnected) "Available" else "Connecting...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isSocketConnected) Color(0xFF16A34A) else Color.Gray
                                    )
                                    if (!uiState.isSocketConnected) {
                                        Spacer(Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { viewModel.retryConnection() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Refresh,
                                                contentDescription = "Retry connection",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } else if (uiState.activeCallId != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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
            }
        }

        if (uiState.isDriver && uiState.activeCallId != null) {
            when (uiState.callStatus) {
                CallStatus.EN_ROUTE -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 190.dp)
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.updateCallStatus(CallStatus.ARRIVED) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mark as Arrived",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                CallStatus.NAVIGATING_TO_HOSPITAL -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 190.dp)
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.completeCall() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Complete Call",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {}
            }
        }

        // Removed separate bottom steps panel; steps now integrated in top card for drivers

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Driver-specific controls (ambulance selection)
            if (uiState.isDriver) {
                if (uiState.assignedAmbulanceId == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = onSelectAmbulance
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Select Ambulance",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE0F2FE).copy(alpha = 0.95f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "SELECTED AMBULANCE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = uiState.assignedAmbulancePlate ?: "Unknown",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                            if (uiState.activeCallId == null) {
                                OutlinedButton(
                                    onClick = { viewModel.unassignAmbulance() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Unassign", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            
            // Doctor-specific controls (patient lookup)
            if (uiState.isDoctor) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    onClick = onPatientLookup
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Patient Lookup",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Emergency call button (visible for users and for drivers when not on a call)
            if (!uiState.isDriver || (uiState.isDriver && uiState.activeCallId == null)) {
                Button(
                    onClick = onMakeEmergencyCall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Emergency Call",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Navigation Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BottomNavItem(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        onClick = { /* Already on home */ },
                        isSelected = true
                    )
                    BottomNavItem(
                        icon = Icons.Filled.History,
                        label = "History",
                        onClick = onNavigateToHistory,
                        isSelected = false
                    )
                    BottomNavItem(
                        icon = Icons.Filled.Contacts,
                        label = "Contacts",
                        onClick = onNavigateToContacts,
                        isSelected = false
                    )
                    BottomNavItem(
                        icon = Icons.Filled.Person,
                        label = "Profile",
                        onClick = onOpenProfile,
                        isSelected = false
                    )
                }
            }
        }
    }

    if (uiState.incomingCallOffer != null) {
        IncomingCallDialog(
            offer = uiState.incomingCallOffer!!,
            onAccept = { viewModel.acceptCall(uiState.incomingCallOffer!!.callId) },
            onDecline = { viewModel.declineCall(uiState.incomingCallOffer!!.callId) }
        )
    }

    if (uiState.showHospitalSelection) {
        HospitalSelectionDialog(
            hospitals = uiState.availableHospitals,
            isLoading = uiState.isLoadingHospitals || uiState.isSelectingHospital,
            onHospitalSelected = { hospitalId ->
                viewModel.selectHospital(hospitalId)
            },
            onDismiss = { /* Cannot dismiss - must select hospital */ }
        )
    }
    
    // Patient profile dialog
    uiState.patientEgn?.let { egn ->
        if (showPatientProfile) {
            Log.d("HomeScreen", "📋 Showing patient profile dialog for EGN: $egn")
            PatientProfileDialog(
                egn = egn,
                onDismiss = { 
                    Log.d("HomeScreen", "❌ Closing patient profile dialog")
                    showPatientProfile = false 
                }
            )
        }
    }
}

@Composable
private fun IncomingCallDialog(
    offer: com.example.emergencynow.ui.util.CallOffer,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Cannot dismiss */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Incoming Emergency Call",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Emergency Call",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Decline")
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(index: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepIcon(text)
        Spacer(Modifier.width(8.dp))
        Text("$index. $text", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StepIcon(text: String?) {
    val icon = when {
        text == null -> Icons.AutoMirrored.Filled.ArrowForward
        text.contains("left", ignoreCase = true) -> Icons.AutoMirrored.Filled.ArrowBack
        text.contains("right", ignoreCase = true) -> Icons.AutoMirrored.Filled.ArrowForward
        text.contains("u-turn", ignoreCase = true) || text.contains("uturn", ignoreCase = true) -> Icons.AutoMirrored.Filled.ArrowBack
        else -> Icons.Filled.Refresh
    }
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun HospitalSelectionDialog(
    hospitals: List<com.example.emergencynow.domain.model.response.HospitalDto>,
    isLoading: Boolean,
    onHospitalSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Select Hospital",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        hospitals.forEach { hospital ->
                            Card(
                                onClick = { onHospitalSelected(hospital.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        hospital.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (hospital.distance != null) {
                                        Text(
                                            "Distance: ${hospital.distance}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientProfileDialog(
    egn: String,
    onDismiss: () -> Unit
) {
    val getProfileByEgnUseCase: GetProfileByEgnUseCase = get()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(egn) {
        isLoading = true
        error = null
        profile = null
        coroutineScope.launch {
            try {
                val result = getProfileByEgnUseCase(egn)
                result.fold(
                    onSuccess = { loadedProfile ->
                        profile = loadedProfile
                        isLoading = false
                    },
                    onFailure = { e ->
                        error = e.message ?: "Failed to load patient profile"
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                error = e.message ?: "An unexpected error occurred"
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Patient Medical Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = error ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    profile != null -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val loadedProfile = profile!!
                            
                            Text(
                                text = "EGN: $egn",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            ProfileInfoCard(title = "Physical Information") {
                                InfoRow("Height", "${loadedProfile.height} cm")
                                InfoRow("Weight", "${loadedProfile.weight} kg")
                                InfoRow("Gender", loadedProfile.gender.name)
                                if (loadedProfile.dateOfBirth != null) {
                                    InfoRow("Date of Birth", loadedProfile.dateOfBirth!!)
                                }
                                if (loadedProfile.bloodType != null) {
                                    InfoRow("Blood Type", loadedProfile.bloodType!!)
                                }
                            }
                            
                            if (!loadedProfile.allergies.isNullOrEmpty()) {
                                ProfileInfoCard(title = "Allergies") {
                                    loadedProfile.allergies!!.forEach { allergy ->
                                        Text("• $allergy", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            
                            if (!loadedProfile.illnesses.isNullOrEmpty()) {
                                ProfileInfoCard(title = "Chronic Illnesses") {
                                    loadedProfile.illnesses!!.forEach { illness ->
                                        Text("• $illness", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            
                            if (!loadedProfile.medicines.isNullOrEmpty()) {
                                ProfileInfoCard(title = "Current Medications") {
                                    loadedProfile.medicines!!.forEach { medicine ->
                                        Text("• $medicine", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
