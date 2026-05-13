package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CallDto(
    val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: String? = null,
    val ambulanceId: String? = null,
    val hospitalId: String? = null,
    val patientEgn: String? = null,
    val patientPhoneNumber: String? = null
)
