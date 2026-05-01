package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserEgnResponse(
    val egn: String
)
