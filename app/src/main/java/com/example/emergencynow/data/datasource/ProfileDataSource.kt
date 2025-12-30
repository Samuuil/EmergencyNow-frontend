package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.ProfileResponse

interface ProfileDataSource {
    suspend fun createProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): ProfileResponse
    
    suspend fun updateProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): ProfileResponse
    
    suspend fun getMyProfile(): ProfileResponse
    
    suspend fun getProfileByEgn(egn: String): ProfileResponse
}
