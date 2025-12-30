package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.domain.model.response.ProfileResponse
import com.example.emergencynow.domain.model.entity.Gender
import com.example.emergencynow.domain.model.entity.Profile

fun ProfileResponse.toDomain(): Profile {
    return Profile(
        id = id ?: "",
        height = height ?: 0,
        weight = weight ?: 0,
        gender = when (gender?.uppercase()) {
            "MALE" -> Gender.MALE
            "FEMALE" -> Gender.FEMALE
            "OTHER" -> Gender.OTHER
            else -> Gender.OTHER
        },
        allergies = allergies,
        bloodType = bloodType,
        illnesses = illnesses,
        medicines = medicines,
        dateOfBirth = dateOfBirth
    )
}
