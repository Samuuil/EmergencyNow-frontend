package com.example.emergencynow.data.service

import com.example.emergencynow.data.model.InitiateLoginRequest
import com.example.emergencynow.data.model.InitiateLoginResponse
import com.example.emergencynow.data.model.RefreshTokenRequest
import com.example.emergencynow.data.model.TokenResponse
import com.example.emergencynow.data.model.VerifyCodeRequest
import retrofit2.Response
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
