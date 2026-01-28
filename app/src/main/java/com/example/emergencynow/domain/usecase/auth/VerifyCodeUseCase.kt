package com.example.emergencynow.domain.usecase.auth

import com.example.emergencynow.domain.model.entity.Token
import com.example.emergencynow.domain.repository.AuthRepository

class VerifyCodeUseCase(
    private val authRepository: Lazy<AuthRepository>
) {
    suspend operator fun invoke(egn: String, code: String): Result<Token> {
        return authRepository.value.verifyCode(egn, code)
    }
}
