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

    LaunchedEffect(trackingState.activeRoutePolyline, trackingState.ambulanceLocation, trackingState.userCallStatus) {
        if (trackingState.userCallStatus != "pending" && trackingState.activeRoutePolyline.isNotEmpty()) {
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

    LaunchedEffect(trackingState.activeCallId, trackingState.userCallStatus) {
        if (trackingState.activeCallId == null) {
            onBackToHome()
        } else if (trackingState.userCallStatus == "arrived" ||
            trackingState.userCallStatus == "completed" ||
            trackingState.userCallStatus == "cancelled") {
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

                if (trackingState.userCallStatus != "pending" && trackingState.ambulanceLocation != null) {
                    Marker(
                        state = MarkerState(position = trackingState.ambulanceLocation!!),
                        title = "Ambulance",
                        icon = createAmbulanceMarker(context, R.drawable.ambulance)
                    )
                }

                if (trackingState.userCallStatus != "pending" && trackingState.activeRoutePolyline.isNotEmpty()) {
                    Polyline(
                        points = trackingState.activeRoutePolyline,
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
                    when (trackingState.userCallStatus) {
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
