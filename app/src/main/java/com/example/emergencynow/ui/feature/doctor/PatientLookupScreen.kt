@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.buttons.PrimaryButton
import com.example.emergencynow.ui.components.decorations.ProfileGeometricBackground
import com.example.emergencynow.ui.components.inputs.PrimaryTextField

@Composable
fun PatientLookupScreen(
    onBack: () -> Unit,
    onLookup: (String) -> Unit
) {
    var egn by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileGeometricBackground(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Patient Lookup") },
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
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Enter Patient EGN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Enter the patient's EGN to view their medical profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(Modifier.height(16.dp))
                
                PrimaryTextField(
                    value = egn,
                    onValueChange = { 
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            egn = it
                            error = null
                        }
                    },
                    label = "EGN",
                    placeholder = "Enter 10-digit EGN",
                    keyboardType = KeyboardType.Number
                )
                
                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                PrimaryButton(
                    text = "Lookup Patient",
                    onClick = {
                        if (egn.length == 10) {
                            onLookup(egn)
                        } else {
                            error = "Please enter a valid 10-digit EGN"
                        }
                    },
                    enabled = egn.length == 10
                )
            }
        }
    }
}
