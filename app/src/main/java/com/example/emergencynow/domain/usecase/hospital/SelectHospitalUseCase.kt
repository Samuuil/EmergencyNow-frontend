package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.repository.HospitalRepository

class SelectHospitalUseCase(private val repository: Lazy<HospitalRepository>) {
    suspend operator fun invoke(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return repository.value.selectHospitalForCall(callId, hospitalId, latitude, longitude)
    }
}
