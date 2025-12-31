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
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        EnterEgnBackground(modifier = Modifier.fillMaxSize())
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
            
            // Content
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

@Composable
fun CallHistoryItem(call: Call) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CurvePaleBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = call.description,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark,
                    modifier = Modifier.weight(1f)
                )
                CallStatusChip(status = call.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatDate(call.createdAt),
                fontSize = 14.sp,
                color = BrandBlueDark.copy(alpha = 0.7f)
            )
            
            if (call.dispatchedAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dispatched: ${formatDate(call.dispatchedAt)}",
                    fontSize = 12.sp,
                    color = BrandBlueDark.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CallStatusChip(status: CallStatus) {
    val (color, text) = when (status) {
        CallStatus.PENDING -> Color(0xFFFFA726) to "Pending"
        CallStatus.DISPATCHED -> Color(0xFF42A5F5) to "Dispatched"
        CallStatus.EN_ROUTE -> Color(0xFF66BB6A) to "En Route"
        CallStatus.ARRIVED -> Color(0xFF26A69A) to "Arrived"
        CallStatus.NAVIGATING_TO_HOSPITAL -> Color(0xFF7E57C2) to "To Hospital"
        CallStatus.COMPLETED -> Color(0xFF66BB6A) to "Completed"
        CallStatus.CANCELLED -> Color(0xFFEF5350) to "Cancelled"
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
