package com.example.emergencynow.domain.model.entity

import java.util.Date

data class Ambulance(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val available: Boolean,
    val driverId: String?,
    val lastCallAcceptedAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)
