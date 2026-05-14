package com.example.emergencynow.data.session

import com.example.emergencynow.ui.util.AuthStorage
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val authStorage: AuthStorage) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val requestUrl = chain.request().url.toString()

        val token = if (requestUrl.contains("/auth/refresh")) {
            authStorage.refreshToken
        } else {
            authStorage.accessToken
        }

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
