package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.ContactResponse

interface ContactDataSource {
    suspend fun getMyContacts(): List<ContactResponse>
    
    suspend fun createContact(
        name: String,
        phoneNumber: String,
        email: String?
    ): ContactResponse
    
    suspend fun deleteContact(id: String)
}
