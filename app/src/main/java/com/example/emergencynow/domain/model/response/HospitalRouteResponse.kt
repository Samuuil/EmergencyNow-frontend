package com.example.emergencynow.domain.model.response

import com.google.gson.annotations.SerializedName

data class HospitalRouteResponse(
    @SerializedName("polyline")
    val polyline: String?,
    
    @SerializedName("distance")
    val distance: Int,
    
    @SerializedName("duration")
    val duration: Int
)
