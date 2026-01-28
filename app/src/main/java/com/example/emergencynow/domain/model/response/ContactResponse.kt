package com.example.emergencynow.domain.model.response

data class ContactResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)
