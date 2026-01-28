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
                drawBlueShapeTop(width, height)
                
                drawBlueShapeHighlight(width, height)
                
                drawBlueShapeMiddleAccent(width, height)

                drawBlueShapeBottomAccent(width, height)

                drawBlueShapeBottom(width, height)
            } else {
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
    drawCircle(
        color = CurveDarkBlue.copy(alpha = 0.1f),
        radius = width * 0.2f,
        center = Offset(width * 0.4f, height * 0.05f)
    )
}

private fun DrawScope.drawBlueShapeMiddleAccent(width: Float, height: Float) {
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

