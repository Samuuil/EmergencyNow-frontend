package com.example.emergencynow.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionManager(private val context: Context) {
    
    private val dataStore: DataStore<Preferences> = context.dataStore
    
    private var currentToken: String = ""
    private var currentRefreshToken: String = ""

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            val tokens = fetchAuthTokens()
            currentToken = tokens.accessToken
            currentRefreshToken = tokens.refreshToken
            _isLoggedIn.value = tokens.accessToken.isNotEmpty()
        }
    }

    suspend fun logout() {
        dataStore.clearAuthTokens()
        currentToken = ""
        currentRefreshToken = ""
        _isLoggedIn.emit(false)
    }

    suspend fun setLoggedIn() {
        _isLoggedIn.value = true
    }

    suspend fun setAuthTokens(accessToken: String, refreshToken: String) {
        dataStore.saveAuthTokens(accessToken, refreshToken)
        currentToken = accessToken
        currentRefreshToken = refreshToken
        if (accessToken.isNotEmpty()) {
            setLoggedIn()
        }
    }

    suspend fun fetchAuthTokens(): AuthTokens {
        return dataStore.getAuthTokens().first()
    }

    fun getAccessToken(): String {
        return currentToken
    }

    fun getRefreshToken(): String {
        return currentRefreshToken
    }

    suspend fun updateTokensFromStorage() {
        val tokens = fetchAuthTokens()
        currentToken = tokens.accessToken
        currentRefreshToken = tokens.refreshToken
    }
}
