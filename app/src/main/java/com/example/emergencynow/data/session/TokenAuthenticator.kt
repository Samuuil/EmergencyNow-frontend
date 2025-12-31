package com.example.emergencynow.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.emergencynow.data.service.AuthService
import com.example.emergencynow.domain.model.request.RefreshTokenRequest
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

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            getRequest(response)
        }
    }

    private suspend fun getRequest(response: Response): Request? {
        val requestUrl = response.request.url.toString()
        
        // Don't retry if already trying to refresh token (avoid infinite loop)
        if (requestUrl.contains("/auth/refresh")) {
            logout()
            return null
        }

        // Don't retry login/registration endpoints
        if (requestUrl.contains("/auth/initiate-login") || 
            requestUrl.contains("/auth/verify-code") ||
            requestUrl.contains("/auth/login")) {
            return null
        }

        val refreshToken = prefs.getString("refresh_token", null) ?: run {
            logout()
            return null
        }

        // Try to refresh token using the correct endpoint
        return try {
            val tokens = authService.refresh(RefreshTokenRequest(refreshToken = refreshToken))
            
            // Save new tokens
            prefs.edit()
                .putString("access_token", tokens.accessToken)
                .putString("refresh_token", tokens.refreshToken)
                .apply()

            // Retry original request with new token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        } catch (e: Exception) {
            logout()
            null
        }
    }

    private fun logout() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }
}
