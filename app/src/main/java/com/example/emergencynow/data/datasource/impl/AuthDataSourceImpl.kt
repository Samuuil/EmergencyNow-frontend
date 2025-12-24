package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.AuthDataSource
import com.example.emergencynow.data.model.request.InitiateLoginRequest
import com.example.emergencynow.data.model.request.LoginMethod
import com.example.emergencynow.data.model.request.RefreshTokenRequest
import com.example.emergencynow.data.model.request.VerifyCodeRequest
import com.example.emergencynow.data.model.response.TokenResponse
import com.example.emergencynow.data.service.AuthService

class AuthDataSourceImpl(
    private val authService: AuthService
) : AuthDataSource {
    
    override suspend fun initiateLogin(egn: String, method: String): Result<String> {
        return try {
            val loginMethod = when(method.lowercase()) {
                "email" -> LoginMethod.EMAIL
                else -> LoginMethod.SMS
            }
            val response = authService.initiateLogin(
                InitiateLoginRequest(egn = egn, method = loginMethod)
            )
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyCode(egn: String, code: String): Result<TokenResponse> {
        return try {
            val response = authService.verifyCode(
                VerifyCodeRequest(egn = egn, code = code)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refresh(refreshToken: String): Result<TokenResponse> {
        return try {
            val response = authService.refresh(
                RefreshTokenRequest(refreshToken = refreshToken)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
