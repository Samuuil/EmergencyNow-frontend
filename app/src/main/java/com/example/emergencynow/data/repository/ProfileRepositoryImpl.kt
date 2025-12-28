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
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): Result<Profile> = safeApiCall {
        profileDataSource.createProfile(height, weight, gender, allergies, bloodType, illnesses, medicines, dateOfBirth).toDomain()
    }
    
    override suspend fun updateProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): Result<Profile> = safeApiCall {
        profileDataSource.updateProfile(height, weight, gender, allergies, bloodType, illnesses, medicines, dateOfBirth).toDomain()
    }
    
    override suspend fun getMyProfile(): Result<Profile> = safeApiCall {
        profileDataSource.getMyProfile().toDomain()
    }
    
    override suspend fun getProfileByEgn(egn: String): Result<Profile> = safeApiCall {
        profileDataSource.getProfileByEgn(egn).toDomain()
    }
}
