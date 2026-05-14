package com.example.emergencynow.domain.usecase.hospital

import com.example.emergencynow.domain.model.entity.HospitalRoute
import com.example.emergencynow.domain.repository.HospitalRepository

class GetHospitalRouteUseCase(private val repository: HospitalRepository) {
    suspend operator fun invoke(callId: String): Result<HospitalRoute> {
        return repository.getHospitalRoute(callId)
    }
}
