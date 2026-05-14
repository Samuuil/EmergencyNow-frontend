package com.example.emergencynow.ui.util

import android.util.Log
import com.example.emergencynow.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Receives FCM pushes — runs even when the app is completely killed.
 *
 * Two message types are handled, distinguished by `data.type`:
 *  - `CALL_OFFER`     → posts a loud notification + saves the offer for the
 *                        DriverViewModel to pick up on next app open.
 *  - other types      → ignored (extend as needed).
 *
 * Backend MUST send push as a **data-only** message (no `notification` field)
 * so this service is invoked even when the app is in the background.
 */
class EmergencyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "EmergencyFCM"
        private const val TYPE_CALL_OFFER = "call_offer"
        private const val TYPE_CALL_CANCELLED = "call_cancelled"
    }

    private val driverNotificationHelper: DriverNotificationHelper by inject()
    private val pendingCallOfferStorage: PendingCallOfferStorage by inject()
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase by inject()
    private val authStorage: AuthStorage by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Push received: data=${message.data}")

        when (message.data["type"]) {
            TYPE_CALL_OFFER -> handleCallOffer(message)
            TYPE_CALL_CANCELLED -> handleCallCancelled(message)
            else -> Log.w(TAG, "Unknown push type: ${message.data["type"]}")
        }
    }

    private fun handleCallOffer(message: RemoteMessage) {
        val data = message.data
        val callId = data["callId"] ?: run {
            Log.w(TAG, "Call offer push missing callId")
            return
        }

        val offer = CallOffer(
            callId = callId,
            description = data["description"] ?: "Emergency Call",
            latitude = data["latitude"]?.toDoubleOrNull() ?: 0.0,
            longitude = data["longitude"]?.toDoubleOrNull() ?: 0.0,
            distance = data["distance"]?.toIntOrNull() ?: 0,
            duration = data["duration"]?.toIntOrNull() ?: 0,
            priority = data["priority"] ?: "HIGH",
        )

        pendingCallOfferStorage.save(offer)
        driverNotificationHelper.showCallOfferNotification(offer)
    }

    private fun handleCallCancelled(message: RemoteMessage) {
        val callId = message.data["callId"] ?: return
        pendingCallOfferStorage.clearIfMatches(callId)
        driverNotificationHelper.stopAlert()
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed")
        // Only register if the user is logged in — for fresh installs the
        // VerifyCodeViewModel will fetch and register the token after login.
        if (authStorage.accessToken.isNullOrBlank()) {
            Log.d(TAG, "Skipping FCM token registration — user not authenticated")
            return
        }
        scope.launch {
            registerDeviceTokenUseCase(token).onFailure {
                Log.e(TAG, "Failed to register FCM token", it)
            }
        }
    }
}
