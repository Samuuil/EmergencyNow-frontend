package com.example.emergencynow.domain.usecase.profile

import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository

class GetProfileUseCase(private val repository: Lazy<ProfileRepository>) {
    suspend operator fun invoke(): Result<Profile> {
        return repository.value.getMyProfile()
    }
}
