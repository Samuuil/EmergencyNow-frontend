package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/refresh-token")
    fun refreshToken(@Body body: Map<String, String>): retrofit2.Call<TokenResponse>
}
