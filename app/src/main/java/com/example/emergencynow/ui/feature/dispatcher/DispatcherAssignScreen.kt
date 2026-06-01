package com.example.emergencynow.ui.feature.dispatcher

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencynow.R
import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.util.createAmbulanceMarker
import com.example.emergencynow.ui.util.createUserLocationMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatcherAssignScreen(
    callId: String,
    onBack: () -> Unit,
    onAssigned: () -> Unit,
    viewModel: DispatcherViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    val call = state.calls[callId]
    var selectedAmbulance by remember { mutableStateOf<DispatcherAmbulanceSummary?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.connectSocket()
            viewModel.stopAlert()
            try {
                awaitCancellation()
            } finally {
                viewModel.disconnectSocket()
            }
        }
    }

    LaunchedEffect(callId, state.isSocketConnected) {
        if (state.isSocketConnected) viewModel.requestAmbulanceRefresh()
    }

    LaunchedEffect(call?.callId) {
        val c = call ?: return@LaunchedEffect
        if (cameraPositionState.position.target == LatLng(0.0, 0.0)) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(c.latitude, c.longitude),
                14f,
            )
        }
    }

    LaunchedEffect(call?.callId) {
        if (call == null && state.isSocketConnected) {
            onBack()
        }
    }

    val displayableAmbulances = state.ambulances.filter {
        it.available && it.latitude != null && it.longitude != null
    }

    LaunchedEffect(displayableAmbulances, call) {
        if (call == null) return@LaunchedEffect
        val builder = LatLngBounds.Builder()
        var has = false
        builder.include(LatLng(call.latitude, call.longitude))
        has = true
        displayableAmbulances.forEach {
            builder.include(LatLng(it.latitude!!, it.longitude!!))
            has = true
        }
        if (has) {
            try {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
            } catch (_: Exception) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(call.latitude, call.longitude),
                    14f,
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        call?.description?.ifBlank { "Assign ambulance" } ?: "Assign ambulance",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            if (call != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(call.latitude, call.longitude)),
                        title = "Emergency",
                        snippet = call.description,
                        icon = createUserLocationMarker(context, R.drawable.user),
                    )

                    displayableAmbulances.forEach { amb ->
                        Marker(
                            state = MarkerState(position = LatLng(amb.latitude!!, amb.longitude!!)),
                            title = amb.licensePlate,
                            icon = createAmbulanceMarker(context, R.drawable.ambulance),
                            onClick = {
                                selectedAmbulance = amb
                                true
                            },
                        )
                    }
                }
            }

            if (displayableAmbulances.isEmpty()) {
                val showLoading = state.isRefreshingAmbulances || !state.hasLoadedAmbulancesOnce
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (showLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp,
                                color = BrandBlueDark,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Locating ambulances…",
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark,
                            )
                        } else {
                            Icon(
                                Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = BrandBlueDark.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No ambulances available",
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Waiting for one to come online…",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                }
            }

            ConnectivityPill(
                connected = state.isSocketConnected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
            )
        }
    }

    val selected = selectedAmbulance
    if (selected != null && call != null) {
        AmbulanceAssignDialog(
            ambulance = selected,
            isAssigning = state.isAssigning,
            onDismiss = { selectedAmbulance = null },
            onConfirm = {
                viewModel.assignAmbulance(call.callId, selected.id) {
                    selectedAmbulance = null
                    onAssigned()
                }
            },
        )
    }
}

@Composable
private fun ConnectivityPill(connected: Boolean, modifier: Modifier = Modifier) {
    val (label, color) = if (connected) "Live" to Color(0xFF16A34A) else "Offline" to Color(0xFFEF4444)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AmbulanceAssignDialog(
    ambulance: DispatcherAmbulanceSummary,
    isAssigning: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isAssigning) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isAssigning, dismissOnClickOutside = !isAssigning),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = BrandBlueDark,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ambulance.licensePlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = BrandBlueDark,
                        )
                        ambulance.vehicleModel?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (ambulance.driverOnline) Color(0xFF16A34A) else Color(0xFFF59E0B),
                                CircleShape,
                            ),
                    )
                }
                if (!ambulance.driverOnline) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Driver app is closed — delivery via push only",
                        fontSize = 12.sp,
                        color = Color(0xFFB45309),
                    )
                }
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isAssigning,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = BrandBlueDark,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isAssigning,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlueDark,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isAssigning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text("Assign", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
