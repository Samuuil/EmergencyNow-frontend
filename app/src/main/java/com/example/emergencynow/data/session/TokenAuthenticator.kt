package com.example.emergencynow.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.emergencynow.data.extensions.requestBody
import com.example.emergencynow.data.service.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authService: AuthService,
    private val context: Context,
) : Authenticator {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val refreshTokenURL = "https://emergencynow.samuil.me/auth/refresh"

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            getRequest(response)
        }
    }

    private suspend fun getRequest(response: Response): Request? {
        // Don't retry if already trying to refresh
        if (response.request.url.toString() == refreshTokenURL) {
            logout()
            return null
        }

        // Don't retry login endpoints
        if (response.request.url.toString().contains("/auth/login") || 
            response.request.url.toString().contains("/auth/verify-code")) {
            return null
        }

        val refreshToken = prefs.getString("refresh_token", null) ?: run {
            logout()
            return null
        }

        // Try to refresh token
        val refreshResult = requestBody(
            authService.refreshToken(mapOf("refreshToken" to refreshToken)).execute()
        )

        return refreshResult.fold(
            onSuccess = { tokens ->
                // Save new tokens
                prefs.edit()
                    .putString("access_token", tokens.accessToken)
                    .putString("refresh_token", tokens.refreshToken)
                    .apply()

                // Retry original request with new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokens.accessToken}")
                    .build()
            },
            onFailure = {
                logout()
                null
            }
        )
    }

    private fun logout() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }
}
