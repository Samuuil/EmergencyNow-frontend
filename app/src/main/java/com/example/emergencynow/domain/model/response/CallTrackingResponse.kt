package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CallTrackingResponse(
    val callId: String,
    val status: String,
    val ambulanceId: String? = null,
    val driverLatitude: Double? = null,
    val driverLongitude: Double? = null,
    val estimatedArrival: Int? = null,
    val route: RouteDto? = null
)

@Serializable
data class RouteDto(
    val polyline: String,
    val distance: Int,
    val duration: Int,
    val steps: List<RouteStepDto>? = null
)

@Serializable
data class RouteStepDto(
    val distance: Int,
    val duration: Int,
    val instruction: String,
    val startLocation: LocationDto,
    val endLocation: LocationDto
)

@Serializable
data class LocationDto(
    val lat: Double,
    val lng: Double
)
