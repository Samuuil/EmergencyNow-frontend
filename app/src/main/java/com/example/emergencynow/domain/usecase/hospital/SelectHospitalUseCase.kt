package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.repository.HospitalRepository

class SelectHospitalUseCase(private val repository: HospitalRepository) {
    suspend operator fun invoke(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return repository.selectHospitalForCall(callId, hospitalId, latitude, longitude)
    }
}
