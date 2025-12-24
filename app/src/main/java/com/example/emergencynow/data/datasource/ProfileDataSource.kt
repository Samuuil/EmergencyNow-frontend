package com.example.emergencynow.data.datasource

import com.example.emergencynow.data.model.response.ProfileResponse

interface ProfileDataSource {
    suspend fun createProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): ProfileResponse
    
    suspend fun updateProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): ProfileResponse
    
    suspend fun getMyProfile(): ProfileResponse
}
