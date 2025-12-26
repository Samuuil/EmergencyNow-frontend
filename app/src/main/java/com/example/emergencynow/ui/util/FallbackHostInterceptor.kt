package com.example.emergencynow.ui.util

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Intercepts requests and, on network failure, retries once by rewriting the host/scheme/port
 * to the fallback base (localhost:3000). Useful for developing against a local server.
 */
class FallbackHostInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalReq = chain.request()
        return try {
            // Try normally first
            chain.proceed(originalReq)
        } catch (e: IOException) {
            // Network error: attempt fallback once
            val fallbackBase = NetworkConfig.fallbackBaseUrl()
            val fallbackUrl = NetworkConfig.withHostFrom(fallbackBase, originalReq.url.toString())
            val newReq = originalReq.newBuilder()
                .url(fallbackUrl)
                .build()
            try {
                NetworkConfig.switchToFallback()
                chain.proceed(newReq)
            } catch (e2: IOException) {
                // If fallback also fails, bubble up original error context
                throw e
            }
        }
    }
}
