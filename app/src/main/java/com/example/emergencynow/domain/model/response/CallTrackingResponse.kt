package com.example.emergencynow.domain.model.response

import com.google.gson.annotations.SerializedName

data class CallTrackingResponse(
    @SerializedName("callId")
    val callId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("ambulanceId")
    val ambulanceId: String? = null,
    
    @SerializedName("driverLatitude")
    val driverLatitude: Double? = null,
    
    @SerializedName("driverLongitude")
    val driverLongitude: Double? = null,
    
    @SerializedName("estimatedArrival")
    val estimatedArrival: Int? = null,
    
    @SerializedName("route")
    val route: RouteDto? = null
)

data class RouteDto(
    @SerializedName("polyline")
    val polyline: String,
    
    @SerializedName("distance")
    val distance: Int,
    
    @SerializedName("duration")
    val duration: Int,
    
    @SerializedName("steps")
    val steps: List<RouteStepDto>? = null
)

data class RouteStepDto(
    @SerializedName("distance")
    val distance: Int,
    
    @SerializedName("duration")
    val duration: Int,
    
    @SerializedName("instruction")
    val instruction: String,
    
    @SerializedName("startLocation")
    val startLocation: LocationDto,
    
    @SerializedName("endLocation")
    val endLocation: LocationDto
)

data class LocationDto(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lng")
    val lng: Double
)
