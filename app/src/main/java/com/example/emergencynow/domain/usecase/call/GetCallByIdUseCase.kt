package com.example.emergencynow.domain.usecase.call

import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.repository.CallRepository

class GetCallByIdUseCase(private val repository: CallRepository) {
    suspend operator fun invoke(callId: String): Result<CallResponse> {
        return repository.getCallById(callId)
    }
}
