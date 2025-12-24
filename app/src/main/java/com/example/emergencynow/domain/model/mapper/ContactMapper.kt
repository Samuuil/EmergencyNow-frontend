package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.data.model.response.ContactResponse
import com.example.emergencynow.domain.model.entity.Contact

fun ContactResponse.toDomain(): Contact {
    return Contact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        email = email
    )
}

fun List<ContactResponse>.toDomainList(): List<Contact> {
    return map { it.toDomain() }
}
