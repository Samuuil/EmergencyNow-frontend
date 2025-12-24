package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Ambulance

interface AmbulanceRepository {
    suspend fun getAvailableAmbulances(): Result<List<Ambulance>>
    
    suspend fun getAmbulanceByDriver(driverId: String): Result<Ambulance?>
    
    suspend fun assignAmbulanceDriver(
        ambulanceId: String,
        driverId: String?
    ): Result<Ambulance>
}
