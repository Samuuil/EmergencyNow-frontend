package com.example.emergencynow.data.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class EmergencyError : Exception() {
    /**
     * Generic error from backend API.
     * Backend returns { code: String, message: String } without statusCode in body.
     * The httpStatusCode is populated from the HTTP response code, not the body.
     */
    @Serializable
    data class Generic(
        @SerialName("code")
        val code: String? = null,
        @SerialName("message")
        val messageString: String? = null,
        @kotlinx.serialization.Transient
        val httpStatusCode: Int? = null,
        @kotlinx.serialization.Transient
        val stringRes: Int? = null
    ) : EmergencyError() {
        override val message: String?
            get() = messageString
        
        /**
         * Check if this is a specific error code from backend
         */
        fun isErrorCode(errorCode: String): Boolean = code == errorCode
        
        /**
         * Check if this is an authentication error (401)
         */
        fun isUnauthorized(): Boolean = httpStatusCode == 401
        
        /**
         * Check if this is a not found error (404)
         */
        fun isNotFound(): Boolean = httpStatusCode == 404
        
        /**
         * Check if this is a validation error (400)
         */
        fun isBadRequest(): Boolean = httpStatusCode == 400
    }
}
