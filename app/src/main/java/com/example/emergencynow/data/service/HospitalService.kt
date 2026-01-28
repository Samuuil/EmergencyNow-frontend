package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.request.GetHospitalsRequest
import com.example.emergencynow.domain.model.request.SelectHospitalRequest
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.model.response.HospitalRouteWrapperResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HospitalService {
    @POST("calls/{id}/hospitals")
    suspend fun getHospitalsForCall(
        @Path("id") id: String,
        @Body body: GetHospitalsRequest
    ): List<HospitalDto>

    @POST("calls/{id}/select-hospital")
    suspend fun selectHospitalForCall(
        @Path("id") id: String,
        @Body body: SelectHospitalRequest
    ): HospitalRouteResponse

    @GET("calls/{id}/hospital-route")
    suspend fun getHospitalRoute(@Path("id") id: String): HospitalRouteWrapperResponse
}
