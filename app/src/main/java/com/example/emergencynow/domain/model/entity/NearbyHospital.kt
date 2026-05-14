package com.example.emergencynow.domain.model.entity

data class NearbyHospital(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int? = null,
    val availableBeds: Int? = null,
    val address: String? = null
)
