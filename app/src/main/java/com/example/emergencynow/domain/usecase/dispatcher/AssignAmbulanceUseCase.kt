package com.example.emergencynow.domain.usecase.dispatcher

import com.example.emergencynow.domain.repository.DispatcherRepository

class AssignAmbulanceUseCase(private val repository: DispatcherRepository) {
    suspend operator fun invoke(callId: String, ambulanceId: String): Result<Unit> {
        return repository.assignAmbulance(callId, ambulanceId)
    }
}
