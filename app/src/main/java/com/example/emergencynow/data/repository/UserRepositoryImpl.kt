package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.UserDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserRepository {
    override suspend fun getUserRole(userId: String): Result<String> = safeApiCall {
        userDataSource.getUserRole(userId)
    }
}
