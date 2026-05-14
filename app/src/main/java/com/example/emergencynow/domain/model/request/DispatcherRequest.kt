package com.example.emergencynow.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AssignAmbulanceRequest(
    val ambulanceId: String,
)
