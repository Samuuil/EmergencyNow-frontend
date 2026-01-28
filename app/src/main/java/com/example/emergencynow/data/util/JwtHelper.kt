package com.example.emergencynow.data.util

import android.util.Base64
import com.example.emergencynow.domain.model.response.JwtPayload
import com.google.gson.Gson

object JwtHelper {
    
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
}
