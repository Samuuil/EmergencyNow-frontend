package com.example.emergencynow.domain.usecase.profile

import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository

class UpdateProfileUseCase(private val repository: Lazy<ProfileRepository>) {
    suspend operator fun invoke(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?,
        bloodType: String?,
        illnesses: List<String>?,
        medicines: List<String>?,
        dateOfBirth: String?
    ): Result<Profile> {
        return repository.value.updateProfile(height, weight, gender, allergies, bloodType, illnesses, medicines, dateOfBirth)
    }
}
