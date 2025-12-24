package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Profile

interface ProfileRepository {
    suspend fun createProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): Result<Profile>
    
    suspend fun updateProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): Result<Profile>
    
    suspend fun getMyProfile(): Result<Profile>
}
