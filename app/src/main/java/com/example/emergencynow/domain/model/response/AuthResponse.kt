package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

data class InitiateLoginResponse(
    val message: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class JwtPayload(
    val sub: String?,
    val role: String?,
    val egn: String?,
)
