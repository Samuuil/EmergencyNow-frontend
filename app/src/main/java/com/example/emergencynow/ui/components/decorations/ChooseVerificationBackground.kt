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
fun ChooseVerificationBackground(
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
                drawTopLeftShape(width, height)
                
                drawTopRightShape(width, height)
                
                drawBottomRightShape(width, height)
                
                drawBottomLeftShape(width, height)
            } else {
                drawTopLeftShape(width, height, darkMode = true)
                drawTopRightShape(width, height, darkMode = true)
                drawBottomRightShape(width, height, darkMode = true)
                drawBottomLeftShape(width, height, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawTopLeftShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.3f else 1.0f
    val color = if (darkMode) {
        Color(0xFF1E3A8A).copy(alpha = opacity)
    } else {
        Color(0xFFBFDBFE).copy(alpha = opacity)
    }
    
    drawOval(
        color = color,
        topLeft = Offset(-width * 0.3f, -height * 0.15f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 1.0f,
            height = height * 0.6f
        )
    )
}

private fun DrawScope.drawTopRightShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.4f else 0.5f
    val color = if (darkMode) {
        Color(0xFF1E40AF).copy(alpha = opacity)
    } else {
        Color(0xFF60A5FA).copy(alpha = opacity)
    }
    
    drawOval(
        color = color,
        topLeft = Offset(width * 0.5f, height * 0.1f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 0.7f,
            height = height * 0.5f
        )
    )
}

private fun DrawScope.drawBottomRightShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.4f else 1.0f
    val color = if (darkMode) {
        Color(0xFF1E3A8A).copy(alpha = opacity)
    } else {
        Color(0xFFBFDBFE).copy(alpha = opacity)
    }
    
    drawOval(
        color = color,
        topLeft = Offset(width * 0.25f, height * 0.6f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 0.9f,
            height = height * 0.5f
        )
    )
}

private fun DrawScope.drawBottomLeftShape(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.2f else 1.0f
    val color = if (darkMode) {
        Color(0xFF1E40AF).copy(alpha = opacity)
    } else {
        Color(0xFFDBEAFE).copy(alpha = opacity)
    }
    
    drawOval(
        color = color,
        topLeft = Offset(-width * 0.1f, height * 0.7f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 0.5f,
            height = height * 0.3f
        )
    )
}

