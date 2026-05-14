package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.domain.repository.AmbulanceRepository

class AssignAmbulanceDriverUseCase(private val repository: AmbulanceRepository) {
    suspend operator fun invoke(driverId: String, ambulanceId: String): Result<Unit> {
        return repository.assignAmbulanceDriver(ambulanceId, driverId).map { }
    }
}
