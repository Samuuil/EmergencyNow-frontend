package com.example.emergencynow.domain.model.entity

import java.time.Instant

data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String?,
    val placeId: String?,
    val isActive: Boolean,
    val createdAt: Instant?
)
