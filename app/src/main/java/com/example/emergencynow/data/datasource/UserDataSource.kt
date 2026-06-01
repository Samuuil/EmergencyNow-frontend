package com.example.emergencynow.data.datasource

interface UserDataSource {
    suspend fun getUserEgn(userId: String): String
    suspend fun getMyEgn(): String
}
