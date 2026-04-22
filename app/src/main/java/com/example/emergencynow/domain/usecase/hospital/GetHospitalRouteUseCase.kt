package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.repository.HospitalRepository

class GetHospitalRouteUseCase(private val repository: HospitalRepository) {
    suspend operator fun invoke(callId: String): Result<HospitalRouteResponse> {
        return repository.getHospitalRoute(callId)
    }
}
