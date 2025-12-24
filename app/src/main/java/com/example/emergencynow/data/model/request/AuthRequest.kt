package com.example.emergencynow.data.model.request

import com.google.gson.annotations.SerializedName

enum class LoginMethod {
    @SerializedName("email")
    EMAIL,

    @SerializedName("sms")
    SMS
}

data class InitiateLoginRequest(
    val egn: String,
    val method: LoginMethod
)

data class VerifyCodeRequest(
    val egn: String,
    val code: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)
