package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.model.entity.NearbyHospital
import com.example.emergencynow.domain.repository.HospitalRepository

class GetHospitalsForCallUseCase(private val repository: HospitalRepository) {
    suspend operator fun invoke(callId: String, latitude: Double, longitude: Double): Result<List<NearbyHospital>> {
        return repository.getHospitalsForCall(callId, latitude, longitude)
    }
}
