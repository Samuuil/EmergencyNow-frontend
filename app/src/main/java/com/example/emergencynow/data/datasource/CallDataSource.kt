package com.example.emergencynow.data.datasource

import com.example.emergencynow.data.model.response.CallResponse
import com.example.emergencynow.data.model.response.CallTrackingResponse

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
}
