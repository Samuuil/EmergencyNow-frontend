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

/**
 * Creates a custom map marker icon with a circular background
 * @param backgroundColor Background color for the marker circle
 * @param size Size of the marker in dp
 * @param drawIcon Lambda to draw the icon shape
 * @return BitmapDescriptor for use with Google Maps Marker
 */
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
    
    // Draw shadow (lighter circle behind)
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = Color.Black.copy(alpha = 0.2f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint)
    
    // Draw circular background
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, bgPaint)
    
    // Draw white border
    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = sizePx / 16f
    }
    canvas.drawCircle(centerX, centerY, radius - borderPaint.strokeWidth / 2, borderPaint)
    
    // Draw icon
    drawIcon(canvas, centerX, centerY)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Converts a drawable resource to BitmapDescriptor for use as a map marker
 * @param context Android context
 * @param drawableResId Resource ID of the drawable (vector or bitmap)
 * @param backgroundColor Background color for the circular marker
 * @param iconColor Color to tint the icon
 * @param size Size of the marker in dp
 */
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
    
    // Draw shadow
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = Color.Black.copy(alpha = 0.2f).toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint)
    
    // Draw circular background
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = backgroundColor.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, bgPaint)
    
    // Draw white border
    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = sizePx / 16f
    }
    canvas.drawCircle(centerX, centerY, radius - borderPaint.strokeWidth / 2, borderPaint)
    
    // Draw the icon from drawable resource
    val drawable = ContextCompat.getDrawable(context, drawableResId)
    drawable?.let {
        // Tint the drawable
        val wrappedDrawable = DrawableCompat.wrap(it.mutate())
        DrawableCompat.setTint(wrappedDrawable, iconColor.toArgb())
        
        // Calculate icon size (60% of marker size)
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

/**
 * Convenience functions for creating map markers with your downloaded icons
 * 
 * IMPORTANT: After downloading icons from Google Fonts, add them to:
 * app/src/main/res/drawable/ with these exact names:
 * - ic_ambulance_marker.xml
 * - ic_hospital_marker.xml  
 * - ic_user_location_marker.xml
 * 
 * Then uncomment the functions below and replace R.drawable.xxx with your actual resource IDs
 */

/**
 * Creates an ambulance marker using your downloaded icon
 * Usage: createAmbulanceMarker(context, R.drawable.ic_ambulance_marker)
 */
fun createAmbulanceMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFF2563EB), // BrandBlueMid
        iconColor = Color.White,
        size = 72.dp // Increased from 56.dp
    )
}

/**
 * Creates a hospital marker using your downloaded icon
 * Usage: createHospitalMarker(context, R.drawable.ic_hospital_marker)
 */
fun createHospitalMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFF10B981), // Green
        iconColor = Color.White,
        size = 72.dp // Increased from 56.dp
    )
}

/**
 * Creates a user location marker using your downloaded icon
 * Usage: createUserLocationMarker(context, R.drawable.ic_user_location_marker)
 */
fun createUserLocationMarker(context: Context, drawableResId: Int): BitmapDescriptor {
    return createMarkerFromDrawable(
        context = context,
        drawableResId = drawableResId,
        backgroundColor = Color(0xFFEF4444), // Emergency red
        iconColor = Color.White,
        size = 72.dp // Increased from 56.dp
    )
}

