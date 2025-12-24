package com.example.emergencynow.data.model.response

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
    val hospitalId: String? = null
)
