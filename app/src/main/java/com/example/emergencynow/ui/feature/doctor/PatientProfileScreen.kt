@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.doctor

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencynow.ui.components.decorations.ChooseVerificationBackground
import com.example.emergencynow.ui.components.cards.InfoRow
import com.example.emergencynow.ui.components.cards.ProfileInfoCard
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.CurvePaleBlue
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
        ChooseVerificationBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Patient Medical Profile",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlueDark,
                lineHeight = 44.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrandBlueMid)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandBlueDark)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error ?: "Unknown error",
                                fontSize = 16.sp,
                                color = BrandBlueDark.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                uiState.profile != null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val profile = uiState.profile!!
                        
                        Text(
                            text = "EGN: $egn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ProfileInfoCard(
                            title = "Physical Information",
                            content = {
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
                        )
                        
                        if (!profile.allergies.isNullOrEmpty()) {
                            ProfileInfoCard(
                                title = "Allergies",
                                content = {
                                    profile.allergies!!.forEach { allergy ->
                                        Text(
                                            "• $allergy",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
                                        )
                                    }
                                }
                            )
                        }
                        
                        if (!profile.illnesses.isNullOrEmpty()) {
                            ProfileInfoCard(
                                title = "Chronic Illnesses",
                                content = {
                                    profile.illnesses!!.forEach { illness ->
                                        Text(
                                            "• $illness",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
                                        )
                                    }
                                }
                            )
                        }
                        
                        if (!profile.medicines.isNullOrEmpty()) {
                            ProfileInfoCard(
                                title = "Current Medications",
                                content = {
                                    profile.medicines!!.forEach { medicine ->
                                        Text(
                                            "• $medicine",
                                            fontSize = 14.sp,
                                            color = BrandBlueDark
                                        )
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

