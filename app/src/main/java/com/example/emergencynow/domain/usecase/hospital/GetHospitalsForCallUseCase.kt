package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.repository.HospitalRepository

class GetHospitalsForCallUseCase(private val repository: Lazy<HospitalRepository>) {
    suspend operator fun invoke(callId: String, latitude: Double, longitude: Double): Result<List<HospitalDto>> {
        return repository.value
            .getHospitalsForCall(callId, latitude, longitude)
            .map { hospitals ->
                hospitals.map { h ->
                    HospitalDto(
                        id = h.id,
                        name = h.name,
                        latitude = h.latitude,
                        longitude = h.longitude
                    )
                }
            }
    }
}
