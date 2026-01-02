@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.emergencynow.ui.components.buttons.PrimaryButton
import com.example.emergencynow.ui.components.decorations.EnterEgnBackground
import com.example.emergencynow.ui.components.inputs.GenderSelector
import com.example.emergencynow.ui.feature.profile.BloodTypeSelector
import com.example.emergencynow.ui.feature.profile.DateInputField
import com.example.emergencynow.ui.feature.profile.MultilineTextArea
import com.example.emergencynow.ui.feature.profile.NumberInputWithUnit
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import org.koin.androidx.compose.koinViewModel

enum class Gender { Male, Female, Other }

@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: PersonalInformationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        EnterEgnBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Your Health Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(24.dp))
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CurvePaleBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Security,
                                contentDescription = null,
                                tint = BrandBlueDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "This information will be shared securely with first responders in an emergency to help them provide better care.",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = BrandBlueDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Height",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandBlueDark.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            NumberInputWithUnit(
                                value = uiState.height,
                                onValueChange = { viewModel.updateHeight(it) },
                                unit = "cm"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weight",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandBlueDark.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            NumberInputWithUnit(
                                value = uiState.weight,
                                onValueChange = { viewModel.updateWeight(it) },
                                unit = "kg"
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Gender",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    GenderSelector(
                        selectedGender = uiState.gender,
                        onGenderSelected = { viewModel.updateGender(it) }
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Blood Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "(Optional)",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    BloodTypeSelector(
                        selectedBloodType = uiState.bloodType,
                        onBloodTypeSelected = { viewModel.updateBloodType(it) }
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Date of Birth",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "(Optional)",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    DateInputField(
                        value = uiState.dateOfBirth,
                        onValueChange = { viewModel.updateDateOfBirth(it) }
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Allergies",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "(Optional)",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    MultilineTextArea(
                        value = uiState.allergies,
                        onValueChange = { viewModel.updateAllergies(it) },
                        placeholder = "Penicillin, Peanuts, Latex..."
                    )
                    Text(
                        text = "Separate multiple allergies with commas.",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Chronic Illnesses",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "(Optional)",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    MultilineTextArea(
                        value = uiState.illnesses,
                        onValueChange = { viewModel.updateIllnesses(it) },
                        placeholder = "Diabetes, Asthma, Hypertension..."
                    )
                    Text(
                        text = "Separate multiple illnesses with commas.",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = "Current Medications",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "(Optional)",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    MultilineTextArea(
                        value = uiState.medicines,
                        onValueChange = { viewModel.updateMedicines(it) },
                        placeholder = "Aspirin, Insulin, Metformin..."
                    )
                    Text(
                        text = "Separate multiple medications with commas.",
                        fontSize = 12.sp,
                        color = BrandBlueDark.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                    
                    if (uiState.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Button(
                        onClick = { viewModel.saveProfile(onSuccess = onContinue) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = BrandBlueDark.copy(alpha = 0.2f)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlueDark,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (uiState.isEditMode) "Save Changes" else "Continue",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (uiState.isSaving) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
        
        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

