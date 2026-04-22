package com.example.emergencynow.ui.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.util.createAmbulanceMarker
import com.example.emergencynow.ui.util.createHospitalMarker
import com.example.emergencynow.ui.util.createUserLocationMarker
import com.example.emergencynow.ui.feature.home.BottomNavItem
import com.example.emergencynow.ui.feature.home.HospitalSelectionDialog
import com.example.emergencynow.ui.feature.home.IncomingCallDialog
import com.example.emergencynow.ui.feature.home.PatientProfileDialog
import com.example.emergencynow.ui.feature.home.StepIcon
import com.example.emergencynow.ui.feature.home.StepItem
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
    viewModel: HomeViewModel = koinViewModel(),
    driverViewModel: DriverViewModel = koinViewModel(),
    callTrackingViewModel: CallTrackingViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val homeState by viewModel.uiState.collectAsStateWithLifecycle()
    val driverState by driverViewModel.uiState.collectAsStateWithLifecycle()
    val trackingState by callTrackingViewModel.uiState.collectAsStateWithLifecycle()
    var showPatientProfile by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            driverViewModel.refresh()
        }
    }

    LaunchedEffect(homeState.isLoading, homeState.isDriver) {
        if (!homeState.isLoading) {
            if (homeState.isDriver) {
                val userId = com.example.emergencynow.ui.util.AuthSession.userId ?: return@LaunchedEffect
                driverViewModel.loadData(userId)
            } else {
                callTrackingViewModel.connectSocket()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) viewModel.startLocationUpdates()
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

    LaunchedEffect(homeState.userLocation) {
        val location = homeState.userLocation ?: return@LaunchedEffect
        driverViewModel.updateDriverLocation(location)
        if (cameraPositionState.position.target == com.google.android.gms.maps.model.LatLng(0.0, 0.0)) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    LaunchedEffect(driverState.hospitalRoutePolyline) {
        val points = driverState.hospitalRoutePolyline
        if (points.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            points.forEach { builder.include(it) }
            homeState.userLocation?.let { builder.include(it) }
            driverState.hospitalLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    LaunchedEffect(trackingState.activeRoutePolyline, trackingState.ambulanceLocation) {
        if (!homeState.isDriver && trackingState.activeRoutePolyline.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            trackingState.activeRoutePolyline.forEach { builder.include(it) }
            homeState.userLocation?.let { builder.include(it) }
            trackingState.ambulanceLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            homeState.userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = if (homeState.isDriver) {
                        if (driverState.assignedAmbulanceId != null) "Your Ambulance" else "Your Location"
                    } else {
                        "Your Location"
                    },
                    icon = if (homeState.isDriver && driverState.assignedAmbulanceId != null) {
                        createAmbulanceMarker(context, R.drawable.ambulance)
                    } else {
                        createUserLocationMarker(context, R.drawable.user)
                    }
                )
            }

            if (homeState.isDriver && driverState.emergencyLocation != null) {
                Marker(
                    state = MarkerState(position = driverState.emergencyLocation!!),
                    title = "Emergency",
                    icon = createUserLocationMarker(context, R.drawable.user)
                )
            }

            if (!homeState.isDriver && trackingState.ambulanceLocation != null &&
                trackingState.userCallStatus != "pending" && trackingState.userCallStatus != "arrived") {
                Marker(
                    state = MarkerState(position = trackingState.ambulanceLocation!!),
                    title = "Ambulance",
                    icon = createAmbulanceMarker(context, R.drawable.ambulance)
                )
            }

            if (driverState.hospitalLocation != null) {
                Marker(
                    state = MarkerState(position = driverState.hospitalLocation!!),
                    title = driverState.selectedHospitalName ?: "Hospital",
                    icon = createHospitalMarker(context, R.drawable.hospital)
                )
            }

            if (homeState.isDriver && driverState.activeRoutePolyline.isNotEmpty() && driverState.callStatus != CallStatus.NAVIGATING_TO_HOSPITAL) {
                Polyline(
                    points = driverState.activeRoutePolyline,
                    color = Color.Blue,
                    width = 10f
                )
            }

            if (driverState.hospitalRoutePolyline.isNotEmpty()) {
                Polyline(
                    points = driverState.hospitalRoutePolyline,
                    color = Color(0xFF3B82F6),
                    width = 12f
                )
            }
        }

        if (homeState.isDriver) {
            if (driverState.activeCallId != null) {
                    var expanded by remember { mutableStateOf(false) }

                    val currentSteps = if (driverState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        driverState.hospitalRouteSteps
                    } else {
                        driverState.activeRouteSteps
                    }

                    val currentDistance = if (driverState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        driverState.hospitalRouteDistance
                    } else {
                        driverState.activeRouteDistance
                    }

                    val currentDuration = if (driverState.callStatus == CallStatus.NAVIGATING_TO_HOSPITAL) {
                        driverState.hospitalRouteDuration
                    } else {
                        driverState.activeRouteDuration
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
                                    IconButton(
                                        onClick = {
                                            if (driverState.patientEgn != null) {
                                                showPatientProfile = true
                                            }
                                        },
                                        modifier = Modifier.size(48.dp),
                                        enabled = driverState.patientEgn != null
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "View Patient Profile",
                                            tint = if (driverState.patientEgn != null)
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
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (driverState.isSocketConnected) "Available" else "Connecting...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (driverState.isSocketConnected) Color(0xFF16A34A) else Color.Gray
                                        )
                                        if (!driverState.isSocketConnected) {
                                            Spacer(Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { driverViewModel.retryConnection() },
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
                                    if (!driverState.isSocketConnected && driverState.error != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = driverState.error ?: "Connection failed",
                                            fontSize = 11.sp,
                                            color = Color(0xFFEF4444),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        } else if (trackingState.activeCallId != null && trackingState.userCallStatus != "arrived") {
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
                    
                    if (trackingState.activeRouteDistance > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Estimated arrival: ${trackingState.activeRouteDuration / 60} min (${trackingState.activeRouteDistance}m)",
                            fontSize = 14.sp,
                            color = BrandBlueDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (homeState.isDriver && driverState.activeCallId != null) {
            when (driverState.callStatus) {
                CallStatus.EN_ROUTE -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(bottom = 240.dp)
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandBlueDark)
                            .clickable { driverViewModel.updateCallStatus(CallStatus.ARRIVED) },
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
                            .padding(bottom = 240.dp)
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandBlueDark)
                            .clickable { driverViewModel.completeCall() },
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (homeState.isDriver) {
                if (driverState.assignedAmbulanceId == null) {
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
                                        text = driverState.assignedAmbulancePlate ?: "Unknown",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlueDark
                                    )
                                }
                            }
                            if (driverState.activeCallId == null) {
                                OutlinedButton(
                                    onClick = { driverViewModel.unassignAmbulance() },
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
            
            if (homeState.isDoctor) {
                Button(
                    onClick = onPatientLookup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlueMid,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Patient Lookup",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (!homeState.isDriver || (homeState.isDriver && driverState.activeCallId == null)) {
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
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BottomNavItem(
                        icon = Icons.Filled.Home,
                        label = "Home",
                        onClick = { },
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

    if (driverState.incomingCallOffer != null) {
        IncomingCallDialog(
            offer = driverState.incomingCallOffer!!,
            onAccept = { driverViewModel.acceptCall(driverState.incomingCallOffer!!.callId) },
            onDecline = { driverViewModel.declineCall(driverState.incomingCallOffer!!.callId) }
        )
    }

    if (driverState.showHospitalSelection) {
        HospitalSelectionDialog(
            hospitals = driverState.availableHospitals,
            isLoading = driverState.isLoadingHospitals || driverState.isSelectingHospital,
            onHospitalSelected = { hospitalId ->
                driverViewModel.selectHospital(hospitalId)
            },
            onDismiss = { }
        )
    }

    driverState.patientEgn?.let { egn ->
        if (showPatientProfile) {
            PatientProfileDialog(
                egn = egn,
                onDismiss = { showPatientProfile = false }
            )
        }
    }
}
