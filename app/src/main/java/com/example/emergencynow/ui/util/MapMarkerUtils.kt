package com.example.emergencynow.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createCustomMarkerIcon(
    backgroundColor: Color,
    size: Dp = 56.dp,
    drawIcon: (Canvas, Float, Float) -> Unit
): BitmapDescriptor {
    val sizePx = size.value.toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val radius = sizePx / 2f
    val centerX = radius
    val centerY = radius

    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = Color.Black.copy(alpha = 0.2f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint)

    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, bgPaint)

    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = sizePx / 16f
    }
    canvas.drawCircle(centerX, centerY, radius - borderPaint.strokeWidth / 2, borderPaint)

    drawIcon(canvas, centerX, centerY)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun createMarkerFromDrawable(
    context: Context,
    drawableResId: Int,
    backgroundColor: Color,
    iconColor: Color = Color.White,
    size: Dp = 56.dp
): BitmapDescriptor {
    val sizePx = size.value.toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val radius = sizePx / 2f
    val centerX = radius
    val centerY = radius

    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = Color.Black.copy(alpha = 0.2f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint)

    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, bgPaint)

    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = sizePx / 16f
    }
    canvas.drawCircle(centerX, centerY, radius - borderPaint.strokeWidth / 2, borderPaint)

    val drawable = ContextCompat.getDrawable(context, drawableResId)
    drawable?.let {
        val wrappedDrawable = DrawableCompat.wrap(it.mutate())
        DrawableCompat.setTint(wrappedDrawable, iconColor.toArgb())

        val iconSize = (sizePx * 0.6f).toInt()
        val left = (sizePx - iconSize) / 2
        val top = (sizePx - iconSize) / 2
        val right = left + iconSize
        val bottom = top + iconSize
        
        wrappedDrawable.setBounds(left, top, right, bottom)
        wrappedDrawable.draw(canvas)
    }
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun createAmbulanceMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFF2563EB),
        iconColor = Color.White,
        size = 72.dp
    )
}

fun createHospitalMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFF10B981),
        iconColor = Color.White,
        size = 72.dp
    )
}

fun createUserLocationMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFFEF4444),
        iconColor = Color.White,
        size = 72.dp
    )
}

