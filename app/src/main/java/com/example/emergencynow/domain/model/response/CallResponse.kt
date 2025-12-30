package com.example.emergencynow.domain.model.response

import com.google.gson.annotations.SerializedName

data class CallResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("dispatchedAt")
    val dispatchedAt: String? = null,
    
    @SerializedName("ambulanceId")
    val ambulanceId: String? = null,
    
    @SerializedName("hospitalId")
    val hospitalId: String? = null,
    
    @SerializedName("userEgn")
    val userEgn: String? = null,
    
    @SerializedName("routeSteps")
    val routeSteps: List<RouteStepResponse>? = null
)

data class RouteStepResponse(
    @SerializedName("distance")
    val distance: Int,
    
    @SerializedName("duration")
    val duration: Int,
    
    @SerializedName("instruction")
    val instruction: String,
    
    @SerializedName("startLocation")
    val startLocation: LocationResponse? = null,
    
    @SerializedName("endLocation")
    val endLocation: LocationResponse? = null
)

data class LocationResponse(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lng")
    val lng: Double
)
