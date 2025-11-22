@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.emergencynow.ui.extention.AuthSession
import com.example.emergencynow.ui.extention.BackendClient
import com.example.emergencynow.ui.extention.InitiateLoginRequest
import com.example.emergencynow.ui.extention.LoginMethod
import kotlinx.coroutines.launch

@Composable
fun ChooseVerificationMethodScreen(
    onBack: () -> Unit,
    onPhone: () -> Unit,
    onEmail: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Your Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Choose how to receive your verification code.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val currentEgn = AuthSession.egn
                        if (currentEgn.isNullOrEmpty() || isLoading) {
                            error = "Missing EGN. Go back and enter it again."
                            return@clickable
                        }
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                AuthSession.lastMethod = LoginMethod.SMS
                                BackendClient.api.initiateLogin(
                                    InitiateLoginRequest(
                                        egn = currentEgn,
                                        method = LoginMethod.SMS
                                    )
                                )
                                onPhone()
                            } catch (e: Exception) {
                                error = "Failed to send verification code."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Send to Phone Number", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("••• •••-••89", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val currentEgn = AuthSession.egn
                        if (currentEgn.isNullOrEmpty() || isLoading) {
                            error = "Missing EGN. Go back and enter it again."
                            return@clickable
                        }
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                AuthSession.lastMethod = LoginMethod.EMAIL
                                BackendClient.api.initiateLogin(
                                    InitiateLoginRequest(
                                        egn = currentEgn,
                                        method = LoginMethod.EMAIL
                                    )
                                )
                                onEmail()
                            } catch (e: Exception) {
                                error = "Failed to send verification code. LoginMethod.EMAIL"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Send to Email", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("j•••@email.com", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Add Another Method", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.weight(1f))
                    Text("Not configured", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
