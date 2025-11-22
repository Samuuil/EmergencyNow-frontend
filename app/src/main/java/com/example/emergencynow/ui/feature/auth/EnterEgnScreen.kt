@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

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
import com.example.emergencynow.ui.extention.AuthSession

@Composable
fun EnterEgnScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    var egn by remember { mutableStateOf("") }
    val isValid = egn.length == 10 && egn.all { it.isDigit() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log In or Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Enter Your EGN", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("We use your EGN to securely identify you within the medical system.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = egn,
                    onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) egn = it },
                    label = { Text("EGN") },
                    placeholder = { Text("10 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* TODO: explain why required */ }) { Text("Why is this required?") }
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        AuthSession.egn = egn
                        onContinue()
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Continue")
                }
                TextButton(onClick = { /* TODO: help */ }) { Text("Need Help?") }
            }
        }
    }
}
