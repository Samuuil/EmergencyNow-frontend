package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer

interface DispatcherRepository {
    suspend fun getMyCalls(): Result<List<DispatcherCallOffer>>

    suspend fun getAvailableAmbulances(): Result<List<DispatcherAmbulanceSummary>>

    suspend fun assignAmbulance(callId: String, ambulanceId: String): Result<Unit>
}
