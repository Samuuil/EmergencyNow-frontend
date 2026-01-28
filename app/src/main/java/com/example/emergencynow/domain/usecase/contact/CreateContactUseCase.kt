package com.example.emergencynow.domain.usecase.contact

import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.repository.ContactRepository

class CreateContactUseCase(private val repository: Lazy<ContactRepository>) {
    suspend operator fun invoke(
        name: String,
        phoneNumber: String,
        email: String?
    ): Result<Contact> {
        return repository.value.createContact(name, phoneNumber, email)
    }
}
