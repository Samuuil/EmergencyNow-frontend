package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CallResponse(
    val id: String,
    val userId: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val dispatchedAt: String? = null,
    val ambulanceId: String? = null,
    val hospitalId: String? = null,
    val userEgn: String? = null,
    val patientEgn: String? = null,
    val patientPhoneNumber: String? = null,
    val routeSteps: List<RouteStepResponse>? = null
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
