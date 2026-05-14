package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class DispatcherAmbulanceSummaryDto(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val driverId: String? = null,
    val driverOnline: Boolean = false,
    val available: Boolean = false,
)
