package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.AmbulanceDataSource
import com.example.emergencynow.domain.model.request.AssignDriverRequest
import com.example.emergencynow.domain.model.response.AmbulanceDto
import com.example.emergencynow.data.service.AmbulanceService

class AmbulanceDataSourceImpl(
    private val ambulanceService: AmbulanceService
) : AmbulanceDataSource {
    
    override suspend fun getAvailableAmbulances(): List<AmbulanceDto> {
        return ambulanceService.getAvailableAmbulances().data
    }
    
    override suspend fun getAmbulanceByDriver(driverId: String): AmbulanceDto? {
        return ambulanceService.getAmbulanceByDriver(driverId)
    }
    
    override suspend fun assignAmbulanceDriver(
        ambulanceId: String,
        driverId: String?
    ): AmbulanceDto {
        return ambulanceService.assignAmbulanceDriver(
            id = ambulanceId,
            body = AssignDriverRequest(driverId = driverId)
        )
    }
}
