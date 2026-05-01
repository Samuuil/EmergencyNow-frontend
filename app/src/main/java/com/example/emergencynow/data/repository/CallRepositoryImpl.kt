package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.entity.CallDetail
import com.example.emergencynow.domain.model.entity.CallStatus
import com.example.emergencynow.domain.model.response.CallResponse
import com.example.emergencynow.domain.model.mapper.toDomain
import com.example.emergencynow.domain.repository.CallRepository
import java.time.Instant

class CallRepositoryImpl(
    private val callDataSource: CallDataSource
) : CallRepository {

    override suspend fun createCall(
        description: String,
        latitude: Double,
        longitude: Double
    ): Result<Call> = safeApiCall {
        val response = callDataSource.createCall(description, latitude, longitude)
        mapResponseToCall(response)
    }

    override suspend fun getCallTracking(callId: String): Result<Call> = safeApiCall {
        val response = callDataSource.getCallTracking(callId)
        Call(
            id = response.callId,
            description = "",
            latitude = 0.0,
            longitude = 0.0,
            status = CallStatus.fromWire(response.status),
            routePolyline = response.route?.polyline,
            estimatedDistance = response.route?.distance,
            estimatedDuration = response.route?.duration,
            routeSteps = response.route?.steps?.map { step ->
                com.example.emergencynow.domain.model.entity.RouteStep(
                    distance = step.distance,
                    duration = step.duration,
                    instruction = step.instruction,
                    startLocation = com.example.emergencynow.domain.model.entity.Location(
                        lat = step.startLocation.lat,
                        lng = step.startLocation.lng
                    ),
                    endLocation = com.example.emergencynow.domain.model.entity.Location(
                        lat = step.endLocation.lat,
                        lng = step.endLocation.lng
                    )
                )
            },
            ambulanceCurrentLatitude = response.driverLatitude,
            ambulanceCurrentLongitude = response.driverLongitude,
            dispatchedAt = null,
            arrivedAt = null,
            completedAt = null,
            createdAt = null,
            selectedHospitalId = null,
            selectedHospitalName = null,
            hospitalRoutePolyline = null,
            hospitalRouteDistance = null,
            hospitalRouteDuration = null,
            hospitalRouteSteps = null
        )
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
            status = CallStatus.fromWire(response.status),
            userEgn = response.userEgn,
            ambulanceId = response.ambulanceId,
            hospitalId = response.hospitalId
        )
    }

    private fun mapResponseToCall(response: CallResponse): Call {
        return Call(
            id = response.id,
            description = response.description,
            latitude = response.latitude,
            longitude = response.longitude,
            status = CallStatus.fromWire(response.status),
            routePolyline = null,
            estimatedDistance = null,
            estimatedDuration = null,
            routeSteps = null,
            ambulanceCurrentLatitude = null,
            ambulanceCurrentLongitude = null,
            dispatchedAt = parseInstant(response.dispatchedAt),
            arrivedAt = null,
            completedAt = null,
            createdAt = parseInstant(response.createdAt),
            selectedHospitalId = response.hospitalId,
            selectedHospitalName = null,
            hospitalRoutePolyline = null,
            hospitalRouteDistance = null,
            hospitalRouteDuration = null,
            hospitalRouteSteps = null
        )
    }

    private fun parseInstant(s: String?): Instant? = s?.let {
        try { Instant.parse(it) } catch (e: Exception) { null }
    }
}
