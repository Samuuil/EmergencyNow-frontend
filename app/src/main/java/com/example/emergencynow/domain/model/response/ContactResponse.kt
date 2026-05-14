package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ContactResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)
