package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.domain.repository.AmbulanceRepository

class MarkAmbulanceAvailableUseCase(private val repository: Lazy<AmbulanceRepository>) {
    suspend operator fun invoke(ambulanceId: String): Result<Unit> {
        return repository.value.markAmbulanceAvailable(ambulanceId)
    }
}
