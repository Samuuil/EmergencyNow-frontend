package com.example.emergencynow.domain.model.response

import com.google.gson.annotations.SerializedName

data class UserEgnResponse(
    @SerializedName("egn")
    val egn: String
)

