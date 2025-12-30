package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.response.CallTrackingResponse
import com.example.emergencynow.domain.model.response.PaginatedResponse
import com.example.emergencynow.data.service.CallService

class CallDataSourceImpl(
    private val callService: CallService
) : CallDataSource {
    
    override suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double,
        userEgn: String
    ): CallResponse {
        return callService.createCall(
            CreateCallRequest(
                description = description,
                latitude = latitude,
                longitude = longitude,
                userEgn = userEgn
            )
        )
    }
    
    override suspend fun getCallTracking(callId: String): CallTrackingResponse {
        return callService.getCallTracking(id = callId)
    }
    
    override suspend fun updateCallStatus(
        callId: String,
        status: String
    ): CallResponse {
        return callService.updateCallStatus(
            id = callId,
            body = mapOf("status" to status)
        )
    }
    
    override suspend fun getMyCalls(
        page: Int?,
        limit: Int?
    ): PaginatedResponse<CallResponse> {
        return callService.getMyCalls(
            page = page,
            limit = limit
        )
    }
    
    override suspend fun getCallById(callId: String): CallResponse {
        return callService.getCallById(id = callId)
    }
}
