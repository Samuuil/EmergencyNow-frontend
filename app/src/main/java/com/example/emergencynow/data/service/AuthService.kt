package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.request.InitiateLoginRequest
import com.example.emergencynow.data.model.request.RefreshTokenRequest
import com.example.emergencynow.data.model.request.VerifyCodeRequest
import com.example.emergencynow.data.model.response.InitiateLoginResponse
import com.example.emergencynow.data.model.response.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/initiate-login")
    suspend fun initiateLogin(@Body body: InitiateLoginRequest): InitiateLoginResponse

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): TokenResponse

    @POST("auth/refresh-token")
    fun refreshToken(@Body body: Map<String, String>): retrofit2.Call<TokenResponse>
}
