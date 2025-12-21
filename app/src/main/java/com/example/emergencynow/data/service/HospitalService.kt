package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.GetHospitalsRequest
import com.example.emergencynow.data.model.HospitalDto
import com.example.emergencynow.data.model.HospitalRouteResponse
import com.example.emergencynow.data.model.SelectHospitalRequest
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
    suspend fun getHospitalRoute(@Path("id") id: String): HospitalRouteResponse
}
