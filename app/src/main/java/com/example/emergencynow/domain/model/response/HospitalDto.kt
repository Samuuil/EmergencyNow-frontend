package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class HospitalDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int? = null,
    val availableBeds: Int? = null,
    val address: String? = null,
    val type: String? = null,
    val status: String? = null
)
