package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class HospitalRouteWrapperResponse(
    val hospital: HospitalSummaryResponse?,
    val route: HospitalRouteInnerResponse?
)

@Serializable
data class HospitalSummaryResponse(
    val id: String?,
    val name: String?
)

@Serializable
data class HospitalRouteInnerResponse(
    val polyline: String?,
    val distance: Int?,
    val duration: Int?,
    val steps: List<RouteStepResponse>? = null
)
