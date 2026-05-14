package com.example.emergencynow.ui.util

import android.content.Context

/**
 * Stores the most recent call offer received while the app was closed/backgrounded
 * via FCM, so that the DriverViewModel can pick it up the next time the app opens.
 *
 * The pending offer is cleared once the driver acts on it (accept/decline) or the
 * call gets cancelled/superseded.
 */
class PendingCallOfferStorage(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("pending_call_offer_prefs", Context.MODE_PRIVATE)

    fun save(offer: CallOffer) {
        prefs.edit()
            .putString(KEY_CALL_ID, offer.callId)
            .putString(KEY_DESCRIPTION, offer.description)
            .putString(KEY_LATITUDE, offer.latitude.toString())
            .putString(KEY_LONGITUDE, offer.longitude.toString())
            .putInt(KEY_DISTANCE, offer.distance)
            .putInt(KEY_DURATION, offer.duration)
            .putString(KEY_PRIORITY, offer.priority)
            .putLong(KEY_RECEIVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun read(): CallOffer? {
        val callId = prefs.getString(KEY_CALL_ID, null) ?: return null
        val lat = prefs.getString(KEY_LATITUDE, null)?.toDoubleOrNull() ?: return null
        val lng = prefs.getString(KEY_LONGITUDE, null)?.toDoubleOrNull() ?: return null
        return CallOffer(
            callId = callId,
            description = prefs.getString(KEY_DESCRIPTION, "Emergency Call") ?: "Emergency Call",
            latitude = lat,
            longitude = lng,
            distance = prefs.getInt(KEY_DISTANCE, 0),
            duration = prefs.getInt(KEY_DURATION, 0),
            priority = prefs.getString(KEY_PRIORITY, "HIGH") ?: "HIGH",
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun clearIfMatches(callId: String) {
        if (prefs.getString(KEY_CALL_ID, null) == callId) clear()
    }

    companion object {
        private const val KEY_CALL_ID = "callId"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_DISTANCE = "distance"
        private const val KEY_DURATION = "duration"
        private const val KEY_PRIORITY = "priority"
        private const val KEY_RECEIVED_AT = "receivedAt"
    }
}
