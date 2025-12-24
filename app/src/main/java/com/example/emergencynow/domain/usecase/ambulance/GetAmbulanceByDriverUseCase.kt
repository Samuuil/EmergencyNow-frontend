package com.example.emergencynow.domain.usecase.ambulance

import com.example.emergencynow.data.model.response.AmbulanceDto
import com.example.emergencynow.domain.model.entity.Ambulance
import com.example.emergencynow.domain.repository.AmbulanceRepository

class GetAmbulanceByDriverUseCase(private val repository: Lazy<AmbulanceRepository>) {
    suspend operator fun invoke(driverId: String): Result<AmbulanceDto?> {
        val result = repository.value.getAmbulanceByDriver(driverId)
        return result.map { ambulance ->
            ambulance?.let {
                AmbulanceDto(
                    id = it.id,
                    licensePlate = it.licensePlate,
                    type = it.vehicleModel,
                    status = if (it.available) "AVAILABLE" else "BUSY",
                    latitude = it.latitude,
                    longitude = it.longitude,
                    driverId = it.driverId ?: driverId
                )
            }
        }
    }
    
    suspend fun getAmbulance(driverId: String): Result<Ambulance?> {
        return repository.value.getAmbulanceByDriver(driverId)
    }
}
