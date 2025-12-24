package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.domain.repository.AmbulanceRepository

class AssignAmbulanceDriverUseCase(private val repository: Lazy<AmbulanceRepository>) {
    suspend operator fun invoke(driverId: String, ambulanceId: String): Result<Unit> {
        return repository.value.assignAmbulanceDriver(ambulanceId, driverId).map { }
    }
}
