@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.emergencynow.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.components.decorations.EnterEgnBackground
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.BrandBlueMid
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



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        EnterEgnBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Log In or Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 40.dp)
                )
                
                Spacer(Modifier.width(40.dp))
            }
            
            Spacer(Modifier.height(32.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter\nYour EGN",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandBlueDark,
                    lineHeight = 52.sp,
                    letterSpacing = (-1).sp
                )
                
                Spacer(Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandBlueMid)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "We use your EGN to securely identify you within the medical system.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    lineHeight = 28.sp
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            BasicTextField(
                value = state.egn,
                onValueChange = { 
                    if (it.all { ch -> ch.isDigit() } && it.length <= 10) {
                        viewModel.onAction(EnterEgnAction.OnEgnChanged(it))
                    }
                },
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    textAlign = TextAlign.Center,
                    letterSpacing = 6.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                color = BrandBlueMid,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                }
            )
            
            Spacer(Modifier.weight(1f))
            
            androidx.compose.material3.Button(
                onClick = { viewModel.onAction(EnterEgnAction.OnContinueClicked) },
                enabled = state.egn.length == 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0xFF1E3A8A).copy(alpha = 0.2f)
                    ),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = BrandBlueDark,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.height(32.dp))
        }
    }
}
