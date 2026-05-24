package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CallResponse(
    val id: String,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val dispatchedAt: String? = null,
    val arrivedAt: String? = null,
    val completedAt: String? = null,
    val userEgn: String? = null,
    val patientEgn: String? = null,
    val patientPhoneNumber: String? = null,
    val routePolyline: String? = null,
    val estimatedDistance: Int? = null,
    val estimatedDuration: Int? = null,
    val routeSteps: List<RouteStepResponse>? = null,
    val ambulanceCurrentLatitude: Double? = null,
    val ambulanceCurrentLongitude: Double? = null,
    val selectedHospitalId: String? = null,
    val selectedHospitalName: String? = null,
    val hospitalRoutePolyline: String? = null,
    val hospitalRouteDistance: Int? = null,
    val hospitalRouteDuration: Int? = null,
    val hospitalRouteSteps: List<RouteStepResponse>? = null,
)

@Serializable
data class RouteStepResponse(
    val distance: Int,
    val duration: Int,
    val instruction: String,
    val startLocation: LocationResponse? = null,
    val endLocation: LocationResponse? = null
)

@Serializable
data class LocationResponse(
    val lat: Double,
    val lng: Double
)
