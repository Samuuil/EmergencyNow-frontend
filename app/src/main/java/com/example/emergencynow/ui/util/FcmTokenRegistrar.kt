package com.example.emergencynow.ui.util

import android.util.Log
import com.example.emergencynow.domain.usecase.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FcmTokenRegistrar(
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
) {
    suspend fun fetchAndRegister(): Result<String> = runCatching {
        val token = FirebaseMessaging.getInstance().token.await()
        Log.d(TAG, "Fetched FCM token: ${token.take(16)}…")
        registerDeviceTokenUseCase(token).getOrThrow()
        token
    }.onFailure { Log.e(TAG, "Failed to register FCM token", it) }

    companion object {
        private const val TAG = "FcmTokenRegistrar"
    }
}
