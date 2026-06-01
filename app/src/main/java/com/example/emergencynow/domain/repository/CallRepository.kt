package com.example.emergencynow.domain.repository

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallDetail
import com.example.emergencynow.domain.model.entity.CallStatus

interface CallRepository {
    suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double,
        patientPhoneNumber: String? = null
    ): Result<Call>

    suspend fun updateCallStatus(
        callId: String,
        status: CallStatus
    ): Result<Call>

    suspend fun getMyCalls(
        page: Int? = null,
        limit: Int? = null
    ): Result<List<Call>>

    suspend fun getCallById(callId: String): Result<CallDetail>
}
