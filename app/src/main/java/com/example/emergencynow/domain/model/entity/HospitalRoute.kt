package com.example.emergencynow.domain.model.entity

data class HospitalRoute(
    val polyline: String?,
    val distance: Int,
    val duration: Int,
    val steps: List<String> = emptyList()
)
