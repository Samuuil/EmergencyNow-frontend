package com.example.emergencynow.domain.usecase.call

import com.example.emergencynow.domain.repository.CallRepository

class UpdateCallStatusUseCase(private val repository: CallRepository) {
    suspend operator fun invoke(callId: String, status: String): Result<Unit> {
        return repository.updateCallStatus(callId, status).map { }
    }
}
