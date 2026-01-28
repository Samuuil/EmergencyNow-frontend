package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Contact

interface ContactRepository {
    suspend fun getMyContacts(): Result<List<Contact>>
    
    suspend fun createContact(
        name: String,
        phoneNumber: String,
        email: String?
    ): Result<Contact>
    
    suspend fun deleteContact(id: String): Result<Unit>
}
