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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import org.koin.androidx.compose.koinViewModel
import com.example.emergencynow.ui.util.createAmbulanceMarker
import com.example.emergencynow.ui.util.createUserLocationMarker
import com.example.emergencynow.R
import com.example.emergencynow.domain.model.entity.CallStatus

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallTrackingScreen(
    onBackToHome: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
    callTrackingViewModel: CallTrackingViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val trackingState by callTrackingViewModel.uiState.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            callTrackingViewModel.connectSocket()
            homeViewModel.startLocationUpdates()
            try {
                kotlinx.coroutines.awaitCancellation()
            } finally {
                callTrackingViewModel.disconnectSocket()
            }
        }
    }

    LaunchedEffect(homeState.userLocation) {
        val loc = homeState.userLocation ?: return@LaunchedEffect
        if (cameraPositionState.position.target == LatLng(0.0, 0.0)) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
        }
    }

    LaunchedEffect(trackingState.activeRoutePolyline, trackingState.ambulanceLocation, trackingState.userCallStatus) {
        val showRoute = trackingState.userCallStatus == CallStatus.DISPATCHED ||
            trackingState.userCallStatus == CallStatus.EN_ROUTE
        if (showRoute && trackingState.activeRoutePolyline.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            trackingState.activeRoutePolyline.forEach { builder.include(it) }
            homeState.userLocation?.let { builder.include(it) }
            trackingState.ambulanceLocation?.let { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } else {
            homeState.userLocation?.let { userLocation ->
                cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 15f)
            }
        }
    }

    LaunchedEffect(trackingState.activeCallId) {
        if (trackingState.activeCallId == null) {
            onBackToHome()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emergency Call Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            callTrackingViewModel.clearCallState()
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
                homeState.userLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Your Location",
                        icon = createUserLocationMarker(context, R.drawable.user)
                    )
                }

                val showAmbulanceOverlay = trackingState.userCallStatus == CallStatus.DISPATCHED ||
                    trackingState.userCallStatus == CallStatus.EN_ROUTE

                if (showAmbulanceOverlay && trackingState.ambulanceLocation != null) {
                    Marker(
                        state = MarkerState(position = trackingState.ambulanceLocation!!),
                        title = "Ambulance",
                        icon = createAmbulanceMarker(context, R.drawable.ambulance)
                    )
                }

                if (showAmbulanceOverlay && trackingState.activeRoutePolyline.isNotEmpty()) {
                    Polyline(
                        points = trackingState.activeRoutePolyline,
                        color = Color.Blue,
                        width = 10f
                    )
                }
            }

            trackingState.patientName?.let { name ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Calling on behalf of: $name",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val identified = trackingState.patientIdentified
                        if (identified != null) {
                            Text(
                                text = if (identified)
                                    "Patient identified in the archive"
                                else
                                    "Patient could not be identified in the archive",
                                fontSize = 12.sp,
                                color = if (identified)
                                    Color(0xFF16A34A)
                                else
                                    Color(0xFFEF4444),
                            )
                        }
                    }
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
                    when (trackingState.userCallStatus) {
                        CallStatus.PENDING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            when {
                                trackingState.isAwaitingDispatcher -> {
                                    Text(
                                        "Waiting for a dispatcher",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    trackingState.queuePosition?.let { pos ->
                                        Text(
                                            "You are #$pos in queue",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                trackingState.isWithDispatcher -> {
                                    Text(
                                        "A dispatcher is reviewing your call",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Selecting the nearest ambulance for you",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {
                                    Text(
                                        "Waiting for acceptance...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Your emergency call is being dispatched",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        CallStatus.DISPATCHED, CallStatus.EN_ROUTE -> {
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
                            if (trackingState.activeRouteDistance > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    "Estimated arrival: ${trackingState.activeRouteDuration / 60} min (${trackingState.activeRouteDistance}m)",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        CallStatus.ARRIVED -> {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Ambulance has arrived",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF16A34A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Help is here. Please follow paramedic instructions.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        CallStatus.NAVIGATING_TO_HOSPITAL -> {
                            Icon(
                                Icons.Filled.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "On the way to hospital",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            if (trackingState.activeRouteDuration > 0) {
                                Text(
                                    "Estimated arrival: ${trackingState.activeRouteDuration / 60} min",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
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
