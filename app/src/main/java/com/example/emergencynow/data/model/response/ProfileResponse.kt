package com.example.emergencynow.data.model.response

data class ProfileResponse(
    val id: String?,
    val height: Int?,
    val weight: Int?,
    val gender: String?,
    val allergies: List<String>?,
    val bloodType: String?,
    val illnesses: List<String>?,
    val medicines: List<String>?,
    val dateOfBirth: String?
)
