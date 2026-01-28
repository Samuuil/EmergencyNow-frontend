package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.ContactDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Contact
import com.example.emergencynow.domain.model.mapper.toDomain
import com.example.emergencynow.domain.model.mapper.toDomainList
import com.example.emergencynow.domain.repository.ContactRepository

class ContactRepositoryImpl(
    private val contactDataSource: ContactDataSource
) : ContactRepository {
    
    override suspend fun getMyContacts(): Result<List<Contact>> = safeApiCall {
        contactDataSource.getMyContacts().toDomainList()
    }
    
    override suspend fun createContact(
        name: String,
        phoneNumber: String,
        email: String?
    ): Result<Contact> = safeApiCall {
        contactDataSource.createContact(name, phoneNumber, email).toDomain()
    }
    
    override suspend fun deleteContact(id: String): Result<Unit> = safeApiCall {
        contactDataSource.deleteContact(id)
    }
}
