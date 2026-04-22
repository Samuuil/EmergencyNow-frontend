package com.example.emergencynow.data.session

import com.example.emergencynow.data.service.AuthService
import com.example.emergencynow.domain.model.request.RefreshTokenRequest
import com.example.emergencynow.ui.util.AuthStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authService: AuthService,
    private val authStorage: AuthStorage,
) : Authenticator {

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

        val refreshToken = authStorage.refreshToken ?: run {
            logout()
            return null
        }

        return try {
            val tokens = authService.refresh(RefreshTokenRequest(refreshToken = refreshToken))
            authStorage.accessToken = tokens.accessToken
            authStorage.refreshToken = tokens.refreshToken

            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        } catch (e: Exception) {
            logout()
            null
        }
    }

    private fun logout() {
        authStorage.clear()
    }
}
