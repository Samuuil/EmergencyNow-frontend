package com.example.emergencynow.domain.model.request

enum class GenderDto {
    MALE,
    FEMALE,
    OTHER
}

data class CreateProfileRequest(
    val height: Int,
    val weight: Int,
    val gender: GenderDto,
    val allergies: List<String>?,
    val bloodType: String?,
    val illnesses: List<String>?,
    val medicines: List<String>?,
    val dateOfBirth: String?
)
