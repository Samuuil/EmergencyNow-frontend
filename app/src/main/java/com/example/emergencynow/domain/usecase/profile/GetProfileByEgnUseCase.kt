package com.example.emergencynow.domain.usecase.profile

import com.example.emergencynow.domain.model.entity.Profile
import com.example.emergencynow.domain.repository.ProfileRepository

class GetProfileByEgnUseCase(private val repository: Lazy<ProfileRepository>) {
    suspend operator fun invoke(egn: String): Result<Profile> {
        return repository.value.getProfileByEgn(egn)
    }
}
