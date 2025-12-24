package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.ContactDataSource
import com.example.emergencynow.data.model.request.CreateContactRequest
import com.example.emergencynow.data.model.response.ContactResponse
import com.example.emergencynow.data.service.ContactService

class ContactDataSourceImpl(
    private val contactService: ContactService
) : ContactDataSource {
    
    override suspend fun getMyContacts(): List<ContactResponse> {
        return contactService.getMyContacts().data
    }
    
    override suspend fun createContact(
        name: String,
        phoneNumber: String,
        email: String?
    ): ContactResponse {
        return contactService.createMyContact(
            CreateContactRequest(
                name = name,
                phoneNumber = phoneNumber,
                email = email
            )
        )
    }
    
    override suspend fun deleteContact(id: String) {
        contactService.deleteMyContact(id)
    }
}
