package com.example.emergencynow.domain.model.response

data class ProfileResponse(
    val id: String? = null,
    val egn: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val bloodType: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val allergies: List<String>? = null,
    val illnesses: List<String>? = null,
    val medicines: List<String>? = null
)
