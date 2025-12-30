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
    var egn: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var lastMethod: LoginMethod? = null
    var userId: String? = null
}

object AuthStorage {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    data class Tokens(val accessToken: String?, val refreshToken: String?)

    fun loadTokens(context: Context): Tokens {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Tokens(
            accessToken = prefs.getString(KEY_ACCESS, null),
            refreshToken = prefs.getString(KEY_REFRESH, null)
        )
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
