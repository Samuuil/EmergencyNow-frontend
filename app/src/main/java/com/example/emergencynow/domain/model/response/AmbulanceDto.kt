package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AmbulanceDto(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String? = null,
    val type: String? = null,
    val status: String? = null,
    val available: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val driverId: String? = null
)
