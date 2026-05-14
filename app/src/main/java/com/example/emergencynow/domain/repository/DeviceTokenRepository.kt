package com.example.emergencynow.domain.repository

interface DeviceTokenRepository {
    suspend fun register(token: String): Result<Unit>
    suspend fun unregister(token: String): Result<Unit>
}
