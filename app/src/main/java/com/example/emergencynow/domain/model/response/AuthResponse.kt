package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class InitiateLoginResponse(
    val message: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class JwtPayload(
    val sub: String? = null,
    val role: String? = null,
)
