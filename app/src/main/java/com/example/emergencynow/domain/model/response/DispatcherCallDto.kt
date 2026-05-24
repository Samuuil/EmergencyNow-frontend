package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class DispatcherCallDto(
    val callId: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val createdAt: String = "",
    val userName: String? = null,
    val patient: PatientDto? = null,
)

@Serializable
data class PatientDto(
    val egn: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val bloodType: String? = null,
    val allergies: List<String>? = null,
    val medicines: List<String>? = null,
    val illnesses: List<String>? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
)
