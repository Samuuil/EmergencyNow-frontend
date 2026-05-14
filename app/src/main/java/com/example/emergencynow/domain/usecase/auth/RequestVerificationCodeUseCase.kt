package com.example.emergencynow.domain.usecase.auth

import com.example.emergencynow.domain.repository.AuthRepository

class RequestVerificationCodeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(egn: String, method: String): Result<String> {
        return authRepository.requestVerificationCode(egn, method)
    }
}
