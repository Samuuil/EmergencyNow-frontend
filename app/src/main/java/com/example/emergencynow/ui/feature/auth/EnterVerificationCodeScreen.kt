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
import com.example.emergencynow.ui.extention.BackendClient
import com.example.emergencynow.ui.extention.VerifyCodeRequest
import kotlinx.coroutines.launch

@Composable
fun EnterVerificationCodeScreen(
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val isValid = code.length == 6 && code.all { it.isDigit() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Your Number") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter the 6-digit code sent to your device.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) code = it },
                label = { Text("Code") },
                placeholder = { Text("000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isLoading) {
                        return@Button
                    }
                    val currentEgn = AuthSession.egn
                    if (currentEgn.isNullOrEmpty()) {
                        error = "Missing EGN. Go back and enter it again."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            val response = BackendClient.api.verifyCode(
                                VerifyCodeRequest(
                                    egn = currentEgn,
                                    code = code
                                )
                            )
                            AuthSession.accessToken = response.accessToken
                            AuthSession.refreshToken = response.refreshToken
                            onVerified()
                        } catch (e: Exception) {
                            error = "Invalid or expired verification code."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Verify")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { /* TODO resend */ }) { Text("Resend") }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
