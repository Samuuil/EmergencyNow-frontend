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
import com.example.emergencynow.ui.extention.HospitalDto
import com.example.emergencynow.ui.extention.GetHospitalsRequest
import com.example.emergencynow.ui.extention.SelectHospitalRequest
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
import com.example.emergencynow.ui.extention.UserSocketManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshTrigger by remember { mutableStateOf(0) }

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

    // Hospital selection state
    var callStatus by remember { mutableStateOf("en_route") } // en_route, arrived, navigating_to_hospital
    var showHospitalSelection by remember { mutableStateOf(false) }
    var availableHospitals by remember { mutableStateOf<List<HospitalDto>>(emptyList()) }
    var isLoadingHospitals by remember { mutableStateOf(false) }
    var selectedHospitalName by remember { mutableStateOf<String?>(null) }
    var hospitalLocation by remember { mutableStateOf<LatLng?>(null) }
    var hospitalRoutePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var hospitalRouteDistance by remember { mutableStateOf(0) }
    var hospitalRouteDuration by remember { mutableStateOf(0) }

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

    // Refetch role and ambulance data when screen resumes (e.g., after ambulance selection)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("HomeScreen", "Screen resumed, triggering refresh")
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(refreshTrigger) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        val accessToken = AuthSession.accessToken
        val userId = AuthSession.userId
        Log.d("HomeScreen", "Init (trigger=$refreshTrigger): userId=$userId, hasToken=${!accessToken.isNullOrEmpty()}")
        if (accessToken != null && accessToken.isNotEmpty() && userId != null && userId.isNotEmpty()) {
            try {
                Log.d("HomeScreen", "Fetching user role...")
                val roleResponse = BackendClient.api.getUserRole(
                    bearer = "Bearer $accessToken",
                    id = userId,
                )
                val role = roleResponse.string().trim()
                debugRole = role
                isDriver = role == "DRIVER"
                Log.d("HomeScreen", "User role: $role, isDriver=$isDriver")
                if (isDriver) {
                    try {
                        Log.d("HomeScreen", "Fetching ambulance for driver $userId...")
                        val ambulance = BackendClient.api.getAmbulanceByDriver(userId)
                        assignedAmbulancePlate = ambulance?.licensePlate
                        assignedAmbulanceId = ambulance?.id
                        Log.d("HomeScreen", "Ambulance assigned: id=${ambulance?.id}, plate=${ambulance?.licensePlate}")
                    } catch (e: Exception) { 
                        Log.e("HomeScreen", "Failed to fetch ambulance: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                debugError = e.message
                Log.e("HomeScreen", "Failed to fetch role: ${e.message}", e)
            }
        } else {
            debugError = "Missing token or userId: token=${accessToken != null}, userId=$userId"
            Log.e("HomeScreen", debugError ?: "Unknown error")
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
        Log.d("HomeScreen", "WebSocket LaunchedEffect: isDriver=$isDriver, ambulanceId=$assignedAmbulanceId, tokenEmpty=${accessToken.isNullOrEmpty()}")
        if (isDriver && assignedAmbulanceId != null && !accessToken.isNullOrEmpty()) {
            Log.d("HomeScreen", "Attempting to connect driver WebSocket...")
            // Set up callbacks before connecting
            DriverSocketManager.onCallOffer = { offer ->
                incomingCallOffer = offer
                // Store emergency location when offer comes in
                emergencyLocation = LatLng(offer.latitude, offer.longitude)
            }
            DriverSocketManager.onConnectionChange = { connected ->
                Log.d("HomeScreen", "WebSocket connection changed: connected=$connected")
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
            // Handle location requests from backend - respond immediately with GPS
            DriverSocketManager.onLocationRequest = { requestId ->
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            DriverSocketManager.sendLocationResponse(
                                requestId,
                                location.latitude,
                                location.longitude
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Silently fail - backend will use stale location
                }
            }
            DriverSocketManager.connect(accessToken)
            Log.d("HomeScreen", "DriverSocketManager.connect() called")
        } else {
            Log.d("HomeScreen", "Disconnecting: condition not met")
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
                                title = if (callStatus == "navigating_to_hospital") "Patient Pickup" else "Emergency Location",
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (callStatus == "navigating_to_hospital") BitmapDescriptorFactory.HUE_ORANGE else BitmapDescriptorFactory.HUE_RED
                                )
                            )
                        }

                        // Show hospital marker when navigating to hospital
                        if (callStatus == "navigating_to_hospital") {
                            hospitalLocation?.let { hospital ->
                                Marker(
                                    state = MarkerState(position = hospital),
                                    title = selectedHospitalName ?: "Hospital",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                )
                            }
                            
                            // Show hospital route polyline
                            if (hospitalRoutePolyline.isNotEmpty()) {
                                Polyline(
                                    points = hospitalRoutePolyline,
                                    color = Color(0xFF4CAF50), // Green for hospital route
                                    width = 12f
                                )
                            }
                        } else {
                            // Show route polyline to emergency (red/blue)
                            if (activeRoutePolyline.isNotEmpty()) {
                                Polyline(
                                    points = activeRoutePolyline,
                                    color = Color(0xFF1976D2),
                                    width = 12f
                                )
                            }
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
                            containerColor = when (callStatus) {
                                "navigating_to_hospital" -> Color(0xFFE3F2FD) // Light blue for hospital
                                "arrived" -> Color(0xFFFFF3E0) // Light orange for arrived
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (callStatus) {
                                "navigating_to_hospital" -> {
                                    Text(
                                        text = "🏥 Navigating to Hospital",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    selectedHospitalName?.let { name ->
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF1976D2)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        val distanceKm = hospitalRouteDistance / 1000.0
                                        val durationMin = hospitalRouteDuration / 60
                                        Text("📍 %.1f km".format(distanceKm))
                                        Text("⏱️ ~$durationMin min")
                                    }
                                }
                                "arrived" -> {
                                    Text(
                                        text = "✅ Arrived at Patient",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Select a hospital to transport patient",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF666666)
                                    )
                                }
                                else -> {
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
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // When there's an active call, show navigation controls
                if (activeCallId != null) {
                    // Show different buttons based on call status
                    when (callStatus) {
                        "en_route" -> {
                            // Driver is on the way to patient - show Mark as Arrived button
                            Button(
                                onClick = {
                                    // Mark as arrived and fetch hospitals
                                    val token = AuthSession.accessToken
                                    val callId = activeCallId
                                    val currentLoc = driverCurrentLocation ?: userLocation
                                    if (!token.isNullOrEmpty() && callId != null && currentLoc != null) {
                                        scope.launch {
                                            try {
                                                // First mark as arrived
                                                BackendClient.api.updateCallStatus(
                                                    bearer = "Bearer $token",
                                                    id = callId,
                                                    body = mapOf("status" to "arrived")
                                                )
                                                callStatus = "arrived"
                                                
                                                // Then fetch hospitals
                                                isLoadingHospitals = true
                                                val hospitals = BackendClient.api.getHospitalsForCall(
                                                    bearer = "Bearer $token",
                                                    id = callId,
                                                    body = GetHospitalsRequest(
                                                        latitude = currentLoc.latitude,
                                                        longitude = currentLoc.longitude
                                                    )
                                                )
                                                availableHospitals = hospitals
                                                showHospitalSelection = true
                                            } catch (e: Exception) {
                                                // Still show hospital selection even if fetching fails
                                                callStatus = "arrived"
                                                showHospitalSelection = true
                                            } finally {
                                                isLoadingHospitals = false
                                            }
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
                        }
                        "arrived" -> {
                            // Driver arrived at patient - show Select Hospital button
                            Button(
                                onClick = {
                                    val token = AuthSession.accessToken
                                    val callId = activeCallId
                                    val currentLoc = driverCurrentLocation ?: userLocation
                                    if (!token.isNullOrEmpty() && callId != null && currentLoc != null) {
                                        scope.launch {
                                            isLoadingHospitals = true
                                            try {
                                                val hospitals = BackendClient.api.getHospitalsForCall(
                                                    bearer = "Bearer $token",
                                                    id = callId,
                                                    body = GetHospitalsRequest(
                                                        latitude = currentLoc.latitude,
                                                        longitude = currentLoc.longitude
                                                    )
                                                )
                                                availableHospitals = hospitals
                                                showHospitalSelection = true
                                            } catch (_: Exception) {
                                                showHospitalSelection = true
                                            } finally {
                                                isLoadingHospitals = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text(if (isLoadingHospitals) "Loading..." else "🏥 Select Hospital")
                            }
                        }
                        "navigating_to_hospital" -> {
                            // Driver is navigating to hospital - show Complete Call button
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
                                                // Reset all navigation state
                                                activeCallId = null
                                                activeRoutePolyline = emptyList()
                                                emergencyLocation = null
                                                activeRouteDistance = 0
                                                activeRouteDuration = 0
                                                callStatus = "en_route"
                                                selectedHospitalName = null
                                                hospitalLocation = null
                                                hospitalRoutePolyline = emptyList()
                                                hospitalRouteDistance = 0
                                                hospitalRouteDuration = 0
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
                                Text("✅ Complete Call - Arrived at Hospital")
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Cancel/Reset button
                    OutlinedButton(
                        onClick = {
                            // Reset call state (cancel)
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
                                    } catch (_: Exception) { }
                                    // Reset all state
                                    activeCallId = null
                                    activeRoutePolyline = emptyList()
                                    emergencyLocation = null
                                    activeRouteDistance = 0
                                    activeRouteDuration = 0
                                    callStatus = "en_route"
                                    selectedHospitalName = null
                                    hospitalLocation = null
                                    hospitalRoutePolyline = emptyList()
                                    hospitalRouteDistance = 0
                                    hospitalRouteDuration = 0
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Cancel Call")
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
                                    callStatus = "en_route"
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

        // Hospital Selection Dialog
        if (showHospitalSelection) {
            Dialog(
                onDismissRequest = { showHospitalSelection = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.8f)
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏥 Select Hospital",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showHospitalSelection = false }) {
                                Text("✕", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        
                        Text(
                            text = "Hospitals sorted by distance from your location",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoadingHospitals) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (availableHospitals.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No hospitals found", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = {
                                        // Retry fetching hospitals
                                        val token = AuthSession.accessToken
                                        val callId = activeCallId
                                        val currentLoc = driverCurrentLocation ?: userLocation
                                        if (!token.isNullOrEmpty() && callId != null && currentLoc != null) {
                                            scope.launch {
                                                isLoadingHospitals = true
                                                try {
                                                    val hospitals = BackendClient.api.getHospitalsForCall(
                                                        bearer = "Bearer $token",
                                                        id = callId,
                                                        body = GetHospitalsRequest(
                                                            latitude = currentLoc.latitude,
                                                            longitude = currentLoc.longitude
                                                        )
                                                    )
                                                    availableHospitals = hospitals
                                                } catch (_: Exception) { }
                                                isLoadingHospitals = false
                                            }
                                        }
                                    }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        } else {
                            // Hospital list
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableHospitals.size) { index ->
                                    val hospital = availableHospitals[index]
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = hospital.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    hospital.address?.let { addr ->
                                                        Text(
                                                            text = addr,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF666666)
                                                        )
                                                    }
                                                }
                                                // Distance badge
                                                hospital.distance?.let { dist ->
                                                    val distanceKm = dist / 1000.0
                                                    val durationMin = (hospital.duration ?: 0) / 60
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "%.1f km".format(distanceKm),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1976D2)
                                                        )
                                                        Text(
                                                            text = "~$durationMin min",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF666666)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(Modifier.height(12.dp))
                                            
                                            Button(
                                                onClick = {
                                                    // Select this hospital
                                                    val token = AuthSession.accessToken
                                                    val callId = activeCallId
                                                    val currentLoc = driverCurrentLocation ?: userLocation
                                                    if (!token.isNullOrEmpty() && callId != null && currentLoc != null) {
                                                        scope.launch {
                                                            try {
                                                                // First, select the hospital
                                                                BackendClient.api.selectHospitalForCall(
                                                                    bearer = "Bearer $token",
                                                                    id = callId,
                                                                    body = SelectHospitalRequest(
                                                                        hospitalId = hospital.id,
                                                                        latitude = currentLoc.latitude,
                                                                        longitude = currentLoc.longitude
                                                                    )
                                                                )
                                                                
                                                                // Set basic hospital info immediately
                                                                selectedHospitalName = hospital.name
                                                                hospitalLocation = LatLng(hospital.latitude, hospital.longitude)
                                                                callStatus = "navigating_to_hospital"
                                                                showHospitalSelection = false
                                                                
                                                                // Then fetch the hospital route separately (backend calculates and stores it)
                                                                try {
                                                                    val routeResponse = BackendClient.api.getHospitalRoute(
                                                                        bearer = "Bearer $token",
                                                                        id = callId
                                                                    )
                                                                    routeResponse.route?.polyline?.let { polyline ->
                                                                        hospitalRoutePolyline = decodePolyline(polyline)
                                                                        Log.d("HomeScreen", "Hospital route loaded: ${hospitalRoutePolyline.size} points")
                                                                    }
                                                                    hospitalRouteDistance = routeResponse.route?.distance ?: 0
                                                                    hospitalRouteDuration = routeResponse.route?.duration ?: 0
                                                                } catch (routeEx: Exception) {
                                                                    Log.e("HomeScreen", "Failed to fetch hospital route: ${routeEx.message}")
                                                                }
                                                                
                                                                // Center camera on hospital
                                                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                                                    hospitalLocation!!,
                                                                    14f
                                                                )
                                                            } catch (e: Exception) {
                                                                Log.e("HomeScreen", "Failed to select hospital: ${e.message}")
                                                                // Even on error, try to use local data
                                                                selectedHospitalName = hospital.name
                                                                hospitalLocation = LatLng(hospital.latitude, hospital.longitude)
                                                                callStatus = "navigating_to_hospital"
                                                                showHospitalSelection = false
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF4CAF50)
                                                )
                                            ) {
                                                Text("Select This Hospital")
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
            val response = BackendClient.api.getAvailableAmbulances("Bearer $accessToken")
            ambulances = response.data.filter { it.driverId == null }
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
                                        description = description.ifBlank { "Emergency call" },
                                        latitude = lat,
                                        longitude = lng
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
    
    // WebSocket state for live tracking
    var isSocketConnected by remember { mutableStateOf(false) }
    var callStatus by remember { mutableStateOf("pending") }
    
    // Hospital information (when driver selects a hospital)
    var selectedHospitalName by remember { mutableStateOf<String?>(null) }
    var hospitalLocation by remember { mutableStateOf<LatLng?>(null) }
    var hospitalRoutePolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var hospitalRouteDistance by remember { mutableStateOf<Int?>(null) }
    var hospitalRouteDuration by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

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
                route.polyline?.let { polyline ->
                    if (polyline.isNotEmpty()) {
                        polylinePoints = decodePolyline(polyline)
                    }
                }
            }

            try {
                val ambulanceResponse = BackendClient.api.getAvailableAmbulances("Bearer $accessToken")
                otherAmbulances = ambulanceResponse.data.mapNotNull { amb ->
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

    // Connect to WebSocket for live tracking updates
    LaunchedEffect(callId) {
        val accessToken = AuthSession.accessToken
        if (!accessToken.isNullOrEmpty()) {
            // Set up callbacks for live updates
            UserSocketManager.onConnectionChange = { connected ->
                isSocketConnected = connected
            }
            UserSocketManager.onCallDispatched = { dispatched ->
                if (dispatched.callId == callId) {
                    ambulanceLocation = LatLng(dispatched.ambulanceLatitude, dispatched.ambulanceLongitude)
                    if (dispatched.polyline.isNotEmpty()) {
                        polylinePoints = decodePolyline(dispatched.polyline)
                    }
                    etaSeconds = dispatched.duration
                    distanceMeters = dispatched.distance
                    callStatus = "dispatched"
                }
            }
            UserSocketManager.onAmbulanceLocation = { update ->
                if (update.callId == callId) {
                    ambulanceLocation = LatLng(update.latitude, update.longitude)
                    // Update route if provided
                    update.polyline?.let { poly ->
                        if (poly.isNotEmpty()) {
                            polylinePoints = decodePolyline(poly)
                        }
                    }
                    update.duration?.let { etaSeconds = it }
                    update.distance?.let { distanceMeters = it }
                }
            }
            UserSocketManager.onCallStatus = { statusUpdate ->
                if (statusUpdate.callId == callId) {
                    callStatus = statusUpdate.status
                    // When status changes to arrived, start polling for hospital selection
                    if (statusUpdate.status == "arrived") {
                        scope.launch {
                            // Poll for hospital route data
                            while (selectedHospitalName == null) {
                                try {
                                    val hospitalRoute = BackendClient.api.getHospitalRoute(
                                        bearer = "Bearer $accessToken",
                                        id = callId
                                    )
                                    if (hospitalRoute.hospital?.name != null) {
                                        selectedHospitalName = hospitalRoute.hospital?.name
                                        hospitalRouteDistance = hospitalRoute.route?.distance
                                        hospitalRouteDuration = hospitalRoute.route?.duration
                                        hospitalRoute.route?.polyline?.let { polyline ->
                                            hospitalRoutePolyline = decodePolyline(polyline)
                                        }
                                        // We don't have hospital lat/lng directly, but route shows direction
                                        callStatus = "navigating_to_hospital"
                                        break
                                    }
                                } catch (_: Exception) { }
                                delay(3000) // Poll every 3 seconds
                            }
                        }
                    }
                }
            }
            UserSocketManager.connect(accessToken)
        }
    }

    // Cleanup WebSocket on dispose
    DisposableEffect(Unit) {
        onDispose {
            UserSocketManager.disconnect()
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
                    // Patient location (your location) - green marker
                    patientLocation?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Your Location",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }
                    // Assigned ambulance - blue marker (moves in real-time)
                    ambulanceLocation?.let { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Ambulance",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                    // Other available ambulances - light blue
                    otherAmbulances.forEach { loc ->
                        Marker(
                            state = MarkerState(position = loc),
                            title = "Available ambulance",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                        )
                    }
                    // Route polyline - show hospital route when navigating to hospital
                    if (callStatus == "navigating_to_hospital" && hospitalRoutePolyline.isNotEmpty()) {
                        Polyline(
                            points = hospitalRoutePolyline,
                            color = Color(0xFF4CAF50), // Green for hospital route
                            width = 10f
                        )
                    } else if (polylinePoints.isNotEmpty()) {
                        Polyline(
                            points = polylinePoints,
                            color = Color(0xFF1976D2),
                            width = 10f
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Call status banner
                val statusText = when (callStatus) {
                    "pending" -> "⏳ Waiting for ambulance..."
                    "dispatched", "en_route" -> "🚑 Ambulance on the way!"
                    "arrived" -> "✅ Ambulance has arrived! Selecting hospital..."
                    "navigating_to_hospital" -> "🏥 Heading to Hospital"
                    "completed" -> "✔️ Call completed"
                    "cancelled" -> "❌ Call cancelled"
                    else -> "📍 Tracking ambulance..."
                }
                val statusColor = when (callStatus) {
                    "arrived" -> Color(0xFF4CAF50)
                    "navigating_to_hospital" -> Color(0xFF1976D2)
                    "completed" -> Color(0xFF2196F3)
                    "cancelled" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.primary
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        // Show hospital name when navigating to hospital
                        if (callStatus == "navigating_to_hospital" && selectedHospitalName != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Destination: $selectedHospitalName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1976D2)
                            )
                            // Show hospital ETA
                            hospitalRouteDuration?.let { duration ->
                                val minutes = duration / 60
                                Text(
                                    text = "Hospital ETA: ~$minutes min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                
                // ETA and distance info
                if (etaSeconds != null || distanceMeters != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        etaSeconds?.let { eta ->
                            val minutes = eta / 60
                            Text(
                                text = "⏱️ ETA: ~$minutes min",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        distanceMeters?.let { dist ->
                            val km = dist / 1000.0
                            Text(
                                text = "📍 %.1f km away".format(km),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Connection status
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSocketConnected) "🟢 Live tracking active" else "🔴 Connecting...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSocketConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Close button for completed/cancelled calls
                if (callStatus == "completed" || callStatus == "cancelled") {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
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
