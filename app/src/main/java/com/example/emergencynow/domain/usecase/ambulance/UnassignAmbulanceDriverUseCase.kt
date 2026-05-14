package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.domain.repository.AmbulanceRepository

class UnassignAmbulanceDriverUseCase(private val repository: AmbulanceRepository) {
    suspend operator fun invoke(ambulanceId: String): Result<Unit> {
        return repository.assignAmbulanceDriver(ambulanceId, null).map { }
    }
}
