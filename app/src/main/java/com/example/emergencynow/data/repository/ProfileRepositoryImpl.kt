package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.ProfileDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.model.mapper.toDomain
import com.example.emergencynow.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val profileDataSource: ProfileDataSource
) : ProfileRepository {
    
    override suspend fun createProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): Result<Profile> = safeApiCall {
        profileDataSource.createProfile(height, weight, gender, allergies).toDomain()
    }
    
    override suspend fun updateProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): Result<Profile> = safeApiCall {
        profileDataSource.updateProfile(height, weight, gender, allergies).toDomain()
    }
    
    override suspend fun getMyProfile(): Result<Profile> = safeApiCall {
        profileDataSource.getMyProfile().toDomain()
    }
}
