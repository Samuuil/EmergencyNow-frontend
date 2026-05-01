package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class HospitalRouteResponse(
    val polyline: String?,
    val distance: Int,
    val duration: Int,
    val steps: List<String> = emptyList()
)
