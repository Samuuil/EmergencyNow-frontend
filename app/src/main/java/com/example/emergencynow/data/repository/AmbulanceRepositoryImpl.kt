package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.AmbulanceDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Ambulance
import com.example.emergencynow.domain.repository.AmbulanceRepository
import java.util.Date

class AmbulanceRepositoryImpl(
    private val ambulanceDataSource: AmbulanceDataSource
) : AmbulanceRepository {
    
    override suspend fun getAvailableAmbulances(): Result<List<Ambulance>> = safeApiCall {
        ambulanceDataSource.getAvailableAmbulances().map { dto ->
            Ambulance(
                id = dto.id,
                licensePlate = dto.licensePlate,
                vehicleModel = dto.vehicleModel,
                latitude = dto.latitude,
                longitude = dto.longitude,
                available = dto.available ?: true,
                driverId = dto.driverId,
                lastCallAcceptedAt = null,
                createdAt = Date(),
                updatedAt = Date()
            )
        }
    }
    
    override suspend fun getAmbulanceByDriver(driverId: String): Result<Ambulance?> = safeApiCall {
        ambulanceDataSource.getAmbulanceByDriver(driverId)?.let { dto ->
            Ambulance(
                id = dto.id,
                licensePlate = dto.licensePlate,
                vehicleModel = dto.vehicleModel,
                latitude = dto.latitude,
                longitude = dto.longitude,
                available = dto.available ?: true,
                driverId = dto.driverId,
                lastCallAcceptedAt = null,
                createdAt = Date(),
                updatedAt = Date()
            )
        }
    }
    
    override suspend fun assignAmbulanceDriver(
        ambulanceId: String,
        driverId: String?
    ): Result<Ambulance> = safeApiCall {
        val dto = ambulanceDataSource.assignAmbulanceDriver(ambulanceId, driverId)
        Ambulance(
            id = dto.id,
            licensePlate = dto.licensePlate,
            vehicleModel = dto.vehicleModel,
            latitude = dto.latitude,
            longitude = dto.longitude,
            available = dto.available ?: false,
            driverId = dto.driverId,
            lastCallAcceptedAt = null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}
