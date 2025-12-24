package com.example.emergencynow.data.model.request

data class CreateContactRequest(
    val name: String,
    val phoneNumber: String,
    val email: String?
)
