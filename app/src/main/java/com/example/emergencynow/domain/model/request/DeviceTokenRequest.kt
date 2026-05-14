package com.example.emergencynow.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequest(
    val token: String,
)
