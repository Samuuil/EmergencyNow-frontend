package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.DispatcherDataSource
import com.example.emergencynow.data.service.DispatcherService
import com.example.emergencynow.domain.model.request.AssignAmbulanceRequest
import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto

class DispatcherDataSourceImpl(
    private val dispatcherService: DispatcherService,
) : DispatcherDataSource {

    override suspend fun getMyCalls(): List<DispatcherCallDto> {
        return dispatcherService.getMyCalls()
    }

    override suspend fun getAvailableAmbulances(): List<DispatcherAmbulanceSummaryDto> {
        return dispatcherService.getAvailableAmbulances()
    }

    override suspend fun assignAmbulance(callId: String, ambulanceId: String) {
        dispatcherService.assignAmbulance(
            callId = callId,
            body = AssignAmbulanceRequest(ambulanceId = ambulanceId),
        )
    }
}
