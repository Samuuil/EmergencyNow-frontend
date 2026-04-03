package com.example.emergencynow.ui.util

import com.example.emergencynow.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl

object NetworkConfig {
    // Use BuildConfig.BASE_URL from local.properties
    private val PRIMARY_BASE = BuildConfig.BASE_URL.removeSuffix("/")
    private const val FALLBACK_BASE = "http://127.0.0.1:3000"

    @Volatile
    private var current: String = PRIMARY_BASE

    fun isPrimary(): Boolean = current == PRIMARY_BASE

    fun switchToFallback() {
        current = FALLBACK_BASE
    }

    fun currentBase(): String = current

    fun retrofitBaseUrl(): String {
        val base = current
        return if (base.endsWith("/")) base else "$base/"
    }

    fun fallbackBaseUrl(): String = FALLBACK_BASE

    fun primaryBaseUrl(): String = PRIMARY_BASE

    fun withHostFrom(base: String, originalUrl: String): String {
        val baseUrl = base.toHttpUrl()
        val orig = originalUrl.toHttpUrl()
        return orig.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()
            .toString()
    }
}
