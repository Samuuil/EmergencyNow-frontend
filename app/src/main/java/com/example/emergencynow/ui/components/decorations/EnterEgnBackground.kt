package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.emergencynow.ui.theme.*

@Composable
fun EnterEgnBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) BackgroundDark else Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            if (!isDarkMode) {
                // Main blob at top - large gradient circle
                drawMainBlobTop(width, height)
                
                // Bottom blob - rotated ellipse at bottom left
                drawBottomBlob(width, height)
            } else {
                // Darker version for dark mode
                drawMainBlobTop(width, height, darkMode = true)
                drawBottomBlob(width, height, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawMainBlobTop(width: Float, height: Float, darkMode: Boolean = false) {
    // Position: top: -15%, left: -25%, width: 140%, padding-bottom: 140%
    // Gradient from #BFDBFE to #EFF6FF
    val opacity = if (darkMode) 0.2f else 1.0f
    val gradient = Brush.linearGradient(
        colors = listOf(
            EgnBlobDark.copy(alpha = opacity),
            EgnBlobLight.copy(alpha = opacity)
        ),
        start = Offset(0f, 0f),
        end = Offset(width * 1.4f, height * 1.4f)
    )
    
    // Draw large circle at top left
    drawCircle(
        brush = gradient,
        radius = width * 0.7f, // 140% width becomes 70% radius
        center = Offset(width * 0.45f, -height * 0.15f) // Adjusted to account for left: -25%
    )
}

private fun DrawScope.drawBottomBlob(width: Float, height: Float, darkMode: Boolean = false) {
    // Position: bottom: -10%, left: -20%, width: 100%, height: 40%
    // border-radius: 50% 50% 0 0 / 30% 30% 0 0
    // transform: rotate(-10deg)
    // Background: #DBEAFE, opacity: 0.5
    val opacity = if (darkMode) 0.15f else 0.5f
    
    rotate(degrees = -10f, pivot = Offset(width * 0.3f, height * 0.95f)) {
        // Draw ellipse at bottom left with specific border radius
        drawOval(
            color = EgnBlobBottom.copy(alpha = opacity),
            topLeft = Offset(-width * 0.2f, height * 0.7f),
            size = androidx.compose.ui.geometry.Size(
                width = width * 1.0f,
                height = height * 0.4f
            )
        )
    }
}

