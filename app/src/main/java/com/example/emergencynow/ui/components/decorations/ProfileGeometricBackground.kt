package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.emergencynow.ui.theme.GeoDark
import com.example.emergencynow.ui.theme.GeoBlue
import com.example.emergencynow.ui.theme.GeoYellow

@Composable
fun ProfileGeometricBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val alpha = if (isDarkMode) 0.2f else 0.9f
    
    Box(modifier = modifier.fillMaxSize()) {
        TopRightProfileDecoration(alpha = alpha)
        
        BottomLeftProfileDecoration(alpha = alpha)
        
        SmallDecorativeDots(alpha = alpha)
    }
}

@Composable
private fun TopRightProfileDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Canvas(
            modifier = Modifier.size(480.dp, 420.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            drawRoundRect(
                color = GeoDark.copy(alpha = alpha),
                topLeft = Offset(canvasWidth * 0.7f, -canvasHeight * 0.15f),
                size = Size(canvasWidth * 0.35f, canvasHeight * 0.35f),
                cornerRadius = CornerRadius(80.dp.toPx(), 80.dp.toPx())
            )
            
            drawCircle(
                color = GeoBlue.copy(alpha = 0.25f * alpha),
                radius = 130.dp.toPx(),
                center = Offset(canvasWidth * 0.85f, canvasHeight * 0.12f)
            )

            rotate(degrees = -30f, pivot = Offset(canvasWidth * 0.75f, canvasHeight * 0.12f)) {
                drawRect(
                    color = GeoYellow.copy(alpha = alpha),
                    topLeft = Offset(canvasWidth * 0.5f, canvasHeight * 0.1f),
                    size = Size(canvasWidth * 0.6f, canvasHeight * 0.03f)
                )
            }

            rotate(degrees = -30f, pivot = Offset(canvasWidth * 0.75f, canvasHeight * 0.08f)) {
                drawRect(
                    color = GeoDark.copy(alpha = 0.4f * alpha),
                    topLeft = Offset(canvasWidth * 0.45f, canvasHeight * 0.05f),
                    size = Size(canvasWidth * 0.7f, canvasHeight * 0.005f)
                )
            }
        }
    }
}

@Composable
private fun BottomLeftProfileDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Canvas(
            modifier = Modifier.size(480.dp, 400.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            drawRoundRect(
                color = GeoBlue.copy(alpha = 0.3f * alpha),
                topLeft = Offset(-canvasWidth * 0.1f, canvasHeight * 0.7f),
                size = Size(canvasWidth * 0.35f, canvasHeight * 0.35f),
                cornerRadius = CornerRadius(60.dp.toPx(), 60.dp.toPx())
            )
            
            rotate(degrees = -20f, pivot = Offset(canvasWidth * 0.2f, canvasHeight * 0.9f)) {
                drawRect(
                    color = GeoDark.copy(alpha = alpha),
                    topLeft = Offset(0f, canvasHeight * 0.88f),
                    size = Size(canvasWidth * 0.5f, canvasHeight * 0.04f)
                )
            }
            
            rotate(degrees = -20f, pivot = Offset(canvasWidth * 0.2f, canvasHeight * 0.82f)) {
                drawRect(
                    color = GeoYellow.copy(alpha = 0.8f * alpha),
                    topLeft = Offset(-canvasWidth * 0.05f, canvasHeight * 0.8f),
                    size = Size(canvasWidth * 0.45f, canvasHeight * 0.03f)
                )
            }
            
            rotate(degrees = -20f, pivot = Offset(canvasWidth * 0.2f, canvasHeight * 0.75f)) {
                drawRect(
                    color = GeoDark.copy(alpha = 0.4f * alpha),
                    topLeft = Offset(canvasWidth * 0.05f, canvasHeight * 0.73f),
                    size = Size(canvasWidth * 0.5f, canvasHeight * 0.005f)
                )
            }
        }
    }
}

@Composable
private fun SmallDecorativeDots(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            drawCircle(
                color = GeoBlue.copy(alpha = 0.6f * alpha),
                radius = 3.dp.toPx(),
                center = Offset(canvasWidth * 0.1f, canvasHeight * 0.05f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = 0.4f * alpha),
                radius = 2.dp.toPx(),
                center = Offset(canvasWidth * 0.14f, canvasHeight * 0.07f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = 0.35f * alpha),
                radius = 1.5.dp.toPx(),
                center = Offset(canvasWidth * 0.18f, canvasHeight * 0.05f)
            )

            drawCircle(
                color = GeoBlue.copy(alpha = 0.4f * alpha),
                radius = 2.dp.toPx(),
                center = Offset(canvasWidth * 0.86f, canvasHeight * 0.88f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = 0.6f * alpha),
                radius = 3.dp.toPx(),
                center = Offset(canvasWidth * 0.9f, canvasHeight * 0.92f)
            )
        }
    }
}
