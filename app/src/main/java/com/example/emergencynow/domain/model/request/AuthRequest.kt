package com.example.emergencynow.domain.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LoginMethod {
    @SerialName("email")
    EMAIL,

    @SerialName("sms")
    SMS
}

@Serializable
data class InitiateLoginRequest(
    val egn: String,
    val method: LoginMethod
)

@Serializable
data class VerifyCodeRequest(
    val egn: String,
    val code: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
