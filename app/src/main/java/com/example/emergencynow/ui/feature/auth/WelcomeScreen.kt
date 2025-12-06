@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.emergencynow.ui.feature.auth

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.emergencynow.ui.extention.AuthSession
import com.example.emergencynow.ui.extention.BackendClient
import com.example.emergencynow.ui.extention.CreateCallRequest
import com.example.emergencynow.ui.extention.AssignDriverRequest
import com.example.emergencynow.ui.extention.DriverSocketManager
import com.example.emergencynow.ui.extention.CallOffer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import android.annotation.SuppressLint
import com.example.emergencynow.ui.extention.CallRoute
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

@Composable
fun WelcomeScreen(
    onRegisterEgn: () -> Unit,
    onLogin: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚑", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Welcome to Emergency Now",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Emergency assistance at your fingertips.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onRegisterEgn, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Register EGN")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Log In")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    onMakeEmergencyCall: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectAmbulance: () -> Unit,
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isDriver by remember { mutableStateOf(false) }
    var roleCheckError by remember { mutableStateOf<String?>(null) }
    var assignedAmbulancePlate by remember { mutableStateOf<String?>(null) }
    var assignedAmbulanceId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var debugRole by remember { mutableStateOf<String?>(null) }
    var debugError by remember { mutableStateOf<String?>(null) }

    // WebSocket state for incoming calls
    var incomingCallOffer by remember { mutableStateOf<CallOffer?>(null) }
    var isSocketConnected by remember { mutableStateOf(false) }
    var activeCallId by remember { mutableStateOf<String?>(null) }

    // Active call navigation state
    var activeRoutePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var emergencyLocation by remember { mutableStateOf<LatLng?>(null) }
    var activeRouteDistance by remember { mutableStateOf(0) }
    var activeRouteDuration by remember { mutableStateOf(0) }
    var driverCurrentLocation by remember { mutableStateOf<LatLng?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        userLocation = latLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                }
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
        val accessToken = AuthSession.accessToken
        val userId = AuthSession.userId
        if (accessToken != null && accessToken.isNotEmpty() && userId != null && userId.isNotEmpty()) {
            try {
                val roleResponse = BackendClient.api.getUserRole(
                    bearer = "Bearer $accessToken",
                    id = userId,
                )
                val role = roleResponse.string().trim()
                debugRole = role
                isDriver = role == "DRIVER"
                if (isDriver) {
                    try {
                        val ambulance = BackendClient.api.getAmbulanceByDriver(userId)
                        assignedAmbulancePlate = ambulance?.licensePlate
                        assignedAmbulanceId = ambulance?.id
                    } catch (_: Exception) { }
                }
            } catch (e: Exception) {
                debugError = e.message
            }
        } else {
            debugError = "Missing token or userId: token=${accessToken != null}, userId=$userId"
        }
    }

    // Helper function to decode Google polyline
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    // Connect to WebSocket when driver has an assigned ambulance
    LaunchedEffect(isDriver, assignedAmbulanceId) {
        val accessToken = AuthSession.accessToken
        if (isDriver && assignedAmbulanceId != null && !accessToken.isNullOrEmpty()) {
            // Set up callbacks before connecting
            DriverSocketManager.onCallOffer = { offer ->
                incomingCallOffer = offer
                // Store emergency location when offer comes in
                emergencyLocation = LatLng(offer.latitude, offer.longitude)
            }
            DriverSocketManager.onConnectionChange = { connected ->
                isSocketConnected = connected
            }
            DriverSocketManager.onCallRoute = { route ->
                // Call was accepted and route received - store route data
                activeCallId = route.callId
                activeRoutePolyline = decodePolyline(route.polyline)
                activeRouteDistance = route.distance
                activeRouteDuration = route.duration
            }
            DriverSocketManager.onRouteUpdate = { route ->
                // Route updated during navigation
                activeRoutePolyline = decodePolyline(route.polyline)
                activeRouteDistance = route.distance
                activeRouteDuration = route.duration
            }
            DriverSocketManager.connect(accessToken)
        } else {
            DriverSocketManager.disconnect()
        }
    }

    // Cleanup WebSocket on dispose
    DisposableEffect(Unit) {
        onDispose {
            DriverSocketManager.disconnect()
        }
    }

    // Location update loop - runs every 2 seconds when there's an active call
    LaunchedEffect(activeCallId) {
        if (activeCallId != null) {
            while (isActive) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val newLocation = LatLng(location.latitude, location.longitude)
                            driverCurrentLocation = newLocation
                            userLocation = newLocation
                            // Send location update via WebSocket
                            DriverSocketManager.sendLocationUpdate(
                                activeCallId!!,
                                location.latitude,
                                location.longitude
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore location errors
                }
                delay(2000) // Update every 2 seconds
            }
        }
    }

    // Update camera to follow driver when navigating
    LaunchedEffect(driverCurrentLocation, activeCallId) {
        if (activeCallId != null && driverCurrentLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(driverCurrentLocation!!, 16f)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // Show driver's current location (blue marker when navigating, default otherwise)
                    val currentLoc = if (activeCallId != null) driverCurrentLocation else userLocation
                    currentLoc?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = if (activeCallId != null) "You (Ambulance)" else "Your Location",
                            icon = if (activeCallId != null)
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                            else null
                        )
                    }

                    // Show emergency location marker when navigating
                    if (activeCallId != null) {
                        emergencyLocation?.let { emergency ->
                            Marker(
                                state = MarkerState(position = emergency),
                                title = "Emergency Location",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )
                        }

                        // Show route polyline
                        if (activeRoutePolyline.isNotEmpty()) {
                            Polyline(
                                points = activeRoutePolyline,
                                color = Color(0xFF1976D2),
                                width = 12f
                            )
                        }
                    }
                }

                // Navigation info overlay when there's an active call
                if (activeCallId != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚨 Navigating to Emergency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val distanceKm = activeRouteDistance / 1000.0
                                val durationMin = activeRouteDuration / 60
                                Text("📍 %.1f km".format(distanceKm))
                                Text("⏱️ ~$durationMin min")
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // When there's an active call, show navigation controls
                if (activeCallId != null) {
                    Button(
                        onClick = {
                            // Mark as arrived
                            val token = AuthSession.accessToken
                            val callId = activeCallId
                            if (!token.isNullOrEmpty() && callId != null) {
                                scope.launch {
                                    try {
                                        BackendClient.api.updateCallStatus(
                                            bearer = "Bearer $token",
                                            id = callId,
                                            body = mapOf("status" to "arrived")
                                        )
                                    } catch (_: Exception) { }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("🏥 Mark as Arrived")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            // Complete the call
                            val token = AuthSession.accessToken
                            val callId = activeCallId
                            if (!token.isNullOrEmpty() && callId != null) {
                                scope.launch {
                                    try {
                                        BackendClient.api.updateCallStatus(
                                            bearer = "Bearer $token",
                                            id = callId,
                                            body = mapOf("status" to "completed")
                                        )
                                        // Reset navigation state
                                        activeCallId = null
                                        activeRoutePolyline = emptyList()
                                        emergencyLocation = null
                                        activeRouteDistance = 0
                                        activeRouteDuration = 0
                                    } catch (_: Exception) { }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("✅ Complete Call")
                    }
                } else {
                    // Normal home screen buttons
                    if (isDriver) {
                        assignedAmbulancePlate?.let { plate ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Using ambulance: $plate",
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = {
                                        val token = AuthSession.accessToken
                                        val ambulanceId = assignedAmbulanceId
                                        if (token.isNullOrEmpty() || ambulanceId.isNullOrEmpty()) return@TextButton
                                        scope.launch {
                                            try {
                                                BackendClient.api.assignAmbulanceDriver(
                                                    bearer = "Bearer $token",
                                                    id = ambulanceId,
                                                    body = AssignDriverRequest(driverId = null)
                                                )
                                                assignedAmbulancePlate = null
                                                assignedAmbulanceId = null
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                ) {
                                    Text("X")
                                }
                            }
                        }

                        Button(
                            onClick = onSelectAmbulance,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Select Ambulance")
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Button(
                        onClick = onMakeEmergencyCall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text("Make Emergency Call")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Profile")
                    }

                    // Show WebSocket connection status for drivers
                    if (isDriver && assignedAmbulanceId != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isSocketConnected) "🟢 Connected - Waiting for calls" else "🔴 Disconnected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSocketConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
        }

        // Incoming Call Dialog
        incomingCallOffer?.let { offer ->
            Dialog(
                onDismissRequest = { /* Don't allow dismiss by tapping outside */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Alert icon
                        Text(
                            text = "🚨",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Incoming Emergency Call",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(Modifier.height(16.dp))

                        // Call details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (offer.description.isNotBlank()) {
                                    Text(
                                        text = offer.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }

                                val distanceKm = offer.distance / 1000.0
                                val durationMin = offer.duration / 60
                                Text(
                                    text = "📍 Distance: %.1f km".format(distanceKm),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "⏱️ ETA: ~$durationMin min",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reject button
                            OutlinedButton(
                                onClick = {
                                    DriverSocketManager.respondToCall(offer.callId, accept = false)
                                    incomingCallOffer = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF44336)
                                )
                            ) {
                                Text("Reject")
                            }

                            // Accept button
                            Button(
                                onClick = {
                                    DriverSocketManager.respondToCall(offer.callId, accept = true)
                                    activeCallId = offer.callId
                                    emergencyLocation = LatLng(offer.latitude, offer.longitude)
                                    incomingCallOffer = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceSelectionScreen(
    onBack: () -> Unit,
    onAmbulanceSelected: () -> Unit,
) {
    val accessToken = AuthSession.accessToken
    var ambulances by remember { mutableStateOf<List<com.example.emergencynow.ui.extention.AmbulanceDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (accessToken.isNullOrEmpty()) {
            error = "Missing session. Log in again."
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val all = BackendClient.api.getAvailableAmbulances()
            ambulances = all.filter { it.driverId == null }
        } catch (e: Exception) {
            error = "Failed to load ambulances."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Ambulance") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(ambulances.size) { index ->
                    val ambulance = ambulances[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Plate: ${ambulance.licensePlate}")
                            ambulance.vehicleModel?.let { Text("Model: $it") }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (accessToken.isNullOrEmpty()) return@Button
                                    val userId = AuthSession.userId ?: return@Button
                                    scope.launch {
                                        try {
                                            BackendClient.api.assignAmbulanceDriver(
                                                bearer = "Bearer $accessToken",
                                                id = ambulance.id,
                                                body = AssignDriverRequest(driverId = userId)
                                            )
                                            onAmbulanceSelected()
                                        } catch (e: Exception) {
                                            error = "Failed to assign ambulance."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Use this ambulance")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyCallScreen(
    onBack: () -> Unit,
    onCallCreated: (String) -> Unit,
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var patientEgn by remember { mutableStateOf(AuthSession.egn ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()
                    }
                }
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emergency Call") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        if (isLoading) return@Button
                        val accessToken = AuthSession.accessToken
                        if (accessToken.isNullOrEmpty()) {
                            error = "Missing session. Log in again."
                            return@Button
                        }
                        val lat = latitude.toDoubleOrNull()
                        val lng = longitude.toDoubleOrNull()
                        if (lat == null || lng == null) {
                            error = "Latitude and longitude must be numbers."
                            return@Button
                        }
                        if (patientEgn.isBlank()) {
                            error = "Patient EGN is required."
                            return@Button
                        }
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                val response = BackendClient.api.createCall(
                                    bearer = "Bearer $accessToken",
                                    body = CreateCallRequest(
                                        description = description.ifBlank { "" },
                                        latitude = lat,
                                        longitude = lng,
                                        patientEgn = patientEgn
                                    )
                                )
                                val callId = response.id
                                if (callId.isNullOrEmpty()) {
                                    error = "Invalid response from server."
                                } else {
                                    onCallCreated(callId)
                                }
                            } catch (e: Exception) {
                                error = "Failed to create emergency call."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Finish Call")
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = patientEgn,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { ch -> ch.isDigit() }) {
                        patientEgn = newValue
                    }
                },
                label = { Text("Patient EGN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun CallTrackingScreen(
    callId: String,
    onBack: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var patientLocation by remember { mutableStateOf<LatLng?>(null) }
    var ambulanceLocation by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var etaSeconds by remember { mutableStateOf<Int?>(null) }
    var distanceMeters by remember { mutableStateOf<Int?>(null) }
    var otherAmbulances by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latD = lat / 1E5
            val lngD = lng / 1E5
            poly.add(LatLng(latD, lngD))
        }
        return poly
    }

    LaunchedEffect(callId) {
        val accessToken = AuthSession.accessToken
        if (accessToken.isNullOrEmpty()) {
            error = "Missing session. Log in again."
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val tracking = BackendClient.api.getCallTracking(
                bearer = "Bearer $accessToken",
                id = callId
            )
            val call = tracking.call
            patientLocation = LatLng(call.latitude, call.longitude)

            val current = tracking.currentLocation
            val fallbackLat = call.ambulanceCurrentLatitude
            val fallbackLng = call.ambulanceCurrentLongitude
            ambulanceLocation = when {
                current != null -> LatLng(current.latitude, current.longitude)
                fallbackLat != null && fallbackLng != null -> LatLng(fallbackLat, fallbackLng)
                else -> null
            }

            tracking.route?.let { route ->
                etaSeconds = route.duration
                distanceMeters = route.distance
                if (route.polyline.isNotEmpty()) {
                    polylinePoints = decodePolyline(route.polyline)
                }
            }

            try {
                val ambulances = BackendClient.api.getAvailableAmbulances()
                otherAmbulances = ambulances.mapNotNull { amb ->
                    val lat = amb.latitude
                    val lng = amb.longitude
                    if (lat != null && lng != null) LatLng(lat, lng) else null
                }
            } catch (_: Exception) {
            }

            val focus = ambulanceLocation ?: patientLocation
            focus?.let {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 13f)
            }
        } catch (e: Exception) {
            error = "Failed to load tracking data."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ambulance Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    patientLocation?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Patient"
                        )
                    }
                    ambulanceLocation?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Assigned ambulance",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                    otherAmbulances.forEach { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Available ambulance",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }
                    if (polylinePoints.isNotEmpty()) {
                        Polyline(points = polylinePoints)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (etaSeconds != null || distanceMeters != null) {
                    val minutes = etaSeconds?.div(60)
                    Text(
                        text = buildString {
                            if (minutes != null) append("ETA: ~${minutes} min")
                            if (distanceMeters != null) {
                                if (isNotEmpty()) append("  b7 ")
                                val km = distanceMeters!! / 1000.0
                                append(String.format("Distance: %.1f km", km))
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHomeScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onEditContacts: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage your personal information and emergency contacts.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Personal Information")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onEditContacts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Emergency Contacts")
            }
        }
    }
}
