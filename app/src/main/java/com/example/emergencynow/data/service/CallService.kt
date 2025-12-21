package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.CallResponse
import com.example.emergencynow.data.model.CallTrackingResponse
import com.example.emergencynow.data.model.CreateCallRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
}
