package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.AmbulanceDto
import com.example.emergencynow.data.model.AssignDriverRequest
import com.example.emergencynow.data.model.PaginatedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AmbulanceService {
    @GET("ambulances/available")
    suspend fun getAvailableAmbulances(): PaginatedResponse<AmbulanceDto>

    @GET("ambulances/driver/{driverId}")
    suspend fun getAmbulanceByDriver(@Path("driverId") driverId: String): AmbulanceDto?

    @PATCH("ambulances/{id}/driver")
    suspend fun assignAmbulanceDriver(
        @Path("id") id: String,
        @Body body: AssignDriverRequest
    ): AmbulanceDto
}
