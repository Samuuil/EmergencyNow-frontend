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
import androidx.compose.material.icons.filled.ArrowBack
enum class Gender { Male, Female, Other }

@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.Male) }
    var allergiesInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Your Health Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { inner ->
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
                    value = height,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) weight = it },
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
                selected = gender.name,
                onSelected = {
                    gender = when (it) {
                        "Female" -> Gender.Female
                        "Other" -> Gender.Other
                        else -> Gender.Male
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = allergiesInput,
                onValueChange = { allergiesInput = it },
                label = { Text("Allergies (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Continue") }
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
