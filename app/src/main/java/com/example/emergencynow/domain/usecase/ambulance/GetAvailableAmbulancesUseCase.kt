package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.domain.model.response.AmbulanceDto
import com.example.emergencynow.domain.repository.AmbulanceRepository

class GetAvailableAmbulancesUseCase(private val repository: Lazy<AmbulanceRepository>) {
    suspend operator fun invoke(): Result<List<AmbulanceDto>> {
        val result = repository.value.getAvailableAmbulances()
        return result.map { ambulances ->
            ambulances.map {
                AmbulanceDto(
                    id = it.id,
                    licensePlate = it.licensePlate,
                    type = it.vehicleModel,
                    status = if (it.available) "AVAILABLE" else "BUSY",
                    latitude = it.latitude,
                    longitude = it.longitude,
                    driverId = it.driverId
                )
            }
        }
    }
}
