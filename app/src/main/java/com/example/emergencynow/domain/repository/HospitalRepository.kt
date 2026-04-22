package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.model.entity.Hospital

interface HospitalRepository {
    suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Hospital>>

    suspend fun selectHospitalForCall(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit>
    
    suspend fun getHospitalRoute(callId: String): Result<HospitalRouteResponse>
}
