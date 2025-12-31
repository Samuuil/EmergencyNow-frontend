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
import androidx.compose.ui.draw.shadow
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
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.util.createAmbulanceMarker
import com.example.emergencynow.ui.util.createHospitalMarker
import com.example.emergencynow.ui.util.createUserLocationMarker
import com.example.emergencynow.R

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
                    title = if (uiState.isDriver) {
                        if (uiState.assignedAmbulanceId != null) "Your Ambulance" else "Your Location"
                    } else {
                        "Your Location"
                    },
                    icon = if (uiState.isDriver) {
                        // Driver: show ambulance icon if ambulance selected, user icon if not
                        if (uiState.assignedAmbulanceId != null) {
                            createAmbulanceMarker(context, R.drawable.ambulance)
                        } else {
                            createUserLocationMarker(context, R.drawable.user)
                        }
                    } else {
                        // Regular user: show user location icon
                        createUserLocationMarker(context, R.drawable.user)
                    }
                )
            }

            if (uiState.isDriver && uiState.emergencyLocation != null) {
                Marker(
                    state = MarkerState(position = uiState.emergencyLocation!!),
                    title = "Emergency",
                    icon = createUserLocationMarker(context, R.drawable.user)
                )
            }

            // Only show ambulance marker for users when call is dispatched/en_route (not pending) and not arrived
            if (!uiState.isDriver && uiState.ambulanceLocation != null && 
                uiState.userCallStatus != "pending" && uiState.userCallStatus != "arrived") {
                Marker(
                    state = MarkerState(position = uiState.ambulanceLocation!!),
                    title = "Ambulance",
                    icon = createAmbulanceMarker(context, R.drawable.ambulance)
                )
            }

            if (uiState.hospitalLocation != null) {
                Marker(
                    state = MarkerState(position = uiState.hospitalLocation!!),
                    title = uiState.selectedHospitalName ?: "Hospital",
                    icon = createHospitalMarker(context, R.drawable.hospital)
                )
            }

            // Only show route for drivers, not for users
            if (uiState.isDriver && uiState.activeRoutePolyline.isNotEmpty() && uiState.callStatus != CallStatus.NAVIGATING_TO_HOSPITAL) {
                Polyline(
                    points = uiState.activeRoutePolyline,
                    color = Color.Blue,
                    width = 10f
                )
            }

            if (uiState.hospitalRoutePolyline.isNotEmpty()) {
                Polyline(
                    points = uiState.hospitalRoutePolyline,
                    color = Color(0xFF3B82F6), // Nice blue color instead of neon green
                    width = 12f
                )
            }
        }

        if (uiState.isDriver) {
            if (uiState.activeCallId != null) {
                    // On-call: show primary instruction + ETA
                    var expanded by remember { mutableStateOf(false) }
                    
                    // Determine which route to show based on call status
                    val currentSteps = if (uiState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        uiState.hospitalRouteSteps
                    } else {
                        uiState.activeRouteSteps
                    }
                    
                    val currentDistance = if (uiState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        uiState.hospitalRouteDistance
                    } else {
                        uiState.activeRouteDistance
                    }
                    
                    val currentDuration = if (uiState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        uiState.hospitalRouteDuration
                    } else {
                        uiState.activeRouteDuration
                    }
                    
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
                                    StepIcon(currentSteps.firstOrNull())
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        currentSteps.firstOrNull() ?: "Drive to destination",
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
                                                BrandBlueDark 
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

                            if (currentDuration > 0 || currentDistance > 0) {
                                Text(
                                    "ETA: ${currentDuration / 60} min • ${currentDistance} m",
                                    color = BrandBlueDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (expanded && currentSteps.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "All Steps (${currentSteps.size} total):",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = BrandBlueDark
                                )
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .heightIn(max = 300.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentSteps.forEachIndexed { index, step ->
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
                            containerColor = Color.White
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
        } else if (uiState.activeCallId != null && uiState.userCallStatus != "arrived") {
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
                            tint = BrandBlueDark,
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
                            color = BrandBlueDark,
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
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandBlueDark)
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
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandBlueDark)
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
                    Button(
                        onClick = onSelectAmbulance,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                            .height(56.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlueDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Select Ambulance",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
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
                                            BrandBlueDark,
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
                                        color = BrandBlueDark
                                    )
                                }
                            }
                            if (uiState.activeCallId == null) {
                                OutlinedButton(
                                    onClick = { viewModel.unassignAmbulance() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = BrandBlueDark
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
                        containerColor = BrandBlueMid.copy(alpha = 0.1f)
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
                            tint = BrandBlueDark
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Patient Lookup",
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CurvePaleBlue
            )
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
                    modifier = Modifier.size(64.dp),
                    tint = BrandBlueDark
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "   Call " + "Incoming",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "A patient needs immediate medical assistance",
                    fontSize = 16.sp,
                    color = BrandBlueDark.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color.Red.copy(alpha = 0.2f)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            "Decline",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlueDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            "Accept",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
    Icon(icon, contentDescription = null, tint = BrandBlueDark)
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
            tint = if (isSelected) BrandBlueDark else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) BrandBlueDark else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CurvePaleBlue
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Select Hospital",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandBlueDark)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hospitals.forEach { hospital ->
                            Card(
                                onClick = { onHospitalSelected(hospital.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            tint = BrandBlueDark,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                hospital.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandBlueDark
                                            )
                                            if (hospital.distance != null) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "Distance: ${hospital.distance}m",
                                                    fontSize = 14.sp,
                                                    color = BrandBlueDark.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = BrandBlueDark,
                                            modifier = Modifier.size(20.dp)
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CurvePaleBlue
            )
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            "Close",
                            tint = BrandBlueDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandBlueDark)
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
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlueDark
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = error ?: "Unknown error",
                                    fontSize = 16.sp,
                                    color = BrandBlueDark.copy(alpha = 0.7f)
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
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
                                        Text(
                                            "• $allergy",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
                                        )
                                    }
                                }
                            }
                            
                            if (!loadedProfile.illnesses.isNullOrEmpty()) {
                                ProfileInfoCard(title = "Chronic Illnesses") {
                                    loadedProfile.illnesses!!.forEach { illness ->
                                        Text(
                                            "• $illness",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
                                        )
                                    }
                                }
                            }
                            
                            if (!loadedProfile.medicines.isNullOrEmpty()) {
                                ProfileInfoCard(title = "Current Medications") {
                                    loadedProfile.medicines!!.forEach { medicine ->
                                        Text(
                                            "• $medicine",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
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
private fun ProfileInfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlueDark
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = BrandBlueDark.copy(alpha = 0.2f)
            )
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
            fontSize = 14.sp,
            color = BrandBlueDark.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlueDark
        )
    }
}
