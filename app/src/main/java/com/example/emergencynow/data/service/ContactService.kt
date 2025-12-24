package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.request.CreateContactRequest
import com.example.emergencynow.data.model.response.ContactResponse
import com.example.emergencynow.data.model.response.PaginatedResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContactService {
    @GET("contacts/me")
    suspend fun getMyContacts(): PaginatedResponse<ContactResponse>

    @POST("contacts/me")
    suspend fun createMyContact(@Body body: CreateContactRequest): ContactResponse

    @DELETE("contacts/me/{id}")
    suspend fun deleteMyContact(@Path("id") id: String)
}
