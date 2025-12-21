package com.example.emergencynow.data.model

data class HospitalDto(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val distance: Int?, // meters from driver location
    val duration: Int?  // seconds to reach
)

data class GetHospitalsRequest(
    val latitude: Double,
    val longitude: Double
)

data class SelectHospitalRequest(
    val hospitalId: String,
    val latitude: Double,
    val longitude: Double
)

data class HospitalInfo(
    val id: String?,
    val name: String?
)

data class HospitalRouteResponse(
    val hospital: HospitalInfo?,
    val route: RouteDto?
)
