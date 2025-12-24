package com.example.emergencynow.domain.usecase.contact

import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.repository.ContactRepository

class GetContactsUseCase(private val repository: Lazy<ContactRepository>) {
    suspend operator fun invoke(): Result<List<Contact>> {
        return repository.value.getMyContacts()
    }
}
