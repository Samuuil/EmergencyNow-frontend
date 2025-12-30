package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Call

interface CallRepository {
    suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double
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
}
