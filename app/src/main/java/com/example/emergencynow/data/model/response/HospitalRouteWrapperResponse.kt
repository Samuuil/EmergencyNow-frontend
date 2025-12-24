package com.example.emergencynow.data.model.response

import com.google.gson.annotations.SerializedName
data class HospitalRouteWrapperResponse(
    @SerializedName("hospital")
    val hospital: HospitalSummaryResponse?,

    @SerializedName("route")
    val route: HospitalRouteInnerResponse?
)

data class HospitalSummaryResponse(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?
)

data class HospitalRouteInnerResponse(
    @SerializedName("polyline")
    val polyline: String?,

    @SerializedName("distance")
    val distance: Int?,

    @SerializedName("duration")
    val duration: Int?,

    @SerializedName("steps")
    val steps: List<Any>? = null
)
