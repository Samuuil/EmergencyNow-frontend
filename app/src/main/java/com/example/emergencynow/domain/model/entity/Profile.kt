package com.example.emergencynow.domain.model.entity

data class Profile(
    val id: String,
    val height: Int,
    val weight: Int,
    val gender: Gender,
    val allergies: List<String>?,
    val bloodType: String?,
    val illnesses: List<String>?,
    val medicines: List<String>?,
    val dateOfBirth: String?
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
