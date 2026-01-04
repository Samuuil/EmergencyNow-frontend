package com.example.emergencynow.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.ui.components.decorations.EnterEgnBackground
import com.example.emergencynow.ui.feature.history.CallHistoryItem
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        EnterEgnBackground(modifier = Modifier.fillMaxSize())
        
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlueDark
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Call History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandBlueDark
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading call history",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            fontSize = 16.sp,
                            color = BrandBlueDark.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlueDark
                            )
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
                uiState.calls.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Call History",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your emergency call history will appear here",
                            fontSize = 16.sp,
                            color = BrandBlueDark.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.calls) { call ->
                            CallHistoryItem(call = call)
                        }
                    }
                }
                }
            }
        }
    }
}

