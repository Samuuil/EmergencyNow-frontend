package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.emergencynow.ui.theme.*

@Composable
fun VerifyNumberBackground(
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
                drawTopRightShape(width, height)
                
                drawTopLeftShape(width, height)
                
                drawBottomShape(width, height)
            } else {
                drawTopRightShape(width, height, darkMode = true)
                drawTopLeftShape(width, height, darkMode = true)
                drawBottomShape(width, height, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawTopRightShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.1f else 0.3f
    val color = Color(0xFF93C5FD).copy(alpha = opacity)
    
    drawOval(
        color = color,
        topLeft = Offset(width * 0.3f, -height * 0.1f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 0.9f,
            height = height * 0.55f
        )
    )
}

private fun DrawScope.drawTopLeftShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.1f else 0.4f
    val color = Color(0xFFBFDBFE).copy(alpha = opacity)
    
    drawOval(
        color = color,
        topLeft = Offset(-width * 0.1f, -height * 0.15f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 0.7f,
            height = height * 0.45f
        )
    )
}

private fun DrawScope.drawBottomShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.05f else 0.6f
    val color = if (darkMode) {
        Color(0xFF1E3A8A).copy(alpha = opacity)
    } else {
        Color(0xFFEFF6FF).copy(alpha = opacity)
    }
    
    drawOval(
        color = color,
        topLeft = Offset(-width * 0.2f, height * 0.7f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 1.4f,
            height = height * 0.35f
        )
    )
}

