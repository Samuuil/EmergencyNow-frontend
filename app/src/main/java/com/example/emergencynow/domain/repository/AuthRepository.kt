package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Token

interface AuthRepository {
    suspend fun requestVerificationCode(egn: String, method: String): Result<String>
    suspend fun verifyCode(egn: String, code: String): Result<Token>
    suspend fun refreshToken(refreshToken: String): Result<Token>
}
