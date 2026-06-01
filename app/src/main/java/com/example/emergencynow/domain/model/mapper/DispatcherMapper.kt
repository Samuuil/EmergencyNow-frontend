package com.example.emergencynow.domain.model.mapper

import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.model.entity.PatientRecord
import com.example.emergencynow.domain.model.response.DispatcherAmbulanceSummaryDto
import com.example.emergencynow.domain.model.response.DispatcherCallDto
import com.example.emergencynow.domain.model.response.PatientDto

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
        callId = callId,
        description = description,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        userName = userName,
        patient = patient?.toDomain(),
    )
}

fun PatientDto.toDomain(): PatientRecord {
    return PatientRecord(
        egn = egn,
        fullName = fullName,
        phoneNumber = phoneNumber,
        email = email,
        bloodType = bloodType,
        allergies = allergies,
        medicines = medicines,
        illnesses = illnesses,
        height = height,
        weight = weight,
        gender = gender,
        dateOfBirth = dateOfBirth,
    )
}
