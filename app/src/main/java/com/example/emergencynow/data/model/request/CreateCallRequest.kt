package com.example.emergencynow.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateCallRequest(
    @SerializedName("description")
    val description: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)
