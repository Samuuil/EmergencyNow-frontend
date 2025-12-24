package com.example.emergencynow.data.model.response

data class ProfileResponse(
    val id: String?,
    val height: Int?,
    val weight: Int?,
    val gender: String?,
    val allergies: List<String>?
)
