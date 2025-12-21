package com.example.emergencynow.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "emergency_now_prefs")

object AppSettings {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ID = stringPreferencesKey("user_id")
    val EGN = stringPreferencesKey("egn")
}

data class AuthTokens(
    val accessToken: String = "",
    val refreshToken: String = ""
)

fun DataStore<Preferences>.getAuthTokens(): Flow<AuthTokens> = data.map { prefs ->
    AuthTokens(
        accessToken = prefs[AppSettings.ACCESS_TOKEN] ?: "",
        refreshToken = prefs[AppSettings.REFRESH_TOKEN] ?: ""
    )
}

suspend fun DataStore<Preferences>.saveAuthTokens(accessToken: String, refreshToken: String) {
    edit { prefs ->
        prefs[AppSettings.ACCESS_TOKEN] = accessToken
        prefs[AppSettings.REFRESH_TOKEN] = refreshToken
    }
}

suspend fun DataStore<Preferences>.clearAuthTokens() {
    edit { prefs ->
        prefs.remove(AppSettings.ACCESS_TOKEN)
        prefs.remove(AppSettings.REFRESH_TOKEN)
        prefs.remove(AppSettings.USER_ID)
        prefs.remove(AppSettings.EGN)
    }
}
