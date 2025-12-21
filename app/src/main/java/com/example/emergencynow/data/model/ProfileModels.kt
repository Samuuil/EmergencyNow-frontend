package com.example.emergencynow.data.model

enum class GenderDto {
    MALE,
    FEMALE,
    OTHER
}

data class CreateProfileRequest(
    val height: Int,
    val weight: Int,
    val gender: GenderDto,
    val allergies: List<String>?
)

data class ProfileResponse(
    val id: String?,
    val height: Int?,
    val weight: Int?,
    val gender: String?,
    val allergies: List<String>?
)
