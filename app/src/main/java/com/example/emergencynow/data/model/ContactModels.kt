package com.example.emergencynow.data.model

data class CreateContactRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?
)

data class ContactResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)
