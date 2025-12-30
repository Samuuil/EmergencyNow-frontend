package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.request.CreateProfileRequest
import com.example.emergencynow.domain.model.response.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ProfileService {
    @GET("profiles/me")
    suspend fun getMyProfile(): ProfileResponse
    
    @POST("profiles/me")
    suspend fun createProfile(@Body body: CreateProfileRequest): ProfileResponse
    
    @PATCH("profiles/me")
    suspend fun updateProfile(@Body body: CreateProfileRequest): ProfileResponse
    
    @GET("profiles/by-egn/{egn}")
    suspend fun getProfileByEgn(@retrofit2.http.Path("egn") egn: String): ProfileResponse
}
