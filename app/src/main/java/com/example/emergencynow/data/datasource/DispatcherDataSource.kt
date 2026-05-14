package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto

interface DispatcherDataSource {
    suspend fun getMyCalls(): List<DispatcherCallDto>

    suspend fun getAvailableAmbulances(): List<DispatcherAmbulanceSummaryDto>

    suspend fun assignAmbulance(callId: String, ambulanceId: String)
}
