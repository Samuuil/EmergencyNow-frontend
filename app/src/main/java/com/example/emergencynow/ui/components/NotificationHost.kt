package com.example.emergencynow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencynow.ui.util.NotificationManager
import com.example.emergencynow.ui.util.NotificationType
import org.koin.compose.koinInject

@Composable
fun NotificationHost(
    modifier: Modifier = Modifier,
    notificationManager: NotificationManager = koinInject()
) {
    val notification by notificationManager.notifications.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding() // Avoid overlapping status bar
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            notification?.let { notif ->
                val backgroundColor = when (notif.type) {
                    NotificationType.Error -> Color(0xFFD64545) // Deep, slightly pastel Red
                    NotificationType.Success -> Color(0xFF4CAF50)
                    NotificationType.Info -> Color(0xFF2196F3)
                }
                
                val icon = when (notif.type) {
                    NotificationType.Error -> Icons.Filled.Error
                    NotificationType.Success -> Icons.Filled.CheckCircle
                    NotificationType.Info -> Icons.Filled.Info
                }

                Row(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    Text(
                        text = notif.message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
