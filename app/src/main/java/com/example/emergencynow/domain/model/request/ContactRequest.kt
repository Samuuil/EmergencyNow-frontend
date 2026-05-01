package com.example.emergencynow.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateContactRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?
)
