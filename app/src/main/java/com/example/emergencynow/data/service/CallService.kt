package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.request.CreateCallRequest
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.response.CallTrackingResponse
import com.example.emergencynow.domain.model.response.PaginatedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CallService {
    @POST("calls")
    suspend fun createCall(@Body body: CreateCallRequest): CallResponse

    @GET("calls/{id}/tracking")
    suspend fun getCallTracking(@Path("id") id: String): CallTrackingResponse

    @PATCH("calls/{id}/status")
    suspend fun updateCallStatus(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): CallResponse

    @GET("calls/me")
    suspend fun getMyCalls(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): PaginatedResponse<CallResponse>
    
    @GET("calls/{id}")
    suspend fun getCallById(@Path("id") id: String): CallResponse
}
