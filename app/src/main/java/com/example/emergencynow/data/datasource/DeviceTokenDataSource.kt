package com.example.emergencynow.data.datasource

interface DeviceTokenDataSource {
    suspend fun register(token: String)
    suspend fun unregister(token: String)
}
