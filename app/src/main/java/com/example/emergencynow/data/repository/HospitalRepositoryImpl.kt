package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.HospitalDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.HospitalRoute
import com.example.emergencynow.domain.model.entity.NearbyHospital
import com.example.emergencynow.domain.repository.HospitalRepository

class HospitalRepositoryImpl(
    private val hospitalDataSource: HospitalDataSource
) : HospitalRepository {

    override suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): Result<List<NearbyHospital>> = safeApiCall {
        hospitalDataSource.getHospitalsForCall(callId, latitude, longitude).map { dto ->
            NearbyHospital(
                id = dto.id,
                name = dto.name,
                latitude = dto.latitude,
                longitude = dto.longitude,
                distanceMeters = dto.distance,
                availableBeds = dto.availableBeds,
                address = dto.address
            )
        }
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

    override suspend fun getHospitalRoute(callId: String): Result<HospitalRoute> = safeApiCall {
        val response = hospitalDataSource.getHospitalRoute(callId)
        HospitalRoute(
            polyline = response.polyline,
            distance = response.distance,
            duration = response.duration,
            steps = response.steps
        )
    }
}
