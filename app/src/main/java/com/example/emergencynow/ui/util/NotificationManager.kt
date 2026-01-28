package com.example.emergencynow.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Notification(
    val message: String,
    val type: NotificationType = NotificationType.Error,
    val id: Long = System.currentTimeMillis()
)

enum class NotificationType {
    Error, Success, Info
}

class NotificationManager {
    private val _notifications = MutableStateFlow<Notification?>(null)
    val notifications = _notifications.asStateFlow()

    private var dismissJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun showError(message: String) {
        show(message, NotificationType.Error)
    }

    fun showSuccess(message: String) {
        show(message, NotificationType.Success)
    }

    private fun show(message: String, type: NotificationType) {
        dismissJob?.cancel()
        _notifications.update { Notification(message, type) }
        
        dismissJob = scope.launch {
            delay(4000) // Show for 4 seconds
            _notifications.update { null }
        }
    }

    fun dismiss() {
        dismissJob?.cancel()
        _notifications.update { null }
    }
}
