@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.buttons.PrimaryButton
import com.example.emergencynow.ui.components.decorations.AlternativeGeometricBackground
import com.example.emergencynow.ui.components.inputs.PrimaryTextField
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AlternativeGeometricBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top bar
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Log In or Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Title and description
            Text(
                text = "Enter Your EGN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "We use your EGN to securely identify you within the medical system.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Input field
            PrimaryTextField(
                value = state.egn,
                onValueChange = { 
                    if (it.all { ch -> ch.isDigit() }) {
                        viewModel.onAction(EnterEgnAction.OnEgnChanged(it))
                    }
                },
                label = "EGN",
                placeholder = "10 digits",
                keyboardType = KeyboardType.Number,
                enabled = !state.isLoading,
                maxLength = 10
            )
            
            Spacer(Modifier.weight(1f))
            
            // Continue button
            PrimaryButton(
                text = if (state.isLoading) "" else "Continue",
                onClick = { viewModel.onAction(EnterEgnAction.OnContinueClicked) },
                enabled = state.egn.length == 10 && !state.isLoading,
                backgroundColor = if (state.egn.length == 10 && !state.isLoading) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                textColor = if (state.egn.length == 10 && !state.isLoading)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
