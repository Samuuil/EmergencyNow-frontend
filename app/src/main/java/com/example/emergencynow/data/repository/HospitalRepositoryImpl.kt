package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.HospitalDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.model.entity.Hospital
import com.example.emergencynow.domain.repository.HospitalRepository
import java.util.Date

class HospitalRepositoryImpl(
    private val hospitalDataSource: HospitalDataSource
) : HospitalRepository {
    
    override suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Hospital>> = safeApiCall {
        hospitalDataSource.getHospitalsForCall(callId, latitude, longitude).map { dto ->
            Hospital(
                id = dto.id,
                name = dto.name,
                address = dto.address ?: "Unknown Address",
                latitude = dto.latitude,
                longitude = dto.longitude,
                phoneNumber = null,
                placeId = null,
                isActive = true,
                createdAt = Date()
            )
        }
    }
    
    override suspend fun getHospitalsForCall(callId: String): Result<List<HospitalDto>> = safeApiCall {
        val dummyHospitals = listOf(
            HospitalDto(
                id = "hosp1",
                name = "City Hospital",
                latitude = 42.6977,
                longitude = 23.3219
            ),
            HospitalDto(
                id = "hosp2",
                name = "Emergency Medical Center",
                latitude = 42.7000,
                longitude = 23.3200
            )
        )
        dummyHospitals
    }
    
    override suspend fun selectHospital(callId: String, hospitalId: String): Result<Unit> = safeApiCall {
        hospitalDataSource.selectHospitalForCall(callId, hospitalId, 0.0, 0.0)
        Unit
    }
    
    override suspend fun selectHospitalForCall(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> = safeApiCall {
        hospitalDataSource.selectHospitalForCall(callId, hospitalId, latitude, longitude)
        Unit
    }
    
    override suspend fun getHospitalRoute(callId: String): Result<HospitalRouteResponse> = safeApiCall {
        hospitalDataSource.getHospitalRoute(callId)
    }
}
