package com.example.emergencynow.domain.usecase.contact

import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.repository.ContactRepository

class UpdateContactUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(
        id: String,
        name: String,
        phoneNumber: String,
        email: String?
    ): Result<Contact> {
        return repository.updateContact(id, name, phoneNumber, email)
    }
}
