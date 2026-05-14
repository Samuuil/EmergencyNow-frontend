package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.request.RegisterDeviceTokenRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface DeviceTokenService {
    @POST("push-tokens")
    suspend fun registerDeviceToken(@Body body: RegisterDeviceTokenRequest)

    @HTTP(method = "DELETE", path = "push-tokens", hasBody = true)
    suspend fun unregisterDeviceToken(@Body body: RegisterDeviceTokenRequest)
}
