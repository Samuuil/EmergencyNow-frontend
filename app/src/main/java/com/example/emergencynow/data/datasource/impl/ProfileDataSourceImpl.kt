package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.ProfileDataSource
import com.example.emergencynow.data.model.request.CreateProfileRequest
import com.example.emergencynow.data.model.request.GenderDto
import com.example.emergencynow.data.model.response.ProfileResponse
import com.example.emergencynow.data.service.ProfileService

class ProfileDataSourceImpl(
    private val profileService: ProfileService
) : ProfileDataSource {
    
    override suspend fun createProfile(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): ProfileResponse {
        val genderDto = when (gender.uppercase()) {
            "MALE" -> GenderDto.MALE
            "FEMALE" -> GenderDto.FEMALE
            else -> GenderDto.OTHER
        }
        
        return profileService.createProfile(
            CreateProfileRequest(
                height = height,
                weight = weight,
                gender = genderDto,
                allergies = allergies,
                bloodType = bloodType,
                illnesses = illnesses,
                medicines = medicines,
                dateOfBirth = dateOfBirth
            )
        )
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
    ): ProfileResponse {
        val genderDto = when (gender.uppercase()) {
            "MALE" -> GenderDto.MALE
            "FEMALE" -> GenderDto.FEMALE
            else -> GenderDto.OTHER
        }
        
        return profileService.updateProfile(
            CreateProfileRequest(
                height = height,
                weight = weight,
                gender = genderDto,
                allergies = allergies,
                bloodType = bloodType,
                illnesses = illnesses,
                medicines = medicines,
                dateOfBirth = dateOfBirth
            )
        )
    }
    
    override suspend fun getMyProfile(): ProfileResponse {
        return profileService.getMyProfile()
    }
    
    override suspend fun getProfileByEgn(egn: String): ProfileResponse {
        return profileService.getProfileByEgn(egn)
    }
}
