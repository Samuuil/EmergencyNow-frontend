package com.example.emergencynow.ui.feature.dispatcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.ui.feature.home.BottomNavItem
import com.example.emergencynow.ui.theme.BrandBlueDark
import org.koin.androidx.compose.koinViewModel

@Composable
fun DispatcherHomeScreen(
    onOpenProfile: () -> Unit,
    onOpenContacts: () -> Unit,
    onAssignCall: (String) -> Unit,
    onMakeEmergencyCall: () -> Unit,
    viewModel: DispatcherViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.connectSocket()
            try {
                awaitCancellation()
            } finally {
                viewModel.disconnectSocket()
            }
        }
    }

    val orderedCalls = state.calls.values.sortedBy { it.createdAt }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
    ) {
        StatusBar(
            isConnected = state.isSocketConnected,
            heldCount = state.calls.size,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Assigned calls (${state.calls.size})",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlueDark,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (orderedCalls.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(orderedCalls, key = { it.callId }) { call ->
                        DispatcherCallCard(
                            call = call,
                            isPending = state.pendingAssignments.contains(call.callId),
                            onAssign = { onAssignCall(call.callId) },
                        )
                    }
                }
            }
        }

        Button(
            onClick = onMakeEmergencyCall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        ) {
            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Emergency Call",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                BottomNavItem(
                    icon = Icons.Filled.Headset,
                    label = "Calls",
                    onClick = { },
                    isSelected = true,
                )
                BottomNavItem(
                    icon = Icons.Filled.Contacts,
                    label = "Contacts",
                    onClick = onOpenContacts,
                    isSelected = false,
                )
                BottomNavItem(
                    icon = Icons.Filled.Person,
                    label = "Profile",
                    onClick = onOpenProfile,
                    isSelected = false,
                )
            }
        }
    }
}

@Composable
private fun StatusBar(isConnected: Boolean, heldCount: Int) {
    val (label, color) = when {
        isConnected -> "Connected • $heldCount/5 calls" to Color(0xFF16A34A)
        else -> "Connecting…" to Color(0xFFF59E0B)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    label,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun DispatcherCallCard(
    call: DispatcherCallOffer,
    isPending: Boolean,
    onAssign: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalHospital,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        call.userName ?: "Unknown caller",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrandBlueDark,
                    )
                    Text(
                        formatRelative(call.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                call.description.ifBlank { "No description" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(16.dp))

            if (isPending) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = BrandBlueDark,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Waiting for driver…",
                        color = BrandBlueDark,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Button(
                    onClick = onAssign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlueDark,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Assign ambulance", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Headset,
            contentDescription = null,
            tint = BrandBlueDark.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No calls right now",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BrandBlueDark,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "You'll be notified when a call arrives.",
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}

private fun formatRelative(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val created = java.time.Instant.parse(iso)
        val now = java.time.Instant.now()
        val seconds = java.time.Duration.between(created, now).seconds
        when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60} min ago"
            seconds < 86400 -> "${seconds / 3600} h ago"
            else -> "${seconds / 86400} d ago"
        }
    } catch (_: Exception) {
        ""
    }
}
