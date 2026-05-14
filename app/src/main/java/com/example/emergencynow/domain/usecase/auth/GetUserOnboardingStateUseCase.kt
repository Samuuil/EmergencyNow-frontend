package com.example.emergencynow.domain.usecase.auth

import com.example.emergencynow.data.error.EmergencyError
import com.example.emergencynow.domain.repository.ProfileRepository

sealed class OnboardingState {
    object ReturningUser : OnboardingState()
    object NewUser : OnboardingState()
}

class GetUserOnboardingStateUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<OnboardingState> =
        repository.getMyProfile().fold(
            onSuccess = { Result.success(OnboardingState.ReturningUser) },
            onFailure = { error ->
                if ((error as? EmergencyError.Generic)?.isNotFound() == true)
                    Result.success(OnboardingState.NewUser)
                else
                    Result.failure(error)
            }
        )
}
