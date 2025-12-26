package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.emergencynow.ui.theme.GeoDark
import com.example.emergencynow.ui.theme.GeoBlue
import com.example.emergencynow.ui.theme.GeoYellow

@Composable
fun AlternativeGeometricBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val alpha = if (isDarkMode) 0.2f else 0.8f
    
    Box(modifier = modifier.fillMaxSize()) {
        // Top-right decoration with circles
        TopRightCircleDecoration(alpha = alpha)
        
        // Bottom-left decoration with circles
        BottomLeftCircleDecoration(alpha = alpha)
    }
}

@Composable
private fun TopRightCircleDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Canvas(
            modifier = Modifier.size(520.dp, 520.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Large circle background
            drawCircle(
                color = GeoBlue.copy(alpha = 0.2f * alpha),
                radius = 240.dp.toPx(),
                center = Offset(canvasWidth + 40.dp.toPx(), -40.dp.toPx())
            )
            
            // Smaller overlay circle
            drawCircle(
                color = GeoDark.copy(alpha = 0.15f * alpha),
                radius = 190.dp.toPx(),
                center = Offset(canvasWidth - 16.dp.toPx(), 16.dp.toPx())
            )
            
            // Rotated rectangles
            rotate(degrees = 30f, pivot = Offset(canvasWidth * 0.6f, canvasHeight * 0.08f)) {
                // Yellow bar
                drawRect(
                    color = GeoYellow.copy(alpha = 0.8f * alpha),
                    topLeft = Offset(canvasWidth * 0.4f, canvasHeight * 0.02f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.75f, canvasHeight * 0.06f)
                )
                
                // Thin blue line above yellow
                drawRect(
                    color = GeoDark.copy(alpha = 0.4f * alpha),
                    topLeft = Offset(canvasWidth * 0.35f, -canvasHeight * 0.02f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.75f, canvasHeight * 0.015f)
                )
            }
            
            // Small decorative circles
            drawCircle(
                color = GeoBlue.copy(alpha = alpha * 0.4f),
                radius = 2.5.dp.toPx(),
                center = Offset(canvasWidth * 0.18f, canvasHeight * 0.12f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha * 0.3f),
                radius = 2.dp.toPx(),
                center = Offset(canvasWidth * 0.12f, canvasHeight * 0.08f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha * 0.35f),
                radius = 3.dp.toPx(),
                center = Offset(canvasWidth * 0.25f, canvasHeight * 0.02f)
            )
        }
    }
}

@Composable
private fun BottomLeftCircleDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Canvas(
            modifier = Modifier.size(520.dp, 520.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Large circle background
            drawCircle(
                color = GeoBlue.copy(alpha = 0.2f * alpha),
                radius = 240.dp.toPx(),
                center = Offset(-40.dp.toPx(), canvasHeight + 40.dp.toPx())
            )
            
            // Rotated rectangles
            rotate(degrees = 30f, pivot = Offset(canvasWidth * 0.25f, canvasHeight * 0.85f)) {
                // Yellow bar
                drawRect(
                    color = GeoYellow.copy(alpha = 0.8f * alpha),
                    topLeft = Offset(canvasWidth * 0.05f, canvasHeight * 0.82f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.65f, canvasHeight * 0.06f)
                )
                
                // Dark blue bar
                drawRect(
                    color = GeoDark.copy(alpha = 0.8f * alpha),
                    topLeft = Offset(canvasWidth * 0.08f, canvasHeight * 0.92f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.7f, canvasHeight * 0.07f)
                )
            }
            
            // Small decorative circles
            drawCircle(
                color = GeoBlue.copy(alpha = alpha * 0.4f),
                radius = 2.5.dp.toPx(),
                center = Offset(canvasWidth * 0.13f, canvasHeight * 0.95f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha * 0.35f),
                radius = 2.dp.toPx(),
                center = Offset(canvasWidth * 0.08f, canvasHeight * 0.88f)
            )
        }
    }
}


