package com.example.emergencynow.ui.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.usecase.profile.GetProfileByEgnUseCase
import com.example.emergencynow.ui.components.cards.InfoRow
import com.example.emergencynow.ui.components.cards.ProfileInfoCard
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.util.CallOffer
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun IncomingCallDialog(
    offer: CallOffer,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
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
        Spacer(modifier = Modifier.width(8.dp))
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
fun BottomNavItem(
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) BrandBlueDark else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun HospitalSelectionDialog(
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
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                hospital.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandBlueDark
                                            )
                                            if (hospital.distance != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
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
fun PatientProfileDialog(
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
                                Spacer(modifier = Modifier.height(8.dp))
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
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            ProfileInfoCard(
                                title = "Physical Information",
                                content = {
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
                            )
                            
                            if (!loadedProfile.allergies.isNullOrEmpty()) {
                                ProfileInfoCard(
                                    title = "Allergies",
                                    content = {
                                        loadedProfile.allergies!!.forEach { allergy ->
                                            Text(
                                                "• $allergy",
                                                fontSize = 14.sp,
                                                color = BrandBlueDark
                                            )
                                        }
                                    }
                                )
                            }
                            
                            if (!loadedProfile.illnesses.isNullOrEmpty()) {
                                ProfileInfoCard(
                                    title = "Chronic Illnesses",
                                    content = {
                                        loadedProfile.illnesses!!.forEach { illness ->
                                            Text(
                                                "• $illness",
                                                fontSize = 14.sp,
                                                color = BrandBlueDark
                                            )
                                        }
                                    }
                                )
                            }
                            
                            if (!loadedProfile.medicines.isNullOrEmpty()) {
                                ProfileInfoCard(
                                    title = "Current Medications",
                                    content = {
                                        loadedProfile.medicines!!.forEach { medicine ->
                                            Text(
                                                "• $medicine",
                                                fontSize = 14.sp,
                                                color = BrandBlueDark
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

