package com.example.emergencynow.domain.model.entity

data class PatientRecord(
    val egn: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val bloodType: String?,
    val allergies: List<String>?,
    val medicines: List<String>?,
    val illnesses: List<String>?,
    val height: Int?,
    val weight: Int?,
    val gender: String?,
    val dateOfBirth: String?,
)

data class DispatcherCallOffer(
    val callId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String,
    val userName: String?,
    val patient: PatientRecord? = null,
)
