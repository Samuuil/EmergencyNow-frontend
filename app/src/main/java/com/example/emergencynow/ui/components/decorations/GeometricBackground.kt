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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.emergencynow.ui.theme.GeoDark
import com.example.emergencynow.ui.theme.GeoBlue
import com.example.emergencynow.ui.theme.GeoYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeometricBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val alpha = if (isDarkMode) 0.2f else 0.9f
    
    Box(modifier = modifier.fillMaxSize()) {
        TopRightDecoration(alpha = alpha)
        
        BottomLeftDecoration(alpha = alpha)
    }
}

@Composable
private fun TopRightDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Canvas(
            modifier = Modifier.size(550.dp, 550.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val path = Path().apply {
                moveTo(canvasWidth, 0f)
                lineTo(canvasWidth, canvasHeight * 0.47f)
                arcTo(
                    rect = Rect(
                        left = canvasWidth * 0.47f,
                        top = 0f,
                        right = canvasWidth,
                        bottom = canvasHeight * 0.47f
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(path = path, color = GeoBlue.copy(alpha = 0.3f * alpha))
            
            rotate(degrees = 35f, pivot = Offset(canvasWidth * 0.6f, canvasHeight * 0.13f)) {
                drawRect(
                    color = GeoDark.copy(alpha = alpha),
                    topLeft = Offset(canvasWidth * 0.6f, -canvasHeight * 0.13f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.67f, canvasHeight * 0.08f)
                )
                
                drawRect(
                    color = GeoYellow.copy(alpha = alpha),
                    topLeft = Offset(canvasWidth * 0.53f, canvasHeight * 0.03f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.67f, canvasHeight * 0.06f)
                )
                
                drawRect(
                    color = GeoBlue.copy(alpha = alpha),
                    topLeft = Offset(canvasWidth * 0.43f, -canvasHeight * 0.07f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.67f, canvasHeight * 0.007f)
                )
            }
            
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 5.dp.toPx(),
                center = Offset(canvasWidth * 0.16f, canvasHeight * 0.16f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 3.dp.toPx(),
                center = Offset(canvasWidth * 0.27f, canvasHeight * 0.09f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 4.dp.toPx(),
                center = Offset(canvasWidth * 0.09f, canvasHeight * 0.25f)
            )
        }
    }
}

@Composable
private fun BottomLeftDecoration(alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Canvas(
            modifier = Modifier.size(550.dp, 550.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val path = Path().apply {
                moveTo(0f, canvasHeight)
                lineTo(0f, canvasHeight * 0.53f)
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = canvasHeight * 0.53f,
                        right = canvasWidth * 0.53f,
                        bottom = canvasHeight
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(path = path, color = GeoBlue.copy(alpha = 0.3f * alpha))
            
            rotate(degrees = -35f, pivot = Offset(canvasWidth * 0.13f, canvasHeight * 0.73f)) {
                drawRect(
                    color = GeoYellow.copy(alpha = alpha),
                    topLeft = Offset(-canvasWidth * 0.07f, canvasHeight * 0.73f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.67f, canvasHeight * 0.06f)
                )
                
                drawRect(
                    color = GeoDark.copy(alpha = alpha),
                    topLeft = Offset(0f, canvasHeight * 0.87f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.67f, canvasHeight * 0.08f)
                )
                
                drawRect(
                    color = GeoBlue.copy(alpha = alpha),
                    topLeft = Offset(-canvasWidth * 0.13f, canvasHeight * 0.67f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth * 0.73f, canvasHeight * 0.007f)
                )
            }
            
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 5.dp.toPx(),
                center = Offset(canvasWidth * 0.83f, canvasHeight * 0.83f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 3.dp.toPx(),
                center = Offset(canvasWidth * 0.73f, canvasHeight * 0.9f)
            )
            drawCircle(
                color = GeoBlue.copy(alpha = alpha),
                radius = 4.dp.toPx(),
                center = Offset(canvasWidth * 0.9f, canvasHeight * 0.73f)
            )
        }
    }
}


