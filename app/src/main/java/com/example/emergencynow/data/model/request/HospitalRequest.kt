package com.example.emergencynow.data.model.request

data class GetHospitalsRequest(
    val latitude: Double,
    val longitude: Double
)

data class SelectHospitalRequest(
    val hospitalId: String,
    val latitude: Double,
    val longitude: Double
)
