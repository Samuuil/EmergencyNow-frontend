package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.data.model.request.CreateCallRequest
import com.example.emergencynow.data.model.response.CallResponse
import com.example.emergencynow.data.model.response.CallTrackingResponse
import com.example.emergencynow.data.model.response.PaginatedResponse
import com.example.emergencynow.data.service.CallService

class CallDataSourceImpl(
    private val callService: CallService
) : CallDataSource {
    
    override suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double
    ): CallResponse {
        return callService.createCall(
            CreateCallRequest(
                description = description,
                latitude = latitude,
                longitude = longitude
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
    
    override suspend fun getUserCalls(
        userId: String,
        page: Int?,
        limit: Int?
    ): PaginatedResponse<CallResponse> {
        return callService.getUserCalls(
            userId = userId,
            page = page,
            limit = limit
        )
    }
}
