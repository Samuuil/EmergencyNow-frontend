package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.DeviceTokenDataSource
import com.example.emergencynow.data.service.DeviceTokenService
import com.example.emergencynow.domain.model.request.RegisterDeviceTokenRequest

class DeviceTokenDataSourceImpl(
    private val service: DeviceTokenService,
) : DeviceTokenDataSource {

    override suspend fun register(token: String) {
        service.registerDeviceToken(RegisterDeviceTokenRequest(token = token))
    }

    override suspend fun unregister(token: String) {
        service.unregisterDeviceToken(RegisterDeviceTokenRequest(token = token))
    }
}
