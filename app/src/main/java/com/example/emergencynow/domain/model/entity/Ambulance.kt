package com.example.emergencynow.domain.model.entity

import java.time.Instant

data class Ambulance(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val available: Boolean,
    val driverId: String?,
    val lastCallAcceptedAt: Instant?,
    val createdAt: Instant?,
    val updatedAt: Instant?
)
