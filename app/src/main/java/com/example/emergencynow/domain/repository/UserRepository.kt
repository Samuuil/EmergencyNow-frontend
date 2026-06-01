package com.example.emergencynow.domain.repository

interface UserRepository {
    suspend fun getUserEgn(userId: String): Result<String>
    suspend fun getMyEgn(): Result<String>
}
