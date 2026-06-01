package com.example.emergencynow.data.service

import com.example.emergencynow.domain.model.response.UserEgnResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("users/{id}/egn")
    suspend fun getUserEgn(@Path("id") id: String): UserEgnResponse

    @GET("users/me/egn")
    suspend fun getMyEgn(): UserEgnResponse
}
