package com.example.emergencynow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.emergencynow.R

val NotoSerifFontFamily = FontFamily(
    Font(R.font.notoserif_thin, FontWeight.Thin),
    Font(R.font.notoserif_extralight, FontWeight.ExtraLight),
    Font(R.font.notoserif_light, FontWeight.Light),
    Font(R.font.notoserif_regular, FontWeight.Normal),
    Font(R.font.notoserif_medium, FontWeight.Medium),
    Font(R.font.notoserif_semibold, FontWeight.SemiBold),
    Font(R.font.notoserif_bold, FontWeight.Bold),
    Font(R.font.notoserif_extrabold, FontWeight.ExtraBold),
    Font(R.font.notoserif_black, FontWeight.Black),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NotoSerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)