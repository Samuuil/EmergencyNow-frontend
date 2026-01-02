package com.example.emergencynow.ui.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.ui.theme.BrandBlueDark
import com.example.emergencynow.ui.theme.CurvePaleBlue
import java.text.SimpleDateFormat
import java.util.Locale

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


