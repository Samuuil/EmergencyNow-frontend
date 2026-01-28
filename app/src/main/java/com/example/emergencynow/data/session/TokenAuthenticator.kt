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
        
        if (requestUrl.contains("/auth/refresh")) {
            logout()
            return null
        }

        if (requestUrl.contains("/auth/initiate-login") || 
            requestUrl.contains("/auth/verify-code")) {
            return null
        }

        val refreshToken = prefs.getString("refresh_token", null) ?: run {
            logout()
            return null
        }

        return try {
            val tokens = authService.refresh(RefreshTokenRequest(refreshToken = refreshToken))
            
            prefs.edit()
                .putString("access_token", tokens.accessToken)
                .putString("refresh_token", tokens.refreshToken)
                .apply()

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
