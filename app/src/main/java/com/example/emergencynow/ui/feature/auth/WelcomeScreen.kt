package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onRegisterEgn: () -> Unit,
    onLogin: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Emergency Now",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Fast emergency response when you need it most",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onRegisterEgn,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Register EGN")
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Log In")
            }
        }
    }
}
