package com.example.emergencynow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue80,
    secondary = SkyBlue80,
    tertiary = LightCyan80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = EmergencyRed
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = LightBlue40,
    tertiary = DarkBlue40,
    background = Color.White,
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF01579B),
    tertiaryContainer = Color(0xFFB3E5FC),
    onTertiaryContainer = Color(0xFF006064),
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun EmergencyNowTheme(
    darkTheme: Boolean = false, // Default to light theme
    // Dynamic color is disabled to use custom white and blue theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}