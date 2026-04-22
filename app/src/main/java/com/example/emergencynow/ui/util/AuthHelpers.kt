package com.example.emergencynow.ui.util

import android.content.Context
import android.util.Base64
import com.example.emergencynow.domain.model.request.LoginMethod
import com.example.emergencynow.domain.model.response.JwtPayload
import com.google.gson.Gson

fun parseJwt(token: String): JwtPayload? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payloadPart = parts[1]
        val decodedBytes = Base64.decode(payloadPart, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val json = String(decodedBytes, Charsets.UTF_8)
        Gson().fromJson(json, JwtPayload::class.java)
    } catch (e: Exception) {
        null
    }
}

object AuthSession {
    @Volatile var egn: String? = null
    @Volatile var lastMethod: LoginMethod? = null
    @Volatile var userId: String? = null
}

class AuthStorage(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(v) = prefs.edit().putString("access_token", v).apply()

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(v) = prefs.edit().putString("refresh_token", v).apply()

    fun clear() = prefs.edit().clear().apply()
}
