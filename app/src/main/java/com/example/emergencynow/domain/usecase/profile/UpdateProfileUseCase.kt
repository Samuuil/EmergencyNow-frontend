package com.example.emergencynow.domain.usecase.profile

import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository

class UpdateProfileUseCase(private val repository: Lazy<ProfileRepository>) {
    suspend operator fun invoke(
        height: Int,
        weight: Int,
        gender: String,
        allergies: List<String>?
    ): Result<Profile> {
        return repository.value.updateProfile(height, weight, gender, allergies)
    }
}
