package com.example.emergencynow.domain.model.entity

data class DispatcherCallOffer(
    val callId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val userName: String?,
)
