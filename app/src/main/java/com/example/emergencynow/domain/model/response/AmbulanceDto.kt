package com.example.emergencynow.domain.model.response

import com.google.gson.annotations.SerializedName

data class AmbulanceDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("licensePlate")
    val licensePlate: String,
    
    @SerializedName("vehicleModel")
    val vehicleModel: String? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("available")
    val available: Boolean? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("driverId")
    val driverId: String? = null
)
