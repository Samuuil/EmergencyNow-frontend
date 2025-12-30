package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.CallDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.model.mapper.toDomain
import com.example.emergencynow.domain.repository.CallRepository

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
            status = parseCallStatus(response.status),
            routePolyline = response.route?.polyline,
            estimatedDistance = response.route?.distance,
            estimatedDuration = response.route?.duration,
            routeSteps = null,
            ambulanceCurrentLatitude = response.driverLatitude,
            ambulanceCurrentLongitude = response.driverLongitude,
            dispatchedAt = null,
            arrivedAt = null,
            completedAt = null,
            createdAt = java.util.Date(),
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
        status: String
    ): Result<Call> = safeApiCall {
        val response = callDataSource.updateCallStatus(callId, status)
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
    
    private fun mapResponseToCall(response: com.example.emergencynow.domain.model.response.CallResponse): Call {
        return Call(
            id = response.id,
            description = response.description,
            latitude = response.latitude,
            longitude = response.longitude,
            status = parseCallStatus(response.status),
            routePolyline = null,
            estimatedDistance = null,
            estimatedDuration = null,
            routeSteps = null,
            ambulanceCurrentLatitude = null,
            ambulanceCurrentLongitude = null,
            dispatchedAt = parseDate(response.dispatchedAt),
            arrivedAt = null,
            completedAt = null,
            createdAt = parseDate(response.createdAt) ?: java.util.Date(),
            selectedHospitalId = response.hospitalId,
            selectedHospitalName = null,
            hospitalRoutePolyline = null,
            hospitalRouteDistance = null,
            hospitalRouteDuration = null,
            hospitalRouteSteps = null
        )
    }
    
    private fun parseDate(dateString: String?): java.util.Date? {
        if (dateString == null) return null
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseCallStatus(status: String): com.example.emergencynow.domain.model.entity.CallStatus {
        return when (status.uppercase()) {
            "PENDING" -> com.example.emergencynow.domain.model.entity.CallStatus.PENDING
            "DISPATCHED" -> com.example.emergencynow.domain.model.entity.CallStatus.DISPATCHED
            "EN_ROUTE" -> com.example.emergencynow.domain.model.entity.CallStatus.EN_ROUTE
            "ARRIVED" -> com.example.emergencynow.domain.model.entity.CallStatus.ARRIVED
            "NAVIGATING_TO_HOSPITAL" -> com.example.emergencynow.domain.model.entity.CallStatus.NAVIGATING_TO_HOSPITAL
            "COMPLETED" -> com.example.emergencynow.domain.model.entity.CallStatus.COMPLETED
            "CANCELLED" -> com.example.emergencynow.domain.model.entity.CallStatus.CANCELLED
            else -> com.example.emergencynow.domain.model.entity.CallStatus.PENDING
        }
    }
}
