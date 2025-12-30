package com.example.emergencynow.domain.usecase.call

import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.model.response.CallDto
import com.example.emergencynow.domain.repository.CallRepository

class CreateCallUseCase(private val repository: Lazy<CallRepository>) {
    suspend operator fun invoke(request: CreateCallRequest): Result<CallDto> {
        val result = repository.value.createCall(
            description = request.description,
            latitude = request.latitude,
            longitude = request.longitude,
            userEgn = request.userEgn
        )
        return result.map { call ->
            CallDto(
                id = call.id,
                userId = com.example.emergencynow.ui.util.AuthSession.userId ?: "",
                latitude = call.latitude,
                longitude = call.longitude,
                status = call.status.name,
                createdAt = call.createdAt.toString()
            )
        }
    }
}
