package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.model.response.HospitalRouteResponse

interface HospitalDataSource {
    suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): List<HospitalDto>
    
    suspend fun selectHospitalForCall(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): HospitalRouteResponse
    
    suspend fun getHospitalRoute(callId: String): HospitalRouteResponse
}
