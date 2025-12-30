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
import androidx.compose.ui.unit.dp
import com.example.emergencynow.ui.theme.*

@Composable
fun WelcomeScreenBackground(
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
                // Blue shape top - large curved shape at top left
                drawBlueShapeTop(width, height)
                
                // Blue shape highlight - small blur effect at top
                drawBlueShapeHighlight(width, height)
                
                // Blue shape middle accent - circular accent in middle right
                drawBlueShapeMiddleAccent(width, height)
                
                // Blue shape bottom accent - gradient at bottom right
                drawBlueShapeBottomAccent(width, height)
                
                // Blue shape bottom - large pale circle at bottom right
                drawBlueShapeBottom(width, height)
            } else {
                // Darker version for dark mode
                drawBlueShapeTop(width, height, darkMode = true)
                drawBlueShapeBottom(width, height, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawBlueShapeTop(width: Float, height: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.2f else 0.6f
    val gradient = Brush.linearGradient(
        colors = listOf(
            CurveLightBlue.copy(alpha = opacity),
            CurveMediumBlue.copy(alpha = opacity)
        ),
        start = Offset(width * 0.2f, 0f),
        end = Offset(width * 0.8f, height * 0.5f)
    )
    
    // Position: top: -15%, left: -35%, width: 110%, height: 70%
    // Create an ellipse rotated -10 degrees
    rotate(degrees = -10f, pivot = Offset(width * 0.2f, height * 0.2f)) {
        drawOval(
            brush = gradient,
            topLeft = Offset(-width * 0.35f, -height * 0.15f),
            size = androidx.compose.ui.geometry.Size(
                width = width * 1.1f,
                height = height * 0.7f
            )
        )
    }
}

private fun DrawScope.drawBlueShapeHighlight(width: Float, height: Float) {
    // Position: top: -5%, left: 20%, width: 40%, height: 20%
    // Small blurred circle
    drawCircle(
        color = CurveDarkBlue.copy(alpha = 0.1f),
        radius = width * 0.2f, // Approximating width/height as radius
        center = Offset(width * 0.4f, height * 0.05f)
    )
}

private fun DrawScope.drawBlueShapeMiddleAccent(width: Float, height: Float) {
    // Position: top: 20%, right: -20%, width: 80%, height: 50%
    // Radial gradient circle with blur effect
    val gradient = Brush.radialGradient(
        colors = listOf(
            CurveDeepBlue.copy(alpha = 0.15f),
            CurveDarkBlue.copy(alpha = 0.15f)
        ),
        center = Offset(width * 1.1f, height * 0.45f),
        radius = width * 0.4f
    )
    
    rotate(degrees = 15f, pivot = Offset(width * 1.1f, height * 0.45f)) {
        drawCircle(
            brush = gradient,
            radius = width * 0.5f,
            center = Offset(width * 1.1f, height * 0.45f)
        )
    }
}

private fun DrawScope.drawBlueShapeBottomAccent(width: Float, height: Float) {
    // Position: bottom: -30%, right: -10%, width: 100%, height: 50%
    // Gradient ellipse
    val gradient = Brush.linearGradient(
        colors = listOf(
            CurveLightBlueAccent.copy(alpha = 0.5f),
            CurvePaleBlueBottom.copy(alpha = 0.5f)
        ),
        start = Offset(width * 0.5f, height * 0.9f),
        end = Offset(width * 1.2f, height * 1.1f)
    )
    
    drawOval(
        brush = gradient,
        topLeft = Offset(width * 0.4f, height * 0.8f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 1.0f,
            height = height * 0.5f
        )
    )
}

private fun DrawScope.drawBlueShapeBottom(width: Float, height: Float, darkMode: Boolean = false) {
    // Position: bottom: -25%, right: -25%, width: 130%, height: 65%
    // Large pale circle at bottom right
    val opacity = if (darkMode) 0.15f else 0.9f
    drawOval(
        color = CurvePaleBlue.copy(alpha = opacity),
        topLeft = Offset(width * 0.2f, height * 0.8f),
        size = androidx.compose.ui.geometry.Size(
            width = width * 1.3f,
            height = height * 0.65f
        )
    )
}

