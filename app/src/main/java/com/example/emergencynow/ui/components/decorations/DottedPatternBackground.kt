package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DottedPatternBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    dotColor: Color? = null
) {
    val color = dotColor ?: if (isDarkMode) {
        Color(0xFF334155).copy(alpha = 0.4f)
    } else {
        Color(0xFFBFDBFE)
    }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val spacing = 24.dp.toPx()
        val dotRadius = 1.dp.toPx()
        
        val rows = (size.height / spacing).toInt() + 1
        val cols = (size.width / spacing).toInt() + 1
        
        for (row in 0..rows) {
            for (col in 0..cols) {
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(
                        x = col * spacing,
                        y = row * spacing
                    )
                )
            }
        }
    }
}
