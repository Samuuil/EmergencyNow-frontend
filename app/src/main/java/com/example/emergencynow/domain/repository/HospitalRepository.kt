package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.HospitalRoute
import com.example.emergencynow.domain.model.entity.NearbyHospital

interface HospitalRepository {
    suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): Result<List<NearbyHospital>>

    suspend fun selectHospitalForCall(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit>

    suspend fun getHospitalRoute(callId: String): Result<HospitalRoute>
}
