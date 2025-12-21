package com.example.emergencynow.data.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("users/user-role/{id}")
    suspend fun getUserRole(@Path("id") id: String): ResponseBody
}
