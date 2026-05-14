package com.example.emergencynow.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class GetHospitalsRequest(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class SelectHospitalRequest(
    val hospitalId: String,
    val latitude: Double,
    val longitude: Double
)
