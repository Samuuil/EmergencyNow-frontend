package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallDetail
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.model.entity.Location
import com.example.emergencynow.domain.model.entity.RouteStep
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.response.RouteStepResponse
import com.example.emergencynow.domain.repository.CallRepository
import java.time.Instant

class CallRepositoryImpl(
    private val callDataSource: CallDataSource
) : CallRepository {

    override suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double,
        patientPhoneNumber: String?
    ): Result<Call> = safeApiCall {
        val response = callDataSource.createCall(description, latitude, longitude, patientPhoneNumber)
        mapResponseToCall(response)
    }

    override suspend fun updateCallStatus(
        callId: String,
        status: CallStatus
    ): Result<Call> = safeApiCall {
        val response = callDataSource.updateCallStatus(callId, status.wire)
        mapResponseToCall(response)
    }

    override suspend fun getMyCalls(
        page: Int?,
        limit: Int?
    ): Result<List<Call>> = safeApiCall {
        val response = callDataSource.getMyCalls(page, limit)
        response.data.map { callResponse ->
            mapResponseToCall(callResponse)
        }
    }

    override suspend fun getCallById(callId: String): Result<CallDetail> = safeApiCall {
        val response = callDataSource.getCallById(callId)
        CallDetail(
            id = response.id,
            status = CallStatus.fromWire(response.status ?: "PENDING"),
            userEgn = response.userEgn,
            patientEgn = response.patientEgn,
            patientPhoneNumber = response.patientPhoneNumber,
            selectedHospitalId = response.selectedHospitalId,
        )
    }

    private fun mapResponseToCall(response: CallResponse): Call {
        return Call(
            id = response.id,
            description = response.description ?: "",
            latitude = response.latitude ?: 0.0,
            longitude = response.longitude ?: 0.0,
            status = CallStatus.fromWire(response.status ?: "PENDING"),
            routePolyline = response.routePolyline,
            estimatedDistance = response.estimatedDistance,
            estimatedDuration = response.estimatedDuration,
            routeSteps = response.routeSteps?.map { it.toDomain() },
            ambulanceCurrentLatitude = response.ambulanceCurrentLatitude,
            ambulanceCurrentLongitude = response.ambulanceCurrentLongitude,
            dispatchedAt = parseInstant(response.dispatchedAt),
            arrivedAt = parseInstant(response.arrivedAt),
            completedAt = parseInstant(response.completedAt),
            createdAt = parseInstant(response.createdAt),
            selectedHospitalId = response.selectedHospitalId,
            selectedHospitalName = response.selectedHospitalName,
            hospitalRoutePolyline = response.hospitalRoutePolyline,
            hospitalRouteDistance = response.hospitalRouteDistance,
            hospitalRouteDuration = response.hospitalRouteDuration,
            hospitalRouteSteps = response.hospitalRouteSteps?.map { it.toDomain() },
            patientEgn = response.patientEgn,
            patientPhoneNumber = response.patientPhoneNumber,
        )
    }

    private fun RouteStepResponse.toDomain(): RouteStep = RouteStep(
        distance = distance,
        duration = duration,
        instruction = instruction,
        startLocation = Location(lat = startLocation?.lat ?: 0.0, lng = startLocation?.lng ?: 0.0),
        endLocation = Location(lat = endLocation?.lat ?: 0.0, lng = endLocation?.lng ?: 0.0),
    )

    private fun parseInstant(s: String?): Instant? = s?.let {
        try { Instant.parse(it) } catch (e: Exception) { null }
    }
}
