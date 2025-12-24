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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.emergencynow.ui.util.AuthSession
import org.koin.androidx.compose.koinViewModel

@Composable
fun EnterEgnScreen(
    onBack: () -> Unit,
    onContinue: (egn: String) -> Unit,
    viewModel: EnterEgnViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.shouldNavigateToVerification) {
        if (state.shouldNavigateToVerification) {
            AuthSession.egn = state.egn
            onContinue(state.egn)
            viewModel.onAction(EnterEgnAction.OnNavigationHandled)
        }
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(EnterEgnAction.OnErrorDismissed) },
            title = { Text("Error") },
            text = { Text(state.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(EnterEgnAction.OnErrorDismissed) }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log In or Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    value = state.egn,
                    onValueChange = { 
                        if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                            viewModel.onAction(EnterEgnAction.OnEgnChanged(it))
                        }
                    },
                    label = { Text("EGN") },
                    placeholder = { Text("10 digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* TODO: explain why required */ }) { Text("Why is this required?") }
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { viewModel.onAction(EnterEgnAction.OnContinueClicked) },
                    enabled = state.egn.length == 10 && state.egn.all { it.isDigit() } && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Continue")
                    }
                }
                TextButton(onClick = { /* TODO: help */ }) { Text("Need Help?") }
            }
        }
    }
}
