package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto

fun DispatcherAmbulanceSummaryDto.toDomain(): DispatcherAmbulanceSummary {
    return DispatcherAmbulanceSummary(
        id = id,
        licensePlate = licensePlate,
        vehicleModel = vehicleModel,
        latitude = latitude,
        longitude = longitude,
        driverId = driverId,
        driverOnline = driverOnline,
        available = available,
    )
}

fun DispatcherCallDto.toDomain(): DispatcherCallOffer {
    return DispatcherCallOffer(
        callId = id,
        description = description ?: "",
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt ?: "",
        userName = user?.stateArchive?.fullName,
    )
}
