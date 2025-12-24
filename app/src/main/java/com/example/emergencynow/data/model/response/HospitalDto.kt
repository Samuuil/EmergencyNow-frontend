package com.example.emergencynow.data.model.response

import com.google.gson.annotations.SerializedName

data class HospitalDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("distance")
    val distance: Int? = null,
    
    @SerializedName("availableBeds")
    val availableBeds: Int? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("status")
    val status: String? = null
)
