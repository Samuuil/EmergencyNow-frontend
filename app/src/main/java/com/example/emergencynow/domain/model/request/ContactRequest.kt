package com.example.emergencynow.domain.model.request

data class CreateContactRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?
)
