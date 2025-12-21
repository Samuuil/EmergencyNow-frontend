package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.CreateProfileRequest
import com.example.emergencynow.data.model.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ProfileService {
    @POST("profiles/me")
    suspend fun createProfile(@Body body: CreateProfileRequest): ProfileResponse
}
