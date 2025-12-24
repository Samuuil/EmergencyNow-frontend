package com.example.emergencynow.domain.usecase.auth

import com.example.emergencynow.domain.model.entity.Token
import com.example.emergencynow.domain.repository.AuthRepository

class RefreshTokenUseCase(
    private val authRepository: Lazy<AuthRepository>
) {
    suspend operator fun invoke(refreshToken: String): Result<Token> {
        return authRepository.value.refreshToken(refreshToken)
    }
}
