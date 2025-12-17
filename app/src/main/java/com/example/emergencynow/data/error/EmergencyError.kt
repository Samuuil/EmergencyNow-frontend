package com.example.emergencynow.data.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class EmergencyError : Exception() {
    @Serializable
    data class Generic(
        @SerialName("code")
        val code: String?,
        @SerialName("message")
        val messageString: String?,
        @SerialName("statusCode")
        val statusCode: Int?,
        val stringRes: Int? = null
    ) : EmergencyError() {
        override val message: String?
            get() = messageString
    }
}
