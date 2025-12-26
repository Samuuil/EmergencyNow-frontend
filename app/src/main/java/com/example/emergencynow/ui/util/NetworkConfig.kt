package com.example.emergencynow.ui.util

import okhttp3.HttpUrl.Companion.toHttpUrl

object NetworkConfig {
    // Primary public URL
    private const val PRIMARY_BASE = "https://emergencynow.samuil.me"
    // Fallback to local dev server (requires ADB reverse when using a physical device)
    private const val FALLBACK_BASE = "http://127.0.0.1:3000"

    @Volatile
    private var current: String = PRIMARY_BASE

    fun isPrimary(): Boolean = current == PRIMARY_BASE

    fun switchToFallback() {
        current = FALLBACK_BASE
    }

    fun currentBase(): String = current

    // Retrofit requires a trailing slash
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
