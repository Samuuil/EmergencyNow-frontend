package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.response.CallResponse

interface CallRepository {
    suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double,
        userEgn: String
    ): Result<Call>
    
    suspend fun getCallTracking(callId: String): Result<Call>
    
    suspend fun updateCallStatus(
        callId: String,
        status: String
    ): Result<Call>
    
    suspend fun getMyCalls(
        page: Int? = null,
        limit: Int? = null
    ): Result<List<Call>>
    
    suspend fun getCallById(callId: String): Result<CallResponse>
}
