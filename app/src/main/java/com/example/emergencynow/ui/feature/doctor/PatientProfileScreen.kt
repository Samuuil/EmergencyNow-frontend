@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencynow.ui.components.decorations.ProfileGeometricBackground
import org.koin.androidx.compose.koinViewModel

@Composable
fun PatientProfileScreen(
    egn: String,
    onBack: () -> Unit,
    viewModel: PatientProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(egn) {
        viewModel.loadPatientProfile(egn)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileGeometricBackground(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Patient Medical Profile") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(20.dp),
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
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                uiState.profile != null -> {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val profile = uiState.profile!!
                        
                        Text(
                            text = "EGN: $egn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        ProfileInfoCard(title = "Physical Information") {
                            InfoRow("Height", "${profile.height} cm")
                            InfoRow("Weight", "${profile.weight} kg")
                            InfoRow("Gender", profile.gender.name)
                            if (profile.dateOfBirth != null) {
                                InfoRow("Date of Birth", profile.dateOfBirth!!)
                            }
                            if (profile.bloodType != null) {
                                InfoRow("Blood Type", profile.bloodType!!)
                            }
                        }
                        
                        if (!profile.allergies.isNullOrEmpty()) {
                            ProfileInfoCard(title = "Allergies") {
                                profile.allergies!!.forEach { allergy ->
                                    Text("• $allergy", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        if (!profile.illnesses.isNullOrEmpty()) {
                            ProfileInfoCard(title = "Chronic Illnesses") {
                                profile.illnesses!!.forEach { illness ->
                                    Text("• $illness", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        if (!profile.medicines.isNullOrEmpty()) {
                            ProfileInfoCard(title = "Current Medications") {
                                profile.medicines!!.forEach { medicine ->
                                    Text("• $medicine", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
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
