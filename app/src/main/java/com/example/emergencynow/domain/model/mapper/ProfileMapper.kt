package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.data.model.response.ProfileResponse
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
        allergies = allergies
    )
}
