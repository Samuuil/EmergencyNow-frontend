package com.example.emergencynow.domain.usecase.auth

import com.example.emergencynow.domain.repository.ContactRepository

sealed class OnboardingState {
    object ReturningUser : OnboardingState()
    object NewUser : OnboardingState()
}

class GetUserOnboardingStateUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke(): Result<OnboardingState> {
        return repository.getMyContacts().map { contacts ->
            if (contacts.isNotEmpty()) OnboardingState.ReturningUser
            else OnboardingState.NewUser
        }
    }
}
