package com.example.emergencynow.domain.model.entity

import java.time.Instant

data class Call(
    val id: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val status: CallStatus,
    val routePolyline: String?,
    val estimatedDistance: Int?,
    val estimatedDuration: Int?,
    val routeSteps: List<RouteStep>?,
    val ambulanceCurrentLatitude: Double?,
    val ambulanceCurrentLongitude: Double?,
    val dispatchedAt: Instant?,
    val arrivedAt: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant?,
    val selectedHospitalId: String?,
    val selectedHospitalName: String?,
    val hospitalRoutePolyline: String?,
    val hospitalRouteDistance: Int?,
    val hospitalRouteDuration: Int?,
    val hospitalRouteSteps: List<RouteStep>?
)

enum class CallStatus(val wire: String) {
    PENDING("PENDING"),
    DISPATCHED("DISPATCHED"),
    EN_ROUTE("EN_ROUTE"),
    ARRIVED("ARRIVED"),
    NAVIGATING_TO_HOSPITAL("NAVIGATING_TO_HOSPITAL"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromWire(s: String): CallStatus =
            entries.firstOrNull { it.wire == s.uppercase() } ?: PENDING
    }
}

data class RouteStep(
    val distance: Int,
    val duration: Int,
    val instruction: String,
    val startLocation: Location,
    val endLocation: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
