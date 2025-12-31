@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.decorations.VerifyNumberBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
import com.example.emergencynow.ui.theme.PrimaryDarkBlue
import com.example.emergencynow.ui.theme.PrimaryDarkerBlue
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with curved shapes
        VerifyNumberBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // Status bar placeholder
            Spacer(Modifier.height(48.dp))
            
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Title and description
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verify\nYour Number",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryDarkBlue,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BrandBlueMid)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Enter the 6-digit code sent to your device to secure your account.",
                    fontSize = 16.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 26.sp
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Code input field with label
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input field box (drawn first, so it's behind the label)
                BasicTextField(
                    value = state.code,
                    onValueChange = { 
                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                            viewModel.onAction(VerifyCodeAction.OnCodeChanged(it))
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        letterSpacing = 10.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 2.dp,
                                    color = BrandBlueMid,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 16.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )
                
                // Label floating above the border (drawn on top, overlapping the border)
                Text(
                    text = "Code",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueMid,
                    modifier = Modifier
                        .offset(x = 16.dp, y = (-10).dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Verify button
            Button(
                onClick = { viewModel.onAction(VerifyCodeAction.OnVerifyClicked) },
                enabled = state.code.length == 6 && state.code.all { it.isDigit() } && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = PrimaryDarkBlue.copy(alpha = 0.3f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDarkBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Resend Code button
            TextButton(
                onClick = { viewModel.onAction(VerifyCodeAction.OnResendClicked) },
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Resend Code",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDarkBlue
                )
            }
            
            // Error message
            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
        }
    }
}
