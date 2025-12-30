package com.example.emergencynow.domain.usecase.call

import com.example.emergencynow.domain.model.response.CallTrackingResponse
import com.example.emergencynow.domain.repository.CallRepository

class GetCallTrackingUseCase(private val repository: Lazy<CallRepository>) {
    suspend operator fun invoke(callId: String): Result<CallTrackingResponse> {
        val result = repository.value.getCallTracking(callId)
        return result.map { call ->
            CallTrackingResponse(
                callId = call.id,
                status = call.status.name,
                ambulanceId = null,
                driverLatitude = call.ambulanceCurrentLatitude,
                driverLongitude = call.ambulanceCurrentLongitude,
                estimatedArrival = call.estimatedDuration,
                route = call.routePolyline?.let { polyline ->
                    com.example.emergencynow.domain.model.response.RouteDto(
                        polyline = polyline,
                        distance = call.estimatedDistance ?: 0,
                        duration = call.estimatedDuration ?: 0
                    )
                }
            )
        }
    }
}
