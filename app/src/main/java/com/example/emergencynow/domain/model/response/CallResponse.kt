package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CallResponse(
    val id: String,
    val userId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: String? = null,
    val dispatchedAt: String? = null,
    val ambulanceId: String? = null,
    val hospitalId: String? = null,
    val userEgn: String? = null,
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
