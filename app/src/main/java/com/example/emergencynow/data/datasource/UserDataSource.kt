package com.example.emergencynow.data.datasource

interface UserDataSource {
    suspend fun getUserRole(userId: String): String
}
