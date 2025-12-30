package com.example.emergencynow.data.datasource

import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.response.CallTrackingResponse
import com.example.emergencynow.domain.model.response.PaginatedResponse

interface CallDataSource {
    suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double
    ): CallResponse
    
    suspend fun getCallTracking(callId: String): CallTrackingResponse
    
    suspend fun updateCallStatus(
        callId: String,
        status: String
    ): CallResponse
    
    suspend fun getUserCalls(
        userId: String,
        page: Int? = null,
        limit: Int? = null
    ): PaginatedResponse<CallResponse>
}
