@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.domain.model.request.LoginMethod
import com.example.emergencynow.domain.usecase.auth.RequestVerificationCodeUseCase
import com.example.emergencynow.ui.components.cards.SelectionCard
import com.example.emergencynow.ui.components.decorations.AlternativeGeometricBackground
import com.example.emergencynow.ui.util.AuthSession
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ChooseVerificationMethodScreen(
    onBack: () -> Unit,
    onPhone: () -> Unit,
    onEmail: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val requestVerificationCodeUseCase: RequestVerificationCodeUseCase = koinInject()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AlternativeGeometricBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top bar
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, enabled = !isLoading) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Verify Your Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Description
            Text(
                text = "Choose how to receive your verification code.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Phone option
            SelectionCard(
                title = "Send to Phone Number",
                subtitle = "••• ••• ••89",
                onClick = {
                    val currentEgn = AuthSession.egn
                    if (currentEgn.isNullOrEmpty() || isLoading) {
                        error = "Missing EGN. Go back and enter it again."
                        return@SelectionCard
                    }
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            AuthSession.lastMethod = LoginMethod.SMS
                            requestVerificationCodeUseCase(egn = currentEgn, method = "sms").getOrThrow()
                            onPhone()
                        } catch (e: Exception) {
                            error = "Failed to send verification code."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Email option
            SelectionCard(
                title = "Send to Email",
                subtitle = "j•••@email.com",
                onClick = {
                    val currentEgn = AuthSession.egn
                    if (currentEgn.isNullOrEmpty() || isLoading) {
                        error = "Missing EGN. Go back and enter it again."
                        return@SelectionCard
                    }
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            AuthSession.lastMethod = LoginMethod.EMAIL
                            requestVerificationCodeUseCase(egn = currentEgn, method = "email").getOrThrow()
                            onEmail()
                        } catch (e: Exception) {
                            error = "Failed to send verification code."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            )
            
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
            
            Spacer(Modifier.weight(1f))
        }

        // Fullscreen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
