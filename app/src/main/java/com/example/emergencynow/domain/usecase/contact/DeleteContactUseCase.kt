package com.example.emergencynow.domain.usecase.contact

import com.example.emergencynow.domain.repository.ContactRepository

class DeleteContactUseCase(private val repository: Lazy<ContactRepository>) {
    suspend operator fun invoke(contactId: String): Result<Unit> {
        return repository.value.deleteContact(contactId)
    }
}
