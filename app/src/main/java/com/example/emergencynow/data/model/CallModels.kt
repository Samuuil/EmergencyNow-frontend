package com.example.emergencynow.data.model

data class CreateCallRequest(
    val description: String,
    val latitude: Double,
    val longitude: Double
)

data class CallResponse(
    val id: String?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?,
    val routePolyline: String?,
    val estimatedDistance: Int?,
    val estimatedDuration: Int?,
    val routeSteps: List<RouteStepDto>?,
    val ambulanceCurrentLatitude: Double?,
    val ambulanceCurrentLongitude: Double?,
    val dispatchedAt: String?,
    val arrivedAt: String?,
    val completedAt: String?,
    val selectedHospitalId: String?,
    val selectedHospitalName: String?,
    val hospitalRoutePolyline: String?,
    val hospitalRouteDistance: Int?,
    val hospitalRouteDuration: Int?,
    val hospitalRouteSteps: List<RouteStepDto>?
)

data class CallTrackingLocation(
    val latitude: Double,
    val longitude: Double
)

data class RoutePoint(
    val lat: Double,
    val lng: Double
)

data class RouteStepDto(
    val distance: Int,
    val duration: Int,
    val instruction: String,
    val startLocation: RoutePoint,
    val endLocation: RoutePoint
)

data class RouteDto(
    val polyline: String?,
    val distance: Int?,
    val duration: Int?,
    val steps: List<RouteStepDto>?
)

data class CallTrackingCall(
    val id: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val status: String?,
    val ambulanceCurrentLatitude: Double?,
    val ambulanceCurrentLongitude: Double?
)

data class CallTrackingResponse(
    val call: CallTrackingCall,
    val currentLocation: CallTrackingLocation?,
    val route: RouteDto?
)
