package com.example.emergencynow.domain.usecase.notifications

import com.example.emergencynow.domain.repository.DeviceTokenRepository

class RegisterDeviceTokenUseCase(private val repository: DeviceTokenRepository) {
    suspend operator fun invoke(token: String): Result<Unit> = repository.register(token)
}
