package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.DeviceTokenDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.repository.DeviceTokenRepository

class DeviceTokenRepositoryImpl(
    private val dataSource: DeviceTokenDataSource,
) : DeviceTokenRepository {

    override suspend fun register(token: String): Result<Unit> = safeApiCall {
        dataSource.register(token)
    }

    override suspend fun unregister(token: String): Result<Unit> = safeApiCall {
        dataSource.unregister(token)
    }
}
