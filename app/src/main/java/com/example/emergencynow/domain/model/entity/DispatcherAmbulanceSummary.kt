package com.example.emergencynow.domain.model.entity

data class DispatcherAmbulanceSummary(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val driverId: String?,
    val driverOnline: Boolean,
    val available: Boolean,
)
