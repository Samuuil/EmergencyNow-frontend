package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.AuthDataSource
import com.example.emergencynow.domain.model.entity.Token
import com.example.emergencynow.domain.model.mapper.toToken
import com.example.emergencynow.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource
) : AuthRepository {
    
    override suspend fun requestVerificationCode(egn: String, method: String): Result<String> {
        return authDataSource.initiateLogin(egn, method)
    }
    
    override suspend fun verifyCode(egn: String, code: String): Result<Token> {
        return authDataSource.verifyCode(egn, code).map { tokenResponse ->
            tokenResponse.toToken()
        }
    }
    
    override suspend fun refreshToken(refreshToken: String): Result<Token> {
        return authDataSource.refresh(refreshToken).map { tokenResponse ->
            tokenResponse.toToken()
        }
    }
}
