package com.example.emergencynow.ui.components.decorations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.emergencynow.ui.theme.BackgroundDark
import com.example.emergencynow.ui.theme.CurveDarkBlue
import com.example.emergencynow.ui.theme.CurveDeepBlue
import com.example.emergencynow.ui.theme.CurveLightBlue
import com.example.emergencynow.ui.theme.CurveLightBlueAccent
import com.example.emergencynow.ui.theme.CurveMediumBlue
import com.example.emergencynow.ui.theme.CurvePaleBlue
import com.example.emergencynow.ui.theme.CurvePaleBlueBottom
import com.example.emergencynow.ui.theme.EgnBlobBottom
import com.example.emergencynow.ui.theme.EgnBlobDark
import com.example.emergencynow.ui.theme.EgnBlobLight

enum class BackgroundVariant { WELCOME, ENTER_EGN, CHOOSE_VERIFICATION, VERIFY_NUMBER }

@Composable
fun DecorativeBackground(
    variant: BackgroundVariant,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    when (variant) {
        BackgroundVariant.WELCOME -> WelcomeBackground(modifier, isDarkMode)
        BackgroundVariant.ENTER_EGN -> EgnBackground(modifier, isDarkMode)
        BackgroundVariant.CHOOSE_VERIFICATION -> ChooseVerifBackground(modifier, isDarkMode)
        BackgroundVariant.VERIFY_NUMBER -> VerifyNumBackground(modifier, isDarkMode)
    }
}

// region WELCOME

@Composable
private fun WelcomeBackground(modifier: Modifier, isDarkMode: Boolean) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) BackgroundDark else Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (!isDarkMode) {
                drawWelcomeTop(w, h)
                drawWelcomeHighlight(w, h)
                drawWelcomeMiddleAccent(w, h)
                drawWelcomeBottomAccent(w, h)
                drawWelcomeBottom(w, h)
            } else {
                drawWelcomeTop(w, h, darkMode = true)
                drawWelcomeBottom(w, h, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawWelcomeTop(w: Float, h: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.2f else 0.6f
    val gradient = Brush.linearGradient(
        colors = listOf(CurveLightBlue.copy(alpha = opacity), CurveMediumBlue.copy(alpha = opacity)),
        start = Offset(w * 0.2f, 0f),
        end = Offset(w * 0.8f, h * 0.5f)
    )
    rotate(degrees = -10f, pivot = Offset(w * 0.2f, h * 0.2f)) {
        drawOval(brush = gradient, topLeft = Offset(-w * 0.35f, -h * 0.15f), size = Size(w * 1.1f, h * 0.7f))
    }
}

private fun DrawScope.drawWelcomeHighlight(w: Float, h: Float) {
    drawCircle(color = CurveDarkBlue.copy(alpha = 0.1f), radius = w * 0.2f, center = Offset(w * 0.4f, h * 0.05f))
}

private fun DrawScope.drawWelcomeMiddleAccent(w: Float, h: Float) {
    val gradient = Brush.radialGradient(
        colors = listOf(CurveDeepBlue.copy(alpha = 0.15f), CurveDarkBlue.copy(alpha = 0.15f)),
        center = Offset(w * 1.1f, h * 0.45f),
        radius = w * 0.4f
    )
    rotate(degrees = 15f, pivot = Offset(w * 1.1f, h * 0.45f)) {
        drawCircle(brush = gradient, radius = w * 0.5f, center = Offset(w * 1.1f, h * 0.45f))
    }
}

private fun DrawScope.drawWelcomeBottomAccent(w: Float, h: Float) {
    val gradient = Brush.linearGradient(
        colors = listOf(CurveLightBlueAccent.copy(alpha = 0.5f), CurvePaleBlueBottom.copy(alpha = 0.5f)),
        start = Offset(w * 0.5f, h * 0.9f),
        end = Offset(w * 1.2f, h * 1.1f)
    )
    drawOval(brush = gradient, topLeft = Offset(w * 0.4f, h * 0.8f), size = Size(w * 1.0f, h * 0.5f))
}

private fun DrawScope.drawWelcomeBottom(w: Float, h: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.15f else 0.9f
    drawOval(color = CurvePaleBlue.copy(alpha = opacity), topLeft = Offset(w * 0.2f, h * 0.8f), size = Size(w * 1.3f, h * 0.65f))
}

// endregion

// region ENTER_EGN

@Composable
private fun EgnBackground(modifier: Modifier, isDarkMode: Boolean) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) BackgroundDark else Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (!isDarkMode) {
                drawEgnTop(w, h)
                drawEgnBottom(w, h)
            } else {
                drawEgnTop(w, h, darkMode = true)
                drawEgnBottom(w, h, darkMode = true)
            }
        }
    }
}

