package com.example.emergencynow.domain.repository

interface UserRepository {
    suspend fun getUserRole(userId: String): Result<String>
}
