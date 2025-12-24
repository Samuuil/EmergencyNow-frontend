package com.example.emergencynow.domain.model.entity

import java.util.Date

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
    val dispatchedAt: Date?,
    val arrivedAt: Date?,
    val completedAt: Date?,
    val createdAt: Date,
    val selectedHospitalId: String?,
    val selectedHospitalName: String?,
    val hospitalRoutePolyline: String?,
    val hospitalRouteDistance: Int?,
    val hospitalRouteDuration: Int?,
    val hospitalRouteSteps: List<RouteStep>?
)

enum class CallStatus {
    PENDING,
    DISPATCHED,
    EN_ROUTE,
    ARRIVED,
    NAVIGATING_TO_HOSPITAL,
    COMPLETED,
    CANCELLED
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