private fun DrawScope.drawEgnTop(w: Float, h: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.2f else 1.0f
    val gradient = Brush.linearGradient(
        colors = listOf(EgnBlobDark.copy(alpha = opacity), EgnBlobLight.copy(alpha = opacity)),
        start = Offset(0f, 0f),
        end = Offset(w * 1.4f, h * 1.4f)
    )
    drawCircle(brush = gradient, radius = w * 0.7f, center = Offset(w * 0.45f, -h * 0.15f))
}

private fun DrawScope.drawEgnBottom(w: Float, h: Float, darkMode: Boolean = false) {
    val opacity = if (darkMode) 0.15f else 0.5f
    rotate(degrees = -10f, pivot = Offset(w * 0.3f, h * 0.95f)) {
        drawOval(color = EgnBlobBottom.copy(alpha = opacity), topLeft = Offset(-w * 0.2f, h * 0.7f), size = Size(w * 1.0f, h * 0.4f))
    }
}

// endregion

// region CHOOSE_VERIFICATION

@Composable
private fun ChooseVerifBackground(modifier: Modifier, isDarkMode: Boolean) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) BackgroundDark else Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawVerifTopLeft(w, h, isDarkMode)
            drawVerifTopRight(w, h, isDarkMode)
            drawVerifBottomRight(w, h, isDarkMode)
            drawVerifBottomLeft(w, h, isDarkMode)
        }
    }
}

private fun DrawScope.drawVerifTopLeft(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.3f else 1.0f
    val color = if (darkMode) Color(0xFF1E3A8A).copy(alpha = opacity) else Color(0xFFBFDBFE).copy(alpha = opacity)
    drawOval(color = color, topLeft = Offset(-w * 0.3f, -h * 0.15f), size = Size(w * 1.0f, h * 0.6f))
}

private fun DrawScope.drawVerifTopRight(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.4f else 0.5f
    val color = if (darkMode) Color(0xFF1E40AF).copy(alpha = opacity) else Color(0xFF60A5FA).copy(alpha = opacity)
    drawOval(color = color, topLeft = Offset(w * 0.5f, h * 0.1f), size = Size(w * 0.7f, h * 0.5f))
}

private fun DrawScope.drawVerifBottomRight(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.4f else 1.0f
    val color = if (darkMode) Color(0xFF1E3A8A).copy(alpha = opacity) else Color(0xFFBFDBFE).copy(alpha = opacity)
    drawOval(color = color, topLeft = Offset(w * 0.25f, h * 0.6f), size = Size(w * 0.9f, h * 0.5f))
}

private fun DrawScope.drawVerifBottomLeft(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.2f else 1.0f
    val color = if (darkMode) Color(0xFF1E40AF).copy(alpha = opacity) else Color(0xFFDBEAFE).copy(alpha = opacity)
    drawOval(color = color, topLeft = Offset(-w * 0.1f, h * 0.7f), size = Size(w * 0.5f, h * 0.3f))
}

// endregion

// region VERIFY_NUMBER

@Composable
private fun VerifyNumBackground(modifier: Modifier, isDarkMode: Boolean) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) BackgroundDark else Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawVerifyTopRight(w, h, isDarkMode)
            drawVerifyTopLeft(w, h, isDarkMode)
            drawVerifyBottom(w, h, isDarkMode)
        }
    }
}

private fun DrawScope.drawVerifyTopRight(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.1f else 0.3f
    drawOval(color = Color(0xFF93C5FD).copy(alpha = opacity), topLeft = Offset(w * 0.3f, -h * 0.1f), size = Size(w * 0.9f, h * 0.55f))
}

private fun DrawScope.drawVerifyTopLeft(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.1f else 0.4f
    drawOval(color = Color(0xFFBFDBFE).copy(alpha = opacity), topLeft = Offset(-w * 0.1f, -h * 0.15f), size = Size(w * 0.7f, h * 0.45f))
}

private fun DrawScope.drawVerifyBottom(w: Float, h: Float, darkMode: Boolean) {
    val opacity = if (darkMode) 0.05f else 0.6f
    val color = if (darkMode) Color(0xFF1E3A8A).copy(alpha = opacity) else Color(0xFFEFF6FF).copy(alpha = opacity)
    drawOval(color = color, topLeft = Offset(-w * 0.2f, h * 0.7f), size = Size(w * 1.4f, h * 0.35f))
}

// endregion
