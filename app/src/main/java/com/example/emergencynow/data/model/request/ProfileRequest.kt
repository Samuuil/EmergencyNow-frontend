package com.example.emergencynow.data.model.request

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
