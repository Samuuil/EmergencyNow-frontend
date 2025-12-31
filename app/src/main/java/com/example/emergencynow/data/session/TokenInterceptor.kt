package com.example.emergencynow.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val requestBuilder = chain.request().newBuilder()
            val requestUrl = chain.request().url.toString()

            // Use refresh token for the refresh endpoint, access token for everything else
            val token = if (requestUrl.contains("/auth/refresh")) {
                getRefreshToken()
            } else {
                getAccessToken()
            }

            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
    }

    private fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    private fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }
}
