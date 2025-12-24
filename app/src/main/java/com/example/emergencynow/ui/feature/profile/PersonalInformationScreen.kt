@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

enum class Gender { Male, Female, Other }

@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: PersonalInformationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Health Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { inner ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text("This information will be shared securely with first responders in an emergency.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.height,
                        onValueChange = { viewModel.updateHeight(it) },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.weight,
                        onValueChange = { viewModel.updateWeight(it) },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Gender", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                SegmentedButton(
                    options = listOf("Male", "Female", "Other"),
                    selected = uiState.gender.replaceFirstChar { it.uppercase() },
                    onSelected = {
                        viewModel.updateGender(it.lowercase())
                    }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.allergies,
                    onValueChange = { viewModel.updateAllergies(it) },
                    label = { Text("Allergies (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(Modifier.height(24.dp))
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        viewModel.saveProfile(onSuccess = onContinue)
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) { 
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (uiState.isEditMode) "Save Changes" else "Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedButton(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            FilterChip(
                selected = selected == opt,
                onClick = { onSelected(opt) },
                label = { Text(opt) }
            )
        }
    }
}
