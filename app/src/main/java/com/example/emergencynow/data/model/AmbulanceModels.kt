package com.example.emergencynow.data.model

data class AmbulanceDto(
    val id: String,
    val licensePlate: String,
    val vehicleModel: String?,
    val latitude: Double?,
    val longitude: Double?,
    val available: Boolean,
    val driverId: String?,
)

data class AssignDriverRequest(
    val driverId: String?
)
