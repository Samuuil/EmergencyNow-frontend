package com.example.emergencynow.data.datasource

import com.example.emergencynow.data.model.response.TokenResponse

interface AuthDataSource {
    suspend fun initiateLogin(egn: String, method: String): Result<String>
    suspend fun verifyCode(egn: String, code: String): Result<TokenResponse>
    suspend fun refresh(refreshToken: String): Result<TokenResponse>
}
