package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.AmbulanceDto

interface AmbulanceDataSource {
    suspend fun getAvailableAmbulances(): List<AmbulanceDto>
    
    suspend fun getAmbulanceByDriver(driverId: String): AmbulanceDto?
    
    suspend fun assignAmbulanceDriver(
        ambulanceId: String,
        driverId: String?
    ): AmbulanceDto
}
