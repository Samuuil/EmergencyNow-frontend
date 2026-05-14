package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.request.AssignAmbulanceRequest
import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DispatcherService {
    @GET("dispatchers/me/calls")
    suspend fun getMyCalls(): List<DispatcherCallDto>

    @GET("dispatchers/me/ambulances")
    suspend fun getAvailableAmbulances(): List<DispatcherAmbulanceSummaryDto>

    @POST("dispatchers/me/calls/{callId}/assign-ambulance")
    suspend fun assignAmbulance(
        @Path("callId") callId: String,
        @Body body: AssignAmbulanceRequest,
    ): Map<String, String>
}
