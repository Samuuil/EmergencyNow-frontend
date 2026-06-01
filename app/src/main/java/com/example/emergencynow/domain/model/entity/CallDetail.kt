package com.example.emergencynow.domain.model.entity

data class CallDetail(
    val id: String,
    val status: CallStatus,
    val userEgn: String?,
    val patientEgn: String?,
    val patientPhoneNumber: String?,
    val selectedHospitalId: String?,
)
