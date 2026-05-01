package com.example.emergencynow.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCallRequest(
    val description: String,
    val latitude: Double,
    val longitude: Double
)
