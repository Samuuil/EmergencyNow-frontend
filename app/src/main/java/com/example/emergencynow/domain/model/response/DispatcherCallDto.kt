package com.example.emergencynow.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class DispatcherCallDto(
    val id: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String? = null,
    val user: DispatcherCallUserDto? = null,
)

@Serializable
data class DispatcherCallUserDto(
    val id: String? = null,
    val stateArchive: DispatcherCallStateArchiveDto? = null,
)

@Serializable
data class DispatcherCallStateArchiveDto(
    val fullName: String? = null,
)
