package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.data.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.repository.HospitalRepository

class GetHospitalRouteUseCase(private val repository: Lazy<HospitalRepository>) {
    suspend operator fun invoke(callId: String): Result<HospitalRouteResponse> {
        return repository.value.getHospitalRoute(callId)
    }
}
