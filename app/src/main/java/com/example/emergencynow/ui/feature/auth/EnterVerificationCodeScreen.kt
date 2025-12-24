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
import org.koin.androidx.compose.koinViewModel

@Composable
fun EnterVerificationCodeScreen(
    egn: String,
    onBack: () -> Unit,
    onVerified: (isReturningUser: Boolean) -> Unit,
    viewModel: VerifyCodeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(egn) {
        viewModel.setEgn(egn)
    }

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onVerified(state.isReturningUser)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Your Number") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter the 6-digit code sent to your device.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = state.code,
                onValueChange = { 
                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                        viewModel.onAction(VerifyCodeAction.OnCodeChanged(it))
                    }
                },
                label = { Text("Code") },
                placeholder = { Text("000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.onAction(VerifyCodeAction.OnVerifyClicked) },
                enabled = state.code.length == 6 && state.code.all { it.isDigit() } && !state.isLoading,
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
                    Text("Verify")
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.onAction(VerifyCodeAction.OnResendClicked) },
                enabled = !state.isLoading
            ) { 
                Text("Resend") 
            }
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
