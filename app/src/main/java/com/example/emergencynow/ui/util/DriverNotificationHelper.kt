package com.example.emergencynow.ui.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.emergencynow.MainActivity
import com.example.emergencynow.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class DriverNotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "DriverNotificationHelper"
        private const val CHANNEL_ID = "driver_call_offers"
        private const val NOTIFICATION_ID_BASE = 4_200
    }

    private val notificationManager = NotificationManagerCompat.from(context)
    private var channelCreated = false

    fun showCallOfferNotification(offer: CallOffer) {
        if (!ensurePermission()) {
            Log.w(TAG, "Notification permission not granted. Skipping call offer notification.")
            return
        }

        ensureChannel()

        val title = context.getString(R.string.driver_call_notification_title)
        val message = context.getString(R.string.driver_call_notification_message, offer.description)

        val detailsParts = mutableListOf<String>()
        if (offer.distance > 0) {
            detailsParts += context.getString(R.string.driver_call_notification_details_distance, offer.distance)
        }

        if (offer.duration > 0) {
            val durationMinutes = max(1, (offer.duration / 60.0).roundToInt())
            detailsParts += context.getString(R.string.driver_call_notification_details_duration_minutes, durationMinutes)
        }

        val bigText = buildString {
            append(message)
            if (detailsParts.isNotEmpty()) {
                append('\n')
                append(detailsParts.joinToString(" • "))
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", offer.callId)
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pendingIntent = PendingIntent.getActivity(
            context,
            offer.callId.hashCode(),
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ambulance)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = NOTIFICATION_ID_BASE + abs(offer.callId.hashCode())
        notificationManager.notify(notificationId, notification)
    }

    private fun ensurePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun ensureChannel() {
        if (channelCreated) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemManager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.driver_call_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.driver_call_channel_description)
            }
            systemManager?.createNotificationChannel(channel)
        }

        channelCreated = true
    }
}
