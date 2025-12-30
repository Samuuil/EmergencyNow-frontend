package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.HospitalDataSource
import com.example.emergencynow.domain.model.request.GetHospitalsRequest
import com.example.emergencynow.domain.model.request.SelectHospitalRequest
import com.example.emergencynow.domain.model.response.HospitalDto
import com.example.emergencynow.domain.model.response.HospitalRouteResponse
import com.example.emergencynow.domain.model.response.HospitalRouteWrapperResponse
import com.example.emergencynow.data.service.HospitalService

class HospitalDataSourceImpl(
    private val hospitalService: HospitalService
) : HospitalDataSource {
    
    override suspend fun getHospitalsForCall(
        callId: String,
        latitude: Double,
        longitude: Double
    ): List<HospitalDto> {
        return hospitalService.getHospitalsForCall(
            id = callId,
            body = GetHospitalsRequest(
                latitude = latitude,
                longitude = longitude
            )
        )
    }
    
    override suspend fun selectHospitalForCall(
        callId: String,
        hospitalId: String,
        latitude: Double,
        longitude: Double
    ): HospitalRouteResponse {
        return hospitalService.selectHospitalForCall(
            id = callId,
            body = SelectHospitalRequest(
                hospitalId = hospitalId,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
    
    override suspend fun getHospitalRoute(callId: String): HospitalRouteResponse {
        val wrapper: HospitalRouteWrapperResponse = hospitalService.getHospitalRoute(id = callId)
        val route = wrapper.route
        // Explicitly convert nullable instructions to non-nullable list
        val instructions = route?.steps?.map { it.instruction } ?: emptyList()
        val steps: List<String> = instructions.filterNotNull()
        return HospitalRouteResponse(
            polyline = route?.polyline,
            distance = route?.distance ?: 0,
            duration = route?.duration ?: 0,
            steps = steps
        )
    }
}
